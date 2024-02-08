package com.exasol.adapter.dialects.exasol;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.*;
import com.exasol.adapter.jdbc.*;
import com.exasol.adapter.metadata.DataType;

/**
 * Exasol-specific query rewriter for {@code IMPORT FROM EXA}. It is similar to {@link ExasolJdbcQueryRewriter} but uses
 * {@code IMPORT INTO (...) FROM EXA}.
 */
class ExasolFromExaQueryRewriter extends AbstractQueryRewriter {

    private static final Logger LOGGER = Logger.getLogger(ExasolFromExaQueryRewriter.class.getName());
    private final ConnectionFactory connectionFactory;

    /**
     * Construct a new instance of {@link ImportIntoTemporaryTableQueryRewriter}.
     *
     * @param dialect              dialect
     * @param remoteMetadataReader remote metadata reader
     * @param connectionFactory    factory for the JDBC connection to remote data source
     */
    ExasolFromExaQueryRewriter(final SqlDialect dialect, final RemoteMetadataReader remoteMetadataReader,
            final ConnectionFactory connectionFactory) {
        super(dialect, remoteMetadataReader, new ExasolConnectionDefinitionBuilder());
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected String generateImportStatement(final String connectionDefinition,
            final List<DataType> selectListDataTypes, final String pushdownQuery) throws SQLException {
        return generateImportStatement(SqlGenerationHelper.createColumnsDescriptionFromDataTypes(selectListDataTypes),
                connectionDefinition, pushdownQuery);
    }

    @Override
    protected String generateImportStatement(final String connectionDefinition, final String pushdownQuery)
            throws SQLException {
        return generateImportStatement(createColumnsDescriptionFromQuery(pushdownQuery), connectionDefinition,
                pushdownQuery);
    }

    private String generateImportStatement(final String columnsDescription, final String connectionDefinition,
            final String pushdownQuery) {
        return "IMPORT INTO (" + columnsDescription + ") FROM EXA " //
                + connectionDefinition + " STATEMENT '" //
                + pushdownQuery.replace("'", "''") + "'";
    }

    private String createColumnsDescriptionFromQuery(final String query) throws SQLException {
        final ColumnMetadataReader columnMetadataReader = this.remoteMetadataReader.getColumnMetadataReader();
        final ResultSetMetadataReader resultSetMetadataReader = new ResultSetMetadataReader(
                this.connectionFactory.getConnection(), columnMetadataReader);
        final String columnsDescription = resultSetMetadataReader.describeColumns(query);
        LOGGER.finer(() -> "Import columns: " + columnsDescription);
        return columnsDescription;
    }
}
