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
import com.exasol.adapter.dialects.rewriting.SqlGenerationVisitor;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.sql.*;

@ExtendWith(MockitoExtension.class)
class ExasolTypedNullSqlGenerationVisitorTest {
    @Mock
    private ExasolSqlDialect dialect;

    @Test
    void testCastsTopLevelNullLiteralToCorrespondingSelectListType() throws AdapterException {
        final SqlSelectList selectList = SqlSelectList.createRegularSelectList(
                List.of(new SqlLiteralNull(), new SqlLiteralExactnumeric(BigDecimal.ONE)));
        assertThat(visit(List.of(DataType.createDecimal(18, 0), DataType.createDecimal(1, 0)), selectList),
                equalTo("CAST(NULL AS DECIMAL(18, 0)), 1"));
    }

    @Test
    void testLeavesNestedNullLiteralUncast() throws AdapterException {
        final SqlSelectList selectList = SqlSelectList
                .createRegularSelectList(List.of(new SqlPredicateIsNull(new SqlLiteralNull())));
        assertThat(visit(List.of(DataType.createBool()), selectList), equalTo("(NULL) IS NULL"));
    }

    @Test
    void testAnyValueSelectList() throws AdapterException {
        // "any value" in this case means just checking whether there is a result row at all
        final SqlSelectList selectList = SqlSelectList.createAnyValueSelectList();
        assertThat(visit(List.of(DataType.createDecimal(18, 0), DataType.createDecimal(1, 0)), selectList), equalTo("true"));
    }

    @Test
    void testLeavesTopLevelNullLiteralUncastWhenNoCorrespondingSelectListTypeExists() throws AdapterException {
        final SqlSelectList selectList = SqlSelectList.createRegularSelectList(List.of(new SqlLiteralNull()));
        assertThat(visit(List.of(), selectList), equalTo("NULL"));
    }

    private String visit(final List<DataType> selectListDataTypes, final SqlSelectList selectList) throws AdapterException {
        final SqlGenerationVisitor visitor = new ExasolTypedNullSqlGenerationVisitor(this.dialect, null, selectListDataTypes);
        return visitor.visit(selectList);
    }
}
