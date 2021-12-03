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
class ExasolSqlDialectExaConnectionIT extends AbstractExasolSqlDialectIT {
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

    @Override
    @Test
    void testHashtypeMapping() throws SQLException {
        final String value = "550e8400-e29b-11d4-a716-446655440000";
        final Table table = createSingleColumnTable("HASHTYPE").insert(value);
        assertVirtualTableContents(table, table("VARCHAR").row(value.replace("-", "")).matches());
    }

    @Override
    @Test
    void testGeometryMapping() {
        final Table table = createSingleColumnTable("GEOMETRY").insert("POINT (2 3)");
        assertVirtualTableContents(table, table("VARCHAR").row("POINT (2 3)").matches());
    }

    @Override
    @Test
    void testCastVarcharAsGeometry() {
        castFrom("VARCHAR(20)").to("GEOMETRY(5)").input("POINT(2 5)").accept("VARCHAR").verify("POINT (2 5)");
    }

    @Override
    @Test
    void testInvervalYearToMonthMappingMaxPrecision() {
        final Table table = createSingleColumnTable("INTERVAL YEAR (9) TO MONTH")//
                .insert("-999999999-11") //
                .insert("-1-1") //
                .insert("0-0") //
                .insert("1-1") //
                .insert("999999999-11");
        assertVirtualTableContents(table, table("VARCHAR") //
                .row("-999999999-11") //
                .row("-000000001-01") //
                .row("+000000000-00") //
                .row("+000000001-01") //
                .row("+999999999-11") //
                .matches());
    }

    @Override
    @Test
    void testIntervalDayToSecondMappingMaxPrecision() {
        final Table table = createSingleColumnTable("INTERVAL DAY (9) TO SECOND") //
                .insert("-999999999 23:59:59.999") //
                .insert("-1 12:34:56.789") //
                .insert("0 00:00:00.000") //
                .insert("1 12:34:56.789") //
                .insert("999999999 23:59:59.999");
        assertVirtualTableContents(table, table("VARCHAR") //
                .row("-999999999 23:59:59.999") //
                .row("-000000001 12:34:56.789") //
                .row("+000000000 00:00:00.000") //
                .row("+000000001 12:34:56.789") //
                .row("+999999999 23:59:59.999") //
                .matches());
    }

    @Override
    @Test
    void testIntervalDayToSecondMappingDefault() {
        final Table table = createSingleColumnTable("INTERVAL DAY TO SECOND").insert("2 12:50:10.123");
        assertVirtualTableContents(table, table("VARCHAR").row("+02 12:50:10.123").matches());
    }

    @Override
    @Test
    void testIntervalDayToSecondMappingCustom() {
        final Table table = createSingleColumnTable("INTERVAL DAY (4) TO SECOND (6)").insert("2 12:50:10.123");
        assertVirtualTableContents(table, table("VARCHAR").row("+0002 12:50:10.123000").matches());
    }

    @Override
    @Test
    void testIntervalYearToMonthMappingDefault() {
        final Table table = createSingleColumnTable("INTERVAL YEAR TO MONTH").insert("5-3");
        assertVirtualTableContents(table, table("VARCHAR").row("+05-03").matches());
    }

    @Override
    @Test
    void testIntervalYearToMonthMappingCustom() {
        final Table table = createSingleColumnTable("INTERVAL YEAR (3) TO MONTH").insert("5-3");
        assertVirtualTableContents(table, table("VARCHAR").row("+005-03").matches());
    }

    @Override
    @Test
    void testCastVarcharAsIntervalDayToSecond() {
        castFrom("VARCHAR(30)").to("INTERVAL DAY (5) TO SECOND (2)").input("+00003 12:50:10.12").accept("VARCHAR")
                .verify("+00003 12:50:10.12");
    }

    @Override
    @Test
    void testCastVarcharAsIntervalYearToMonth() {
        castFrom("VARCHAR(30)").to("INTERVAL YEAR (5) TO MONTH").input("+00004-06").accept("VARCHAR")
                .verify("+00004-06");
    }
}
