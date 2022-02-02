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
 * These tests take the following specialties of a JDBC connection into account:
 * </p>
 * <ul>
 * <li>{@code INTERVAL} types are reported with JDBC type name {@code VARCHAR} in ResultSets</li>
 * <li>{@code GEOMETRY} types are reported with JDBC type name {@code VARCHAR} in ResultSets</li>
 * <ul>
 */
class ExasolSqlDialectJdbcConnectionIT extends AbstractRemoteExasolVirtualSchemaConnectionIT {
    @Override
    protected Map<String, String> getConnectionSpecificVirtualSchemaProperties() {
        return Collections.emptyMap();
    }

    @Override
    @Test
    void testCharMappingUtf8() {
        final Table table = createSingleColumnTable("CHAR(20) UTF8").insert("Howdy.").insert("Grüzi.");
        assertVirtualTableContents(table, table("CHAR").row(pad("Howdy.", 20)).row(pad("Grüzi.", 20)).matches());
    }

    @Override
    @Test
    void testCharMappingAscii() {
        final Table table = createSingleColumnTable("CHAR(20) ASCII").insert("sun").insert("rain");
        assertVirtualTableContents(table, table("CHAR").row(pad("sun", 20)).row(pad("rain", 20)).matches());
    }

    @Override
    @Test
    void testCastVarcharToChar() {
        castFrom("VARCHAR(20)").to("CHAR(40)").input("Hello.").verify(pad("Hello.", 40));
    }

    @Override
    @Test
    void testDefaultGeometry() {
        typeAssertionFor("GEOMETRY").withValue("POINT (2 5)") //
                .expectDescribeType("GEOMETRY(3857)") //
                .expectResultSetType("VARCHAR") //
                .runAssert();
    }

    @Override
    @Test
    void testNonDefaultGeometry() {
        typeAssertionFor("GEOMETRY(4321)").withValue("POINT (2 5)") //
                .expectResultSetType("VARCHAR") //
                .runAssert();
    }

    @Override
    @Test
    void testDefaultIntervalYearToMonth() {
        typeAssertionFor("INTERVAL YEAR TO MONTH").withValue("5-3") //
                .expectTypeOf("INTERVAL YEAR(2) TO MONTH") //
                .expectDescribeType("INTERVAL YEAR(2) TO MONTH") //
                .expectResultSetType("VARCHAR") //
                .expectValue("+05-03") //
                .runAssert();
    }

    @Override
    @Test
    void testNonDefaultIntervalYearToMonth() {
        typeAssertionFor("INTERVAL YEAR(5) TO MONTH").withValue("5-3") //
                .expectResultSetType("VARCHAR") //
                .expectValue("+00005-03") //
                .runAssert();
    }

    @Override
    @Test
    void testDefaultIntervalDayToSecond() {
        typeAssertionFor("INTERVAL DAY TO SECOND").withValue("2 12:50:10.123") //
                .expectTypeOf("INTERVAL DAY(2) TO SECOND(3)") //
                .expectDescribeType("INTERVAL DAY(2) TO SECOND(3)") //
                .expectResultSetType("VARCHAR") //
                .expectValue("+02 12:50:10.123") //
                .runAssert();
    }

    @Override
    @Test
    void testNonDefaultIntervalDayToSecond() {
        typeAssertionFor("INTERVAL DAY(4) TO SECOND(6)").withValue("2 12:50:10.123") //
                .expectResultSetType("VARCHAR") //
                .expectValue("+0002 12:50:10.123000") //
                .runAssert();
    }
}