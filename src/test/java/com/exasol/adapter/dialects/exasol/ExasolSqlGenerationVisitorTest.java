package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exasol.adapter.sql.SqlLiteralDouble;
import com.exasol.adapter.sql.SqlLiteralTimestampUtc;

@ExtendWith(MockitoExtension.class)
class ExasolSqlGenerationVisitorTest {
    @Mock
    private ExasolSqlDialect exasolSqlDialect;

    @Test
    void testVisitSqlLiteralTimestampUtcThrowsException() {
        when(this.exasolSqlDialect.isTimestampWithLocalTimeZoneEnabled()).thenReturn(false);
        final ExasolSqlGenerationVisitor exasolSqlGenerationVisitor = new ExasolSqlGenerationVisitor(
                this.exasolSqlDialect, null);
        final SqlLiteralTimestampUtc sqlLiteralTimestampUtc = new SqlLiteralTimestampUtc("2015-12-01 12:01:01.1234");
        assertThrows(UnsupportedOperationException.class,
                () -> exasolSqlGenerationVisitor.visit(sqlLiteralTimestampUtc));
    }

    @Test
    void testVisitSqlLiteralTimestampUtc() {
        when(this.exasolSqlDialect.isTimestampWithLocalTimeZoneEnabled()).thenReturn(true);
        final String value = "2015-12-01 12:01:01.1234";
        when(this.exasolSqlDialect.getStringLiteral(value)).thenReturn("'" + value + "'");
        final ExasolSqlGenerationVisitor exasolSqlGenerationVisitor = new ExasolSqlGenerationVisitor(
                this.exasolSqlDialect, null);
        final SqlLiteralTimestampUtc sqlLiteralTimestampUtc = new SqlLiteralTimestampUtc(value);
        assertThat(exasolSqlGenerationVisitor.visit(sqlLiteralTimestampUtc),
                equalTo("TIMESTAMP '2015-12-01 12:01:01.1234'"));
    }

    @Test
    void testVisitSqlLiteralDouble() {
        final ExasolSqlGenerationVisitor exasolSqlGenerationVisitor = new ExasolSqlGenerationVisitor(
                this.exasolSqlDialect, null);
        assertThat(exasolSqlGenerationVisitor.visit(new SqlLiteralDouble(1.23)), equalTo("CAST(1.23 AS DOUBLE)"));
    }
}