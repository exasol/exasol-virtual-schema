package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.exasol.adapter.AdapterException;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.dummy.DummySqlDialect;
import com.exasol.adapter.dialects.rewriting.SqlGenerationContext;
import com.exasol.adapter.metadata.ColumnMetadata;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.metadata.DataType.ExaCharset;
import com.exasol.adapter.sql.SqlColumn;

public class ExasolLocalSqlGenerationVisitorTest {
    private static ExasolLocalSqlGenerationVisitor exasolLocalSqlGenerationVisitor;

    @BeforeAll
    public static void beforeAll() {
        final Map<String, String> rawProperties = new HashMap<>();
        final AdapterProperties adapterProperties = new AdapterProperties(rawProperties);
        final SqlDialect sqlDialect = new DummySqlDialect(null, adapterProperties, null);
        final SqlGenerationContext context = new SqlGenerationContext("", "TEXT_SCHEMA_NAME", false);
        exasolLocalSqlGenerationVisitor = new ExasolLocalSqlGenerationVisitor(sqlDialect, context);
    }

    @Test
    void testVisitSqlLiteralVarchar() throws AdapterException {

        final SqlColumn argument = new SqlColumn(1, //
                ColumnMetadata.builder().name("a").type(DataType.createVarChar(20, ExaCharset.ASCII)).build(),
                "table_name"); //

        assertThat(exasolLocalSqlGenerationVisitor.visit(argument),
                equalTo("CAST(\"table_name\".\"a\" AS VARCHAR(20) UTF8)"));

    }

    @Test
    void testVisitSqlLiteralChar() throws AdapterException {

        final SqlColumn argument = new SqlColumn(1, //
                ColumnMetadata.builder().name("a").type(DataType.createChar(20, ExaCharset.ASCII)).build(),
                "table_name"); //

        assertThat(exasolLocalSqlGenerationVisitor.visit(argument),
                equalTo("CAST(\"table_name\".\"a\" AS CHAR(20) UTF8)"));

    }

    @Test
    void testVisitSqlLiteralDouble() throws AdapterException {

        final SqlColumn argument = new SqlColumn(1, //
                ColumnMetadata.builder().name("a").type(DataType.createDouble()).build(), "table_name"); //

        assertThat(exasolLocalSqlGenerationVisitor.visit(argument), equalTo("\"table_name\".\"a\""));

    }
}
