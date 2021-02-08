package com.exasol.adapter.dialects.exasol;

import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.ImportIntoTemporaryTableQueryRewriter;
import com.exasol.adapter.jdbc.ConnectionFactory;
import com.exasol.adapter.jdbc.RemoteMetadataReader;

/**
 * Exasol-specific query rewriter for regular JDBC connections to the remote Exasol data source.
 */
public class ExasolJdbcQueryRewriter extends ImportIntoTemporaryTableQueryRewriter {
    /**
     * Create a new instance of the {@link ExasolJdbcQueryRewriter}.
     *
     * @param dialect              dialect
     * @param remoteMetadataReader remote metadata reader
     * @param connectionFactory    factory for JDBC connection to remote data source
     */
    public ExasolJdbcQueryRewriter(final SqlDialect dialect, final RemoteMetadataReader remoteMetadataReader,
            final ConnectionFactory connectionFactory) {
        super(dialect, remoteMetadataReader, connectionFactory, new ExasolConnectionDefinitionBuilder());
    }
}