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

import com.exasol.adapter.dialects.exasol.fingerprint.FingerprintExtractor;
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
 * <li>{@code INTERVAL} types are converted to {@code VARCHAR}</li>
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
            final String fingerprint = FingerprintExtractor.extractFingerprint(EXASOL.getJdbcUrl()).orElseThrow();
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

    @Test
    void testDefaultHashType() {
        typeAssertionFor("HASHTYPE").withValue("550e8400-e29b-11d4-a716-446655440000")
                .expectDescribeType("CHAR(32) ASCII").expectTypeOf("HASHTYPE(16 BYTE)").expectResultSetType("VARCHAR")
                .expectValue("550e8400e29b11d4a716446655440000").runAssert();
    }
}
