package com.exasol.adapter.dialects.exasol;

import java.util.logging.Logger;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterException;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.*;
import com.exasol.adapter.dialects.rewriting.SqlGenerationContext;
import com.exasol.adapter.sql.SqlStatement;

/**
 * Exasol-specific query rewriter for cases where the {@code IS_LOCAL} option is used.
 * <p>
 * In this case the query is rewritten into a {@code SELECT} rather than to the usual {@code IMPORT} statement. This way
 * the Virtual Schema query can directly be executed on the local database in the context of the statement it was
 * embedded in. That saves the overhead of using the ExaLoader and is thus considerably faster.
 * </p>
 */
public class ExasolLocalQueryRewriter implements QueryRewriter {
    private static final Logger LOGGER = Logger.getLogger(ExasolLocalQueryRewriter.class.getName());

    private final SqlDialect dialect;

    /**
     * Create a new instance of the {@link ExasolLocalQueryRewriter}.
     *
     * @param dialect dialect
     */
    public ExasolLocalQueryRewriter(final SqlDialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public String rewrite(final SqlStatement statement, final ExaMetadata exaMetadata,
            final AdapterProperties properties) throws AdapterException {
        final SqlGenerationContext context = new SqlGenerationContext(properties.getCatalogName(),
                properties.getSchemaName(), false);
        final SqlGenerator sqlGeneratorVisitor = this.dialect.getSqlGenerator(context);
        final String selectStatement = sqlGeneratorVisitor.generateSqlFor(statement);
        LOGGER.finer(() -> "SELECT push-down statement:\n" + selectStatement);
        return selectStatement;
    }
}