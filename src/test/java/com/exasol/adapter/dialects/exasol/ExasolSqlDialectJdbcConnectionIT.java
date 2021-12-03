package com.exasol.adapter.dialects.exasol;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
    @Disabled("SPOT-13565")
    void testCastVarcharAsIntervalYearToMonth() {
        // expected "+00004-06" but was "+04-06"
    }

    @Override
    @Test
    @Disabled("SPOT-13565")
    void testCastVarcharAsIntervalDayToSecond() {
        // expected "+00003 12:50:10.12" but was "+03 12:50:10.120"
    }

    @Override
    @Test
    @Disabled("SPOT-13565")
    void testIntervalYearToMonthMappingCustom() {
        // expected "+005-03" but was "+05-03"
    }

    @Override
    @Test
    @Disabled("SPOT-13565")
    void testIntervalDayToSecondMappingCustom() {
        // expected "+0002 12:50:10.123000" but was "+02 12:50:10.123"
    }

    @Override
    @Test
    @Disabled("SPOT-13565")
    void testInvervalYearToMonthMappingMaxPrecision() {
        // SQLException: ETL-3031: [Column=0 Row=0] [Interval (year to month) conversion failed for '-999999999-11' -
        // the leading precision of the interval is too small]
    }

    @Override
    @Test
    @Disabled("SPOT-13565")
    void testIntervalDayToSecondMappingMaxPrecision() {
        // SQLException: ETL-3032: [Column=0 Row=0] [Interval (day to second) conversion failed for '-999999999
        // 23:59:59.999' - the leading precision of the interval is too small]
    }
}