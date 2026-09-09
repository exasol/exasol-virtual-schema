package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exasol.adapter.AdapterException;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.sql.*;

@ExtendWith(MockitoExtension.class)
class ExasolTypedNullSqlGenerationVisitorTest {
    @Mock
    private ExasolSqlDialect dialect;

    @Test
    void testCastsTopLevelNullLiteralToCorrespondingSelectListType() throws AdapterException {
        final ExasolTypedNullSqlGenerationVisitor visitor = new ExasolTypedNullSqlGenerationVisitor(this.dialect, null,
                List.of(DataType.createDecimal(18, 0), DataType.createDecimal(1, 0)));
        final SqlSelectList selectList = SqlSelectList.createRegularSelectList(
                List.of(new SqlLiteralNull(), new SqlLiteralExactnumeric(BigDecimal.ONE)));

        assertThat(visitor.visit(selectList), equalTo("CAST(NULL AS DECIMAL(18, 0)), 1"));
    }

    @Test
    void testAnyValueSelectList() throws AdapterException {
        final ExasolTypedNullSqlGenerationVisitor visitor = new ExasolTypedNullSqlGenerationVisitor(this.dialect, null,
                List.of(DataType.createDecimal(18, 0), DataType.createDecimal(1, 0)));
        final SqlSelectList selectList = SqlSelectList.createAnyValueSelectList();

        assertThat(visitor.visit(selectList), equalTo("true"));
    }

    @Test
    void testLeavesTopLevelNullLiteralUncastWhenNoCorrespondingSelectListTypeExists() throws AdapterException {
        final ExasolTypedNullSqlGenerationVisitor visitor = new ExasolTypedNullSqlGenerationVisitor(this.dialect, null,
                List.of());
        final SqlSelectList selectList = SqlSelectList.createRegularSelectList(List.of(new SqlLiteralNull()));

        assertThat(visitor.visit(selectList), equalTo("NULL"));
    }
}
