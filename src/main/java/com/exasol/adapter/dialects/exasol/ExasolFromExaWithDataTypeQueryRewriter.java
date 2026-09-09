package com.exasol.adapter.dialects.exasol;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterException;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.*;
import com.exasol.adapter.jdbc.*;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.sql.SqlStatement;

/**
 * Exasol-specific query rewriter for {@code IMPORT FROM EXA} that adds data types to the pushdown query. Data types
 * like {@code HASHTYPE} will be reported correctly.
 * <p>
 * This rewriter is similar to {@link ExasolJdbcQueryRewriter} but uses {@code IMPORT INTO (...) FROM EXA}.
 */
class ExasolFromExaWithDataTypeQueryRewriter extends AbstractQueryRewriter {

    private static final Logger LOGGER = Logger.getLogger(ExasolFromExaWithDataTypeQueryRewriter.class.getName());
    private final ConnectionFactory connectionFactory;

    ExasolFromExaWithDataTypeQueryRewriter(final SqlDialect dialect, final RemoteMetadataReader remoteMetadataReader,
            final ConnectionFactory connectionFactory) {
        super(dialect, remoteMetadataReader, new ExasolConnectionDefinitionBuilder());
        this.connectionFactory = connectionFactory;
    }

    // Replace this workaround with the VSCJDBC query-generation hook once available.
    // See https://github.com/exasol/exasol-virtual-schema/issues/156
    @Override
    public String rewrite(final SqlStatement statement, final List<DataType> selectListDataTypes,
            final ExaMetadata exaMetadata, final AdapterProperties properties) throws AdapterException, SQLException {
        if (selectListDataTypes.isEmpty()) {
            return super.rewrite(statement, selectListDataTypes, exaMetadata, properties);
        }
        final String pushdownQuery = new ExasolTypedNullSqlGenerationVisitor(this.dialect,
                new SqlGenerationContext(properties.getCatalogName(), properties.getSchemaName(), false),
                selectListDataTypes).generateSqlFor(statement);
        final String connectionDefinition = this.connectionDefinitionBuilder.buildConnectionDefinition(properties,
                getConnectionInformation(exaMetadata, properties));
        return generateImportStatement(connectionDefinition, selectListDataTypes, pushdownQuery);
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
                + ExasolSqlEscaper.escapeStringLiteralContent(pushdownQuery) + "'";
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
