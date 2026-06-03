package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.exasol.adapter.AdapterException;
import com.exasol.adapter.dialects.JDBCAdapterContext;
import com.exasol.adapter.dialects.rewriting.SqlGenerationContext;
import com.exasol.adapter.metadata.ColumnMetadata;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.metadata.DataType.ExaCharset;
import com.exasol.adapter.sql.SqlColumn;

class ExasolLocalSqlGenerationVisitorTest {
    private ExasolSqlDialect dialect;
    private ExasolLocalSqlGenerationVisitor exasolLocalSqlGenerationVisitor;

    @BeforeEach
    void beforeEach() {
        this.dialect = spy(new ExasolSqlDialect(JDBCAdapterContext.builder().build()));
        doReturn("\"table_name\"").when(this.dialect).applyQuote("table_name");
        doReturn("\"a\"").when(this.dialect).applyQuote("a");
        final SqlGenerationContext context = new SqlGenerationContext("", "TEXT_SCHEMA_NAME", false);
        this.exasolLocalSqlGenerationVisitor = new ExasolLocalSqlGenerationVisitor(this.dialect, context);
    }

    @Test
    void testVisitSqlLiteralVarchar() throws AdapterException {

        final SqlColumn argument = new SqlColumn(1, //
                ColumnMetadata.builder().name("a").type(DataType.createVarChar(20, ExaCharset.ASCII)).build(),
                "table_name"); //

        assertAll(//
                () -> assertThat(this.exasolLocalSqlGenerationVisitor.visit(argument),
                        equalTo("CAST(\"table_name\".\"a\" AS VARCHAR(20) UTF8)")),
                () -> verify(this.dialect).applyQuote("table_name"),
                () -> verify(this.dialect).applyQuote("a"));

    }

    @Test
    void testVisitSqlLiteralChar() throws AdapterException {

        final SqlColumn argument = new SqlColumn(1, //
                ColumnMetadata.builder().name("a").type(DataType.createChar(20, ExaCharset.ASCII)).build(),
                "table_name"); //

        assertAll(//
                () -> assertThat(this.exasolLocalSqlGenerationVisitor.visit(argument),
                        equalTo("CAST(\"table_name\".\"a\" AS CHAR(20) UTF8)")),
                () -> verify(this.dialect).applyQuote("table_name"),
                () -> verify(this.dialect).applyQuote("a"));

    }

    @Test
    void testVisitSqlLiteralDouble() throws AdapterException {

        final SqlColumn argument = new SqlColumn(1, //
                ColumnMetadata.builder().name("a").type(DataType.createDouble()).build(), "table_name"); //

        assertAll(//
                () -> assertThat(this.exasolLocalSqlGenerationVisitor.visit(argument), equalTo("\"table_name\".\"a\"")),
                () -> verify(this.dialect).applyQuote("table_name"),
                () -> verify(this.dialect).applyQuote("a"));

    }
}
