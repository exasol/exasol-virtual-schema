package com.exasol.adapter.dialects.exasol;

import java.sql.SQLException;
import java.util.logging.Logger;

import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.AbstractQueryRewriter;
import com.exasol.adapter.jdbc.RemoteMetadataReader;

/**
 * Exasol-specific query rewriter for {@code IMPORT FROM EXA}.
 */
public class ExasolFromExaQueryRewriter extends AbstractQueryRewriter {
    private static final Logger LOGGER = Logger.getLogger(ExasolFromExaQueryRewriter.class.getName());

    /**
     * Create a new instance of the {@link ExasolFromExaQueryRewriter}.
     *
     * @param dialect              dialect
     * @param remoteMetadataReader remote metadata reader
     */
    public ExasolFromExaQueryRewriter(final SqlDialect dialect, final RemoteMetadataReader remoteMetadataReader) {
        super(dialect, remoteMetadataReader, new ExasolConnectionDefinitionBuilder());
    }

    @Override
    protected String generateImportStatement(final String connectionDefinition, final String pushdownQuery)
            throws SQLException {
        final String importStatement = "IMPORT FROM EXA " + connectionDefinition + " STATEMENT '"
                + pushdownQuery.replace("'", "''") + "'";
        LOGGER.finer(() -> "IMPORT push-down statement:\n" + importStatement);
        return importStatement;
    }
}