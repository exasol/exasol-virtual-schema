package com.exasol.adapter.dialects.exasol;

import static com.exasol.matcher.ResultSetStructureMatcher.table;

import java.util.Map;

import org.junit.jupiter.api.*;

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
    private static ConnectionDefinition exaConnection;

    // We need this singleton trick here since the connection and therefore object factory from the abstract base
    // class is not yet initialized in beforeEach().
    // This is due to the calling order of static methods and initializers in JUnit.
    @Override
    @BeforeEach
    void beforeEach() {
        if (exaConnection == null) {
            exaConnection = objectFactory.createConnectionDefinition("EXA_CONNECTION",
                    "127.0.0.1:" + CONTAINER.getDefaultInternalDatabasePort());
        }
        super.beforeEach();
    }

    @AfterAll
    static void afterAll() {
        dropAll(exaConnection);
        exaConnection = null;
    }

    @Override
    protected Map<String, String> getConnectionSepcificVirtualSchemaProperties() {
        return Map.of("IMPORT_FROM_EXA", "true", "EXA_CONNECTION", exaConnection.getName());
    }

    @Test
    void testInvervalYearToMonthMapping() {
        final Table table = createSingleColumnTable("INTERVAL YEAR (9) TO MONTH")//
                .insert("-999999999-11") //
                .insert("-1-1") //
                .insert("0-0") //
                .insert("1-1") //
                .insert("999999999-11");
        assertVirtualTableContents(table, table("INTERVAL YEAR TO MONTH") //
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
        assertVirtualTableContents(table, table("INTERVAL DAY TO SECOND") //
                .row("-999999999 23:59:59.999") //
                .row("-000000001 12:34:56.789") //
                .row("+000000000 00:00:00.000") //
                .row("+000000001 12:34:56.789") //
                .row("+999999999 23:59:59.999") //
                .matches());
    }

    @Test
    void testCastVarcharAsIntervalDayToSecond() {
        assertCast("VARCHAR(30)", "INTERVAL DAY (5) TO SECOND (2)", "+00003 12:50:10.12", "+00003 12:50:10.12");
    }

    @Test
    void testCastVarcharAsIntervalYearToMonth() {
        assertCast("VARCHAR(30)", "INTERVAL YEAR (5) TO MONTH", "+00004-06", "+00004-06");
    }
}