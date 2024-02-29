package com.exasol.adapter.dialects.exasol;

import static com.exasol.matcher.ResultSetStructureMatcher.table;

import org.junit.jupiter.api.Test;

import com.exasol.dbbuilder.dialects.Table;

/**
 * Base class for tests of a remote Exasol virtual schema, i.e. JDBC and EXAConnection.
 */
abstract class AbstractRemoteExasolVirtualSchemaConnectionIT extends AbstractExasolSqlDialectIT {
    @Test
    void testIntervalYearToMonthMapping() {
        final Table table = createSingleColumnTable("INTERVAL YEAR (9) TO MONTH")//
                .insert("-999999999-11") //
                .insert("-1-1") //
                .insert("0-0") //
                .insert("1-1") //
                .insert("999999999-11");
        // why is precision 9 missing in expected data type?
        assertVirtualTableContents(table, table(expectDataType("INTERVAL YEAR TO MONTH")) //
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
        // why is precision 9 missing in expected data type?
        assertVirtualTableContents(table, table(expectDataType("INTERVAL DAY TO SECOND")) //
                .row("-999999999 23:59:59.999") //
                .row("-000000001 12:34:56.789") //
                .row("+000000000 00:00:00.000") //
                .row("+000000001 12:34:56.789") //
                .row("+999999999 23:59:59.999") //
                .matches());
    }

    @Test
    void testCastVarcharAsIntervalDayToSecond() {
        // why is precision 5 and 2 missing in expected data type?
        castFrom("VARCHAR(30)").to("INTERVAL DAY (5) TO SECOND (2)").input("+00003 12:50:10.12")
                .accept(expectDataType("INTERVAL DAY TO SECOND")).verify("+00003 12:50:10.12");
    }

    @Test
    void testCastVarcharAsIntervalYearToMonth() {
        // why is precision 5 missing in expectedDataType?
        castFrom("VARCHAR(30)").to("INTERVAL YEAR (5) TO MONTH").input("+00004-06")
                .accept(expectDataType("INTERVAL YEAR TO MONTH")).verify("+00004-06");
    }
}
