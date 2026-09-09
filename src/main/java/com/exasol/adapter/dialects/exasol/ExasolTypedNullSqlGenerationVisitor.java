package com.exasol.adapter.dialects.exasol;

import java.util.List;

import com.exasol.adapter.AdapterException;
import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.SqlGenerationContext;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.sql.SqlLiteralNull;
import com.exasol.adapter.sql.SqlNode;
import com.exasol.adapter.sql.SqlSelectList;

/**
 * Generates remote Exasol SQL with type information for top-level {@code NULL} literals.
 */
class ExasolTypedNullSqlGenerationVisitor extends ExasolSqlGenerationVisitor {
    private final List<DataType> selectListDataTypes;

    ExasolTypedNullSqlGenerationVisitor(final SqlDialect dialect, final SqlGenerationContext context,
            final List<DataType> selectListDataTypes) {
        super(dialect, context);
        this.selectListDataTypes = selectListDataTypes;
    }

    @Override
    protected String createExplicitColumnsSelectList(final SqlSelectList selectList) throws AdapterException {
        final List<SqlNode> expressions = selectList.getExpressions();
        final StringBuilder sql = new StringBuilder();
        for (int index = 0; index < expressions.size(); index++) {
            if (index > 0) {
                sql.append(", ");
            }
            final SqlNode expression = expressions.get(index);
            if (expression instanceof SqlLiteralNull && index < this.selectListDataTypes.size()) {
                sql.append("CAST(NULL AS ").append(this.selectListDataTypes.get(index)).append(")");
            } else {
                sql.append(expression.accept(this));
            }
        }
        return sql.toString();
    }
}
