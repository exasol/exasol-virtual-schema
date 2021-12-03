package com.exasol.adapter.dialects.exasol;

import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.exasol.dbbuilder.dialects.Table;
import com.exasol.dbbuilder.dialects.exasol.VirtualSchema;

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
 * <li>{@code INTERVAL} types precision is lost</li>
 * <ul>
 */
class ExasolSqlDialectJdbcConnectionIT extends AbstractExasolSqlDialectIT {
    @Override
    protected Map<String, String> getConnectionSpecificVirtualSchemaProperties() {
        return Collections.emptyMap();
    }

    @Override
    @Test
    void testCastVarcharAsIntervalYearToMonth() {
        castFrom("VARCHAR(30)").to("INTERVAL YEAR (5) TO MONTH").input("+00004-06").accept("INTERVAL YEAR TO MONTH")
                .verify("+04-06");
    }

    @Override
    @Test
    void testCastVarcharAsIntervalDayToSecond() {
        castFrom("VARCHAR(30)").to("INTERVAL DAY (5) TO SECOND (2)").input("+00003 12:50:10.12")
                .accept("INTERVAL DAY TO SECOND").verify("+03 12:50:10.120");
    }

    @Override
    @Test
    void testIntervalYearToMonthMappingCustom() {
        final Table table = createSingleColumnTable("INTERVAL YEAR (3) TO MONTH").insert("5-3");
        assertVirtualTableContents(table, table("INTERVAL YEAR TO MONTH").row("+05-03").matches());
    }

    @Override
    @Test
    void testIntervalDayToSecondMappingCustom() {
        final Table table = createSingleColumnTable("INTERVAL DAY (4) TO SECOND (6)").insert("2 12:50:10.123");
        assertVirtualTableContents(table, table("INTERVAL DAY TO SECOND").row("+02 12:50:10.123").matches());
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

        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        try {
            final SQLException thrown = assertThrows(SQLException.class,
                    () -> selectAllFromCorrespondingVirtualTable(virtualSchema, table));
            assertThat(thrown.getMessage(), containsString(
                    "ETL-3031: [Column=0 Row=0] [Interval (year to month) conversion failed for '-999999999-11' - the leading precision of the interval is too small]"));
        } finally {
            virtualSchema.drop();
        }
    }

    @Override
    @Test
    void testIntervalDayToSecondMappingMaxPrecision() {
        final Table table = createSingleColumnTable("INTERVAL DAY (9) TO SECOND") //
                .insert("-999999999 23:59:59.999");

        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        try {
            final SQLException thrown = assertThrows(SQLException.class,
                    () -> selectAllFromCorrespondingVirtualTable(virtualSchema, table));
            assertThat(thrown.getMessage(), containsString(
                    "ETL-3032: [Column=0 Row=0] [Interval (day to second) conversion failed for '-999999999 23:59:59.999' - the leading precision of the interval is too small]"));
        } finally {
            virtualSchema.drop();
        }
    }
}