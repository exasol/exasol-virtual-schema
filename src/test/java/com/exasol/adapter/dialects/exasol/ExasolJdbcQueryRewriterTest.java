package com.exasol.adapter.dialects.exasol;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.*;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterException;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.*;
import com.exasol.adapter.jdbc.ConnectionFactory;
import com.exasol.adapter.jdbc.RemoteMetadataReader;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.sql.TestSqlStatementFactory;

@ExtendWith(MockitoExtension.class)
class ExasolJdbcQueryRewriterTest {
    private static final String CONNECTION_NAME = "JDBC_conn";

    @Mock
    private RemoteMetadataReader metadataReaderMock;
    @Mock
    private ConnectionFactory connectionFactoryMock;
    @Mock
    private ExaMetadata exaMetadataMock;

    @Test
    void pushdownQuery() throws AdapterException, SQLException {
        final AdapterProperties properties = new AdapterProperties(Map.of("CONNECTION_NAME", CONNECTION_NAME));
        assertThat(
                testee(properties).rewrite(TestSqlStatementFactory.createSelectOneFromDual(),
                        List.of(DataType.createBool()), exaMetadataMock, properties),
                equalTo("IMPORT INTO (c1 BOOLEAN) FROM JDBC AT " + CONNECTION_NAME
                        + " STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
    }

    @Test
    void rewriteWithJdbcConnection() throws AdapterException, SQLException {
        final Connection connectionMock = mockConnection();
        when(connectionFactoryMock.getConnection()).thenReturn(connectionMock);
        final AdapterProperties properties = new AdapterProperties(Map.of("CONNECTION_NAME", CONNECTION_NAME));
        final SqlDialectFactory dialectFactory = new ExasolSqlDialectFactory();
        final SqlDialect dialect = dialectFactory.createSqlDialect(connectionFactoryMock, properties);
        final ExasolMetadataReader metadataReader = new ExasolMetadataReader(connectionMock, properties);
        final QueryRewriter queryRewriter = new ExasolJdbcQueryRewriter(dialect, metadataReader, connectionFactoryMock);
        assertThat(
                queryRewriter.rewrite(TestSqlStatementFactory.createSelectOneFromDual(), emptyList(), exaMetadataMock,
                        properties),
                equalTo("IMPORT INTO (c1 DECIMAL(18, 0)) FROM JDBC AT " + CONNECTION_NAME
                        + " STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
    }

    @Test
    void rewriteWithJdbcConnectionAndExpectedResultSetDataTypes() throws AdapterException, SQLException {
        final Connection connectionMock = mock(Connection.class);
        final AdapterProperties properties = new AdapterProperties(Map.of("CONNECTION_NAME", CONNECTION_NAME));
        final SqlDialectFactory dialectFactory = new ExasolSqlDialectFactory();
        final SqlDialect dialect = dialectFactory.createSqlDialect(connectionFactoryMock, properties);
        final ExasolMetadataReader metadataReader = new ExasolMetadataReader(connectionMock, properties);
        final QueryRewriter queryRewriter = new ExasolJdbcQueryRewriter(dialect, metadataReader, connectionFactoryMock);
        final List<DataType> dataTypes = List.of(DataType.createGeometry(4));
        assertThat(
                queryRewriter.rewrite(TestSqlStatementFactory.createSelectOneFromDual(), dataTypes, exaMetadataMock,
                        properties),
                equalTo("IMPORT INTO (c1 GEOMETRY(4)) FROM JDBC AT " + CONNECTION_NAME
                        + " STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
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

    private ExasolJdbcQueryRewriter testee(final AdapterProperties properties) {
        final SqlDialectFactory dialectFactory = new ExasolSqlDialectFactory();
        final SqlDialect dialect = dialectFactory.createSqlDialect(connectionFactoryMock, properties);
        return new ExasolJdbcQueryRewriter(dialect, metadataReaderMock, connectionFactoryMock);
    }
}
