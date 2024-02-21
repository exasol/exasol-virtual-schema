package com.exasol.adapter.dialects.exasol;

import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.AbstractQueryRewriter;
import com.exasol.adapter.jdbc.RemoteMetadataReader;

/**
 * Exasol-specific query rewriter for {@code IMPORT FROM EXA} that does not add data types to the pushdown query. Data
 * types like {@code HASHTYPE} will be reported as {@code VARCHAR}.
 */
public class ExasolFromExaQueryRewriter extends AbstractQueryRewriter {

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
    protected String generateImportStatement(final String connectionDefinition, final String pushdownQuery) {
        return "IMPORT FROM EXA " + connectionDefinition + " STATEMENT '" + pushdownQuery.replace("'", "''") + "'";
    }
}
