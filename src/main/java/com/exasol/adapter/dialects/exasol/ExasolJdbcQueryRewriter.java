package com.exasol.adapter.dialects.exasol;

import java.sql.SQLException;

import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.AbstractQueryRewriter;
import com.exasol.adapter.jdbc.*;

/**
 * Exasol-specific query rewriter for regular JDBC connections to the remote Exasol data source.
 */
public class ExasolJdbcQueryRewriter extends AbstractQueryRewriter {
    private final ConnectionFactory connectionFactory;

    /**
     * Create a new instance of the {@link ExasolJdbcQueryRewriter}.
     *
     * @param dialect              dialect
     * @param remoteMetadataReader remote metadata reader
     * @param connectionFactory    factory for JDBC connection to remote data source
     */
    public ExasolJdbcQueryRewriter(final SqlDialect dialect, final RemoteMetadataReader remoteMetadataReader,
            final ConnectionFactory connectionFactory) {
        super(dialect, remoteMetadataReader, new ExasolConnectionDefinitionBuilder());
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected String generateImportStatement(String connectionDefinition, String pushdownQuery) throws SQLException {
        final String columnDescription = this.createImportColumnsDescription(pushdownQuery);
        return "IMPORT INTO (" + columnDescription + ") FROM JDBC " + connectionDefinition + " STATEMENT '"
                + pushdownQuery.replace("'", "''") + "'";
    }

    private String createImportColumnsDescription(final String query) throws SQLException {
        final ColumnMetadataReader columnMetadataReader = this.remoteMetadataReader.getColumnMetadataReader();
        final ResultSetMetadataReader resultSetMetadataReader = new ResultSetMetadataReader(
                this.connectionFactory.getConnection(), columnMetadataReader);
        return resultSetMetadataReader.describeColumns(query);
    }
}