package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.AdapterProperties.CONNECTION_NAME_PROPERTY;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_CONNECTION_PROPERTY;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_IMPORT_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

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
class ExasolFromExaQueryRewriterTest {
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
    @Mock
    private Connection connectionMock;

    @Test
    void rewritePushdownQuery() throws AdapterException, SQLException {
        final AdapterProperties properties = createAdapterProperties();
        final SqlDialect dialect = new ExasolSqlDialect(connectionFactoryMock, properties);
        final QueryRewriter queryRewriter = new ExasolFromExaQueryRewriter(dialect,
                new ExasolMetadataReader(connectionMock, properties));
        assertThat(
                queryRewriter.rewrite(TestSqlStatementFactory.createSelectOneFromDual(), EMPTY_SELECT_LIST_DATA_TYPES,
                        exaMetadataMock, properties),
                equalTo("IMPORT FROM EXA AT \"THE_EXA_CONNECTION\" STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
    }

    private AdapterProperties createAdapterProperties() {
        return new AdapterProperties(Map.of(EXASOL_IMPORT_PROPERTY, "true", //
                CONNECTION_NAME_PROPERTY, "exasol_connection", //
                EXASOL_CONNECTION_PROPERTY, "THE_EXA_CONNECTION", //
                "GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA", "false"));
    }

    @Test
    void rewritePushdownQueryEscapesSingleQuotes() throws AdapterException, SQLException {
        final AdapterProperties properties = createAdapterProperties();
        when(dialectMock.getSqlGenerator(any())).thenReturn(sqlGeneratorMock);
        when(sqlGeneratorMock.generateSqlFor(any())).thenReturn("string ' with '' quotes \"...");
        final QueryRewriter queryRewriter = new ExasolFromExaQueryRewriter(dialectMock,
                new ExasolMetadataReader(connectionMock, properties));
        assertThat(
                queryRewriter.rewrite(TestSqlStatementFactory.createSelectOneFromDual(), EMPTY_SELECT_LIST_DATA_TYPES,
                        exaMetadataMock, properties),
                equalTo("IMPORT FROM EXA AT \"THE_EXA_CONNECTION\" STATEMENT 'string '' with '''' quotes \"...'"));
    }
}
