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
 * This class exercises a set of tests defined in the base class for on a local Exasol, using {@code IMPORT} via a EXA
 * connection.
 * <p>
 * In this case the Adapter uses a different (JDBC) connection to attach to the database than the ExaLoader which runs
 * this {@code IMPORT}.
 * </p>
 * <p>
 * This tests takes the following specialties of a local connection into account:
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
        this.exaConnection = objectFactory.createConnectionDefinition(EXA_CONNECTION_NAME,
                "127.0.0.1:" + CONTAINER.getDefaultInternalDatabasePort(), this.user.getName(),
                this.user.getPassword());
    }

    @Override
    @AfterEach
    void afterEach() {
        dropAll(this.exaConnection);
        this.exaConnection = null;
        super.afterEach();
    }

    @Override
    protected Map<String, String> getConnectionSepcificVirtualSchemaProperties() {
        return Map.of("IMPORT_FROM_EXA", "true", EXASOL_CONNECTION_PROPERTY, this.exaConnection.getName());
    }

    // In case of an EXA connection, the result type is `VARCHAR` where `CHAR` normally would be expected.
    @Override
    @Test
    void testCharMappingUtf8() {
        final Table table = createSingleColumnTable("CHAR(20) UTF8").insert("Howdy.").insert("Grüzi.");
        assertVirtualTableContents(table, table("VARCHAR").row(pad("Howdy.", 20)).row(pad("Grüzi.", 20)).matches());
    }

    // In case of an EXA connection, the result type is `VARCHAR` where `CHAR` normally would be expected.
    @Override
    @Test
    void testCharMappingAscii() {
        final Table table = createSingleColumnTable("CHAR(20) ASCII").insert("sun").insert("rain");
        assertVirtualTableContents(table, table("VARCHAR").row(pad("sun", 20)).row(pad("rain", 20)).matches());
    }

    // In case of an EXA connection, the result type is `VARCHAR` where `CHAR` normally would be expected.
    @Override
    @Test
    void testCastVarcharToChar() {
        assertCast("VARCHAR(20)", "CHAR(40)", "VARCHAR", "Hello.", pad("Hello.", 40));
    }

    @Test
    void testInvervalYearToMonthMapping() {
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

    @Test
    void testIntervalDayToSecontMapping() {
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

    @Test
    void testCastVarcharAsIntervalDayToSecond() {
        assertCast("VARCHAR(30)", "INTERVAL DAY (5) TO SECOND (2)", "VARCHAR", "+00003 12:50:10.12",
                "+00003 12:50:10.12");
    }

    @Test
    void testCastVarcharAsIntervalYearToMonth() {
        assertCast("VARCHAR(30)", "INTERVAL YEAR (5) TO MONTH", "VARCHAR", "+00004-06", "+00004-06");
    }

    @Test
    void testPasswordNotVisibleInImportFromExa() throws NoDriverFoundException, SQLException {
        final Table table = this.sourceSchema.createTable("T1", "C1", "VARCHAR(20)").insert("Hello.");
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        final String sql = "SELECT * FROM " + this.virtualSchema.getFullyQualifiedName() + "." + table.getName();
        assertThat(explainVirtual(sql), //
                table().row( //
                        anything(), //
                        not(anyOf( //
                                containsString(this.user.getName()), //
                                containsString(this.user.getPassword()), //
                                containsString(CONTAINER.getUsername()), //
                                containsString(CONTAINER.getPassword()) //
                        )), //
                        anything(), //
                        anything() //
                ).matches());
    }

    private ResultSet explainVirtual(final String sql) throws SQLException {
        return query("EXPLAIN VIRTUAL " + sql);
    }
}