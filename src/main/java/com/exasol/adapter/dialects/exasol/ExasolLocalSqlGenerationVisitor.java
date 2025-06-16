package com.exasol.adapter.dialects.exasol;

import com.exasol.adapter.AdapterException;
import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.SqlGenerationContext;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.metadata.DataType.ExaDataType;
import com.exasol.adapter.sql.SqlColumn;

/**
 * This class generates SQL queries for the {@link ExasolSqlDialect}. It is a 'specialisation' for LOCAL mode, based on
 * {@link ExasolSqlGenerationVisitor}. It adds a cast to UTF8 for char and varchar datatype imports.
 */
public class ExasolLocalSqlGenerationVisitor extends ExasolSqlGenerationVisitor {
    /**
     * Creates a new instance of the {@link ExasolLocalSqlGenerationVisitor}.
     *
     * @param dialect {@link ExasolSqlDialect} dialect
     * @param context SQL generation context
     */
    ExasolLocalSqlGenerationVisitor(final SqlDialect dialect, final SqlGenerationContext context) {
        super(dialect, context);
    }

    @Override
    public String visit(final SqlColumn column) throws AdapterException {
        final ExasolSqlDialect exasolSqlDialect = new ExasolSqlDialect(null, null, null);
        final String tablePrefix = getTablePrefix(column, exasolSqlDialect);
        final ExaDataType exaDataType = column.getMetadata().getType().getExaDataType();
        if (exaDataType == DataType.ExaDataType.CHAR || //
                exaDataType == DataType.ExaDataType.VARCHAR) {
            // Provide a cast to corresponding datatype with the same length but with utf8 as charset.
            return addCastAndReturnColumnName(exaDataType, column, exasolSqlDialect, tablePrefix);
        } else {
            return tablePrefix + exasolSqlDialect.applyQuote(column.getName());
        }
    }

    private String addCastAndReturnColumnName(final ExaDataType exaDataType, final SqlColumn column,
            final ExasolSqlDialect exasolSqlDialect, final String tablePrefix) {
        final int size = column.getMetadata().getType().getSize();

        final String dataTypeStr = exaDataType == DataType.ExaDataType.CHAR ? "CHAR" : "VARCHAR";
        return "CAST(" + tablePrefix + exasolSqlDialect.applyQuote(column.getName()) + " AS " + dataTypeStr + "(" + size
                + ") UTF8)";
    }

    private String getTablePrefix(final SqlColumn column, final ExasolSqlDialect exasolSqlDialect) {
        String tablePrefix = "";
        if (column.hasTableAlias()) {
            tablePrefix = exasolSqlDialect.applyQuote(column.getTableAlias())
                    + exasolSqlDialect.getTableCatalogAndSchemaSeparator();
        } else if ((column.getTableName() != null) && !column.getTableName().isEmpty()) {
            tablePrefix = exasolSqlDialect.applyQuote(column.getTableName())
                    + exasolSqlDialect.getTableCatalogAndSchemaSeparator();
        }
        return tablePrefix;
    }
}
