package com.exasol.adapter.dialects.exasol;

import static com.exasol.matcher.ResultSetStructureMatcher.table;

import org.junit.jupiter.api.Test;

import com.exasol.dbbuilder.dialects.Table;

/**
 * This class contains the common parts of the variants of the Exasol Virtual Schema that connect to a remote Exasol
 * instance or cluster.
 */
abstract class AbstractRemoteExasolVirtualSchemaConnectionIT extends AbstractExasolSqlDialectIT {
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
    void testIntervalDayToSecondMapping() {
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
}