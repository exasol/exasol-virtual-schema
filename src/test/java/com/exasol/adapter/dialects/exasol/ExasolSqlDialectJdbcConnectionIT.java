package com.exasol.adapter.dialects.exasol;

import static com.exasol.matcher.ResultSetStructureMatcher.table;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.exasol.dbbuilder.dialects.Table;

/**
 * This class exercises a set of tests defined in the base class on a local Exasol, using {@code IMPORT} via a JDBC
 * connection.
 * <p>
 * In this case the Adapter uses the same JDBC connection to attach to the database that the ExaLoader needs for running
 * the {@code IMPORT}.
 * </p>
 * <p>
 * These tests take the following specialties of a local connection into account:
 * </p>
 * <ul>
 * <li>{@code INTERVAL} types are converted to {@code VARCHAR}</li>
 * <ul>
 */
class ExasolSqlDialectJdbcConnectionIT extends AbstractExasolSqlDialectIT {
    @Override
    protected Map<String, String> getConnectionSpecificVirtualSchemaProperties() {
        return Collections.emptyMap();
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
}
