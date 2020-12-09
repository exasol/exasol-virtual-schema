package com.exasol.adapter.dialects.exasol;

import java.sql.SQLException;

import com.exasol.adapter.dialects.AbstractQueryRewriter;
import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.jdbc.ConnectionDefinitionBuilder;
import com.exasol.adapter.jdbc.RemoteMetadataReader;

/**
 * Exasol-specific query rewriter for {@code IMPORT FROM EXA}.
 */
public class ExasolFromExaQueryRewriter extends AbstractQueryRewriter {
    /**
     * Create a new instance of the {@link ExasolFromExaQueryRewriter}.
     *
     * @param dialect              dialect
     * @param remoteMetadataReader remote metadata reader
     */
    public ExasolFromExaQueryRewriter(final SqlDialect dialect, final RemoteMetadataReader remoteMetadataReader
           ) {
        super(dialect, remoteMetadataReader);
    }

    @Override
    protected ConnectionDefinitionBuilder createConnectionDefinitionBuilder() {
        return new ExasolConnectionDefinitionBuilder();
    }

    @Override
    protected String generateImportStatement(String connectionDefinition, String pushdownQuery) throws SQLException {
        return "IMPORT FROM EXA " + connectionDefinition + " STATEMENT '"
                + pushdownQuery.replace("'", "''") + "'";
    }
}