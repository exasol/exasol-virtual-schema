package com.exasol.adapter.dialects.exasol;

import com.exasol.adapter.AdapterException;
import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.SqlGenerationContext;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.sql.SqlColumn;

public class ExasolLocalSqlGenerationVisitor extends ExasolSqlGenerationVisitor{
    /**
     * Creates a new instance of the {@link ExasolSqlGenerationVisitor}.
     *
     * @param dialect {@link ExasolSqlDialect} dialect
     * @param context SQL generation context
     */
    ExasolLocalSqlGenerationVisitor(SqlDialect dialect, SqlGenerationContext context) {
        super(dialect, context);
    }
    @Override
    public String visit(final SqlColumn column) throws AdapterException {
        ExasolSqlDialect exasolSqlDialect = new ExasolSqlDialect(null,null);
        String tablePrefix = "";
        if (column.hasTableAlias()) {
            tablePrefix = exasolSqlDialect.applyQuote(column.getTableAlias())
                    + exasolSqlDialect.getTableCatalogAndSchemaSeparator();
        } else if ((column.getTableName() != null) && !column.getTableName().isEmpty()) {
            tablePrefix = exasolSqlDialect.applyQuote(column.getTableName())
                    + exasolSqlDialect.getTableCatalogAndSchemaSeparator();
        }
        //PROVIDE A CAST TO CORRESPONDING DATATYPE WITH THE SAME LENGTH BUT WITH UTF8 AS CHARSET
        if (column.getMetadata().getType().getExaDataType() == DataType.ExaDataType.CHAR || //
                column.getMetadata().getType().getExaDataType() == DataType.ExaDataType.VARCHAR) {
            int size = column.getMetadata().getType().getSize();
            String dataTypeStr = column.getMetadata().getType().getExaDataType() == DataType.ExaDataType.CHAR ? "CHAR":"VARCHAR";
            return "CAST(" + tablePrefix + exasolSqlDialect.applyQuote(column.getName())+" AS "+ dataTypeStr+"("+size+") UTF8)";
        } else {
            return tablePrefix + exasolSqlDialect.applyQuote(column.getName());
        }
    }
}
