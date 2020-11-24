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
}