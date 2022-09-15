package com.exasol.adapter.dialects.exasol;

import static com.exasol.matcher.ResultSetStructureMatcher.table;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.exasol.dbbuilder.dialects.Table;

/**
 * This class exercises a set of tests defined in the base class on a local Exasol, using {@code SELECT} instead of
 * {@code IMPORT}
 * <p>
 * These tests take the following specialties of a local connection into account:
 * </p>
 * <ul>
 * <li>{@code INTERVAL} types are present in the result set without modification</li>
 * <li>{@code HASHTYPE} types are present in the result set without modification</li>
 * <li>{@code GEOMETRY} types are present in the result set without modification</li>
 * <ul>
 */
class ExasolSqlDialectLocalConnectionIT extends AbstractExasolSqlDialectIT {

    @Override
    protected Map<String, String> getConnectionSpecificVirtualSchemaProperties() {
        return Map.of("IS_LOCAL", "true");
    }

    @Override
    protected Set<String> expectVarcharFor() {
        return isMajorVersionOrHigher(8) ? Collections.emptySet() : Set.of("INTERVAL", "HASHTYPE");
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
        castFrom("VARCHAR(30)").to("INTERVAL DAY (5) TO SECOND (2)").input("+00003 12:50:10.12")
                .verify("+00003 12:50:10.12");
    }

    @Test
    void testCastVarcharAsIntervalYearToMonth() {
        castFrom("VARCHAR(30)").to("INTERVAL YEAR (5) TO MONTH").input("+00004-06").verify("+00004-06");
    }
}
