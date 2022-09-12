package com.exasol.adapter.dialects.exasol;

import java.util.List;

import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.AbstractQueryRewriter;
import com.exasol.adapter.jdbc.RemoteMetadataReader;
import com.exasol.adapter.metadata.DataType;

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
    public ExasolFromExaQueryRewriter(final SqlDialect dialect, final RemoteMetadataReader remoteMetadataReader) {
        super(dialect, remoteMetadataReader, new ExasolConnectionDefinitionBuilder());
    }

    @Override
    protected String generateImportStatement(final String connectionDefinition,
            final List<DataType> selectListDataTypes, final String pushdownQuery) {
        return "IMPORT FROM EXA " + connectionDefinition + " STATEMENT '" + pushdownQuery.replace("'", "''") + "'";
    }
}