package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_CONNECTION_PROPERTY;
import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.JdbcDatabaseContainer.NoDriverFoundException;

import com.exasol.dbbuilder.dialects.Table;
import com.exasol.dbbuilder.dialects.exasol.ConnectionDefinition;

/**
 * This class exercises a set of tests defined in the base class on a local Exasol, using {@code IMPORT} via a EXA
 * connection.
 * <p>
 * In this case the Adapter uses a different (JDBC) connection to attach to the database than the ExaLoader which runs
 * this {@code IMPORT}.
 * </p>
 * <p>
 * These tests take the following specialties of a local connection into account:
 * </p>
 * <ul>
 * <li>{@code INTERVAL} types are reported with JDBC type name {@code VARCHAR} in ResultSets</li>
 * <li>{@code HASHTYPE} types are reported with JDBC type name {@code VARCHAR} in ResultSets</li>
 * <li>{@code GEOMETRY} types are reported with JDBC type name {@code VARCHAR} in ResultSets</li>
 * <ul>
 */
class ExasolSqlDialectExaConnectionIT extends AbstractRemoteExasolVirtualSchemaConnectionIT {
    private static final String EXA_CONNECTION_NAME = "EXA_CONNECTION";
    private ConnectionDefinition exaConnection;

    @Override
    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        this.exaConnection = objectFactory.createConnectionDefinition(EXA_CONNECTION_NAME, getTargetAddress(),
                this.user.getName(), this.user.getPassword());
    }

    private String getTargetAddress() {
        if (exasolVersionSupportsFingerprintInAddress()) {
            final String fingerprint = EXASOL.getTlsCertificateFingerprint().orElseThrow();
            return "127.0.0.1/" + fingerprint + ":" + EXASOL.getDefaultInternalDatabasePort();
        }
        return "127.0.0.1:" + EXASOL.getDefaultInternalDatabasePort();
    }

    @Override
    @AfterEach
    void afterEach() {
        dropAll(this.exaConnection);
        this.exaConnection = null;
        super.afterEach();
    }

    @Override
    protected Map<String, String> getConnectionSpecificVirtualSchemaProperties() {
        return Map.of("IMPORT_FROM_EXA", "true", EXASOL_CONNECTION_PROPERTY, this.exaConnection.getName());
    }

    @Test
    void testPasswordNotVisibleInImportFromExa() throws NoDriverFoundException, SQLException {
        final Table table = this.sourceSchema.createTable("T1", "C1", "VARCHAR(20)").insert("Hello.");
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        final String sql = "SELECT * FROM " + this.virtualSchema.getFullyQualifiedName() + ".\"" + table.getName()
                + "\"";
        assertThat(explainVirtual(sql), //
                table().row( //
                        anything(), //
                        not(anyOf( //
                                containsString(this.user.getName()), //
                                containsString(this.user.getPassword()), //
                                containsString(EXASOL.getUsername()), //
                                containsString(EXASOL.getPassword()) //
                        )), //
                        anything(), //
                        anything() //
                ).matches());
    }

    private ResultSet explainVirtual(final String sql) throws SQLException {
        return query("EXPLAIN VIRTUAL " + sql);
    }

    @Override
    @Test
    void testCharMappingUtf8() {
        final Table table = createSingleColumnTable("CHAR(20) UTF8").insert("Howdy.").insert("Grüzi.");
        assertVirtualTableContents(table, table("VARCHAR").row(pad("Howdy.", 20)).row(pad("Grüzi.", 20)).matches());
    }

    @Override
    @Test
    void testCharMappingAscii() {
        final Table table = createSingleColumnTable("CHAR(20) ASCII").insert("sun").insert("rain");
        assertVirtualTableContents(table, table("VARCHAR").row(pad("sun", 20)).row(pad("rain", 20)).matches());
    }

    @Override
    @Test
    void testCastVarcharToChar() {
        castFrom("VARCHAR(20)").to("CHAR(40)").input("Hello.").accept("VARCHAR").verify(pad("Hello.", 40));
    }

    @Override
    @Test
    void testDefaultHashType() {
        typeAssertionFor("HASHTYPE").withValue("550e8400-e29b-11d4-a716-446655440000")
                .expectDescribeType("HASHTYPE(16 BYTE)") //
                .expectTypeOf("HASHTYPE(16 BYTE)") //
                .expectResultSetType("VARCHAR") //
                .expectValue("550e8400e29b11d4a716446655440000") //
                .runAssert();
    }

    @Override
    @Test
    void testNonDefaultHashType() {
        typeAssertionFor("HASHTYPE(4 BYTE)").withValue("550e8400") //
                .expectDescribeType("HASHTYPE(4 BYTE)") //
                .expectTypeOf("HASHTYPE(4 BYTE)") //
                .expectResultSetType("VARCHAR") //
                .runAssert();
    }

    @Override
    @Test
    void testHashTypeWithBitSize() {
        typeAssertionFor("HASHTYPE(16 BIT)").withValue("550e") //
                .expectDescribeType("HASHTYPE(2 BYTE)") //
                .expectTypeOf("HASHTYPE(2 BYTE)") //
                .expectResultSetType("VARCHAR") //
                .runAssert();
    }

    @Override
    @Test
    void testDefaultGeometry() {
        typeAssertionFor("GEOMETRY").withValue("POINT (2 5)") //
                .expectDescribeType("GEOMETRY(3857)") //
                .expectResultSetType("VARCHAR") //
                .runAssert();
    }

    @Override
    @Test
    void testNonDefaultGeometry() {
        typeAssertionFor("GEOMETRY(4321)").withValue("POINT (2 5)") //
                .expectResultSetType("VARCHAR") //
                .runAssert();
    }

    @Override
    @Test
    void testDefaultIntervalYearToMonth() {
        typeAssertionFor("INTERVAL YEAR TO MONTH").withValue("5-3") //
                .expectTypeOf("INTERVAL YEAR(2) TO MONTH") //
                .expectDescribeType("INTERVAL YEAR(2) TO MONTH") //
                .expectResultSetType("VARCHAR") //
                .expectValue("+05-03") //
                .runAssert();
    }

    @Override
    @Test
    void testNonDefaultIntervalYearToMonth() {
        typeAssertionFor("INTERVAL YEAR(5) TO MONTH").withValue("5-3") //
                .expectResultSetType("VARCHAR") //
                .expectValue("+00005-03") //
                .runAssert();
    }

    @Override
    @Test
    void testDefaultIntervalDayToSecond() {
        typeAssertionFor("INTERVAL DAY TO SECOND").withValue("2 12:50:10.123") //
                .expectTypeOf("INTERVAL DAY(2) TO SECOND(3)") //
                .expectDescribeType("INTERVAL DAY(2) TO SECOND(3)") //
                .expectResultSetType("VARCHAR") //
                .expectValue("+02 12:50:10.123") //
                .runAssert();
    }

    @Override
    @Test
    void testNonDefaultIntervalDayToSecond() {
        typeAssertionFor("INTERVAL DAY(4) TO SECOND(6)").withValue("2 12:50:10.123") //
                .expectResultSetType("VARCHAR") //
                .expectValue("+0002 12:50:10.123000") //
                .runAssert();
    }
}
