package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.AdapterProperties.CONNECTION_NAME_PROPERTY;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterException;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.QueryRewriter;
import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.SqlGenerator;
import com.exasol.adapter.jdbc.ConnectionFactory;
import com.exasol.adapter.jdbc.RemoteMetadataReader;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.sql.TestSqlStatementFactory;

@ExtendWith(MockitoExtension.class)
class ExasolFromExaWithDataTypeQueryRewriterTest {
    private static final List<DataType> EMPTY_SELECT_LIST_DATA_TYPES = Collections.emptyList();
    @Mock
    private RemoteMetadataReader metadataReaderMock;
    @Mock
    private ConnectionFactory connectionFactoryMock;
    @Mock
    private ExaMetadata exaMetadataMock;
    @Mock
    private SqlDialect dialectMock;
    @Mock
    private SqlGenerator sqlGeneratorMock;

    @Test
    void rewritePushdownQuery() throws AdapterException, SQLException {
        final Connection connectionMock = mockConnection();
        when(connectionFactoryMock.getConnection()).thenReturn(connectionMock);
        final AdapterProperties properties = createAdapterProperties();
        final SqlDialect dialect = new ExasolSqlDialect(connectionFactoryMock, properties, exaMetadataMock);
        final QueryRewriter queryRewriter = new ExasolFromExaWithDataTypeQueryRewriter(dialect,
                new ExasolMetadataReader(connectionMock, properties, exaMetadataMock), connectionFactoryMock);
        assertThat(
                queryRewriter.rewrite(TestSqlStatementFactory.createSelectOneFromDual(), EMPTY_SELECT_LIST_DATA_TYPES,
                        exaMetadataMock, properties),
                equalTo("IMPORT INTO (c1 DECIMAL(18, 0)) FROM EXA AT \"THE_EXA_CONNECTION\""
                        + " STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
    }

    private AdapterProperties createAdapterProperties() {
        return new AdapterProperties(Map.of(EXASOL_IMPORT_PROPERTY, "true", //
                CONNECTION_NAME_PROPERTY, "exasol_connection", //
                EXASOL_CONNECTION_PROPERTY, "THE_EXA_CONNECTION", //
                GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA, "true"));
    }

    @Test
    void rewritePushdownQueryEscapesSingleQuotes() throws AdapterException, SQLException {
        final Connection connectionMock = mockConnection();
        when(connectionFactoryMock.getConnection()).thenReturn(connectionMock);
        final AdapterProperties properties = createAdapterProperties();
        when(dialectMock.getSqlGenerator(any())).thenReturn(sqlGeneratorMock);
        when(sqlGeneratorMock.generateSqlFor(any())).thenReturn("string ' with '' quotes \"...");
        final QueryRewriter queryRewriter = new ExasolFromExaWithDataTypeQueryRewriter(dialectMock,
                new ExasolMetadataReader(connectionMock, properties, exaMetadataMock), connectionFactoryMock);
        assertThat(
                queryRewriter.rewrite(TestSqlStatementFactory.createSelectOneFromDual(), EMPTY_SELECT_LIST_DATA_TYPES,
                        exaMetadataMock, properties),
                equalTo("IMPORT INTO (c1 DECIMAL(18, 0)) FROM EXA AT \"THE_EXA_CONNECTION\""
                        + " STATEMENT 'string '' with '''' quotes \"...'"));
    }

    @Test
    void generateImportStatement() throws SQLException {
        final Connection connectionMock = mockConnection();
        when(connectionFactoryMock.getConnection()).thenReturn(connectionMock);
        final AdapterProperties properties = createAdapterProperties();
        final SqlDialect dialect = new ExasolSqlDialect(connectionFactoryMock, properties, exaMetadataMock);
        final ExasolFromExaWithDataTypeQueryRewriter queryRewriter = new ExasolFromExaWithDataTypeQueryRewriter(dialect,
                new ExasolMetadataReader(connectionMock, properties, exaMetadataMock), connectionFactoryMock);
        assertThat(queryRewriter.generateImportStatement("connection", "pushdownQuery"),
                equalTo("IMPORT INTO (c1 DECIMAL(18, 0)) FROM EXA connection STATEMENT 'pushdownQuery'"));
    }

    protected Connection mockConnection() throws SQLException {
        final ResultSetMetaData metadataMock = mock(ResultSetMetaData.class);
        when(metadataMock.getColumnCount()).thenReturn(1);
        when(metadataMock.getColumnType(1)).thenReturn(4);
        final PreparedStatement statementMock = mock(PreparedStatement.class);
        when(statementMock.getMetaData()).thenReturn(metadataMock);
        final Connection connectionMock = mock(Connection.class);
        when(connectionMock.prepareStatement(any())).thenReturn(statementMock);
        return connectionMock;
    }
}
