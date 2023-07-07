package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.AdapterProperties.CONNECTION_NAME_PROPERTY;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exasol.adapter.AdapterException;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.*;
import com.exasol.adapter.dialects.rewriting.AbstractQueryRewriterTestBase;
import com.exasol.adapter.jdbc.ConnectionFactory;
import com.exasol.adapter.metadata.*;
import com.exasol.adapter.sql.*;

@ExtendWith(MockitoExtension.class)
class ExasolFromExaQueryRewriterTest extends AbstractQueryRewriterTestBase {
    private static final List<DataType> EMPTY_SELECT_LIST_DATA_TYPES = Collections.emptyList();

    @BeforeEach
    void beforeEach() {
        this.statement = TestSqlStatementFactory.createSelectOneFromDual();
    }

    @Test
    void rewriteWithJdbcConnection(@Mock final ConnectionFactory connectionFactoryMock)
            throws AdapterException, SQLException {
        final Connection connectionMock = mockConnection();
        when(connectionFactoryMock.getConnection()).thenReturn(connectionMock);
        final AdapterProperties properties = new AdapterProperties(Map.of("CONNECTION_NAME", CONNECTION_NAME));
        final SqlDialectFactory dialectFactory = new ExasolSqlDialectFactory();
        final SqlDialect dialect = dialectFactory.createSqlDialect(connectionFactoryMock, properties);
        final ExasolMetadataReader metadataReader = new ExasolMetadataReader(connectionMock, properties);
        final QueryRewriter queryRewriter = new ExasolJdbcQueryRewriter(dialect, metadataReader, connectionFactoryMock);
        assertThat(queryRewriter.rewrite(this.statement, EMPTY_SELECT_LIST_DATA_TYPES, EXA_METADATA, properties),
                equalTo("IMPORT INTO (c1 DECIMAL(18, 0)) FROM JDBC AT " + CONNECTION_NAME
                        + " STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
    }

    @Test
    void rewriteWithJdbcConnectionAndExpectedResultSetDataTypes(@Mock final ConnectionFactory connectionFactoryMock)
            throws AdapterException, SQLException {
        final Connection connectionMock = mock(Connection.class);
        final AdapterProperties properties = new AdapterProperties(Map.of("CONNECTION_NAME", CONNECTION_NAME));
        final SqlDialectFactory dialectFactory = new ExasolSqlDialectFactory();
        final SqlDialect dialect = dialectFactory.createSqlDialect(connectionFactoryMock, properties);
        final ExasolMetadataReader metadataReader = new ExasolMetadataReader(connectionMock, properties);
        final QueryRewriter queryRewriter = new ExasolJdbcQueryRewriter(dialect, metadataReader, connectionFactoryMock);
        final List<DataType> dataTypes = List.of(DataType.createGeometry(4));
        assertThat(queryRewriter.rewrite(this.statement, dataTypes, EXA_METADATA, properties),
                equalTo("IMPORT INTO (c1 GEOMETRY(4)) FROM JDBC AT " + CONNECTION_NAME
                        + " STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
    }

    @Test
    void rewriteLocal() throws AdapterException, SQLException {
        final AdapterProperties properties = new AdapterProperties(Map.of(EXASOL_IS_LOCAL_PROPERTY, "true"));
        final SqlDialect dialect = new ExasolSqlDialect(null, properties);
        final QueryRewriter queryRewriter = new ExasolLocalQueryRewriter(dialect);
        assertThat(queryRewriter.rewrite(this.statement, EMPTY_SELECT_LIST_DATA_TYPES, EXA_METADATA, properties),
                equalTo("SELECT 1 FROM \"DUAL\""));
    }

    @Test
    void rewriteToImportFromExaWithConnectionDetailsInProperties() throws AdapterException, SQLException {
        final AdapterProperties properties = new AdapterProperties(Map.of(EXASOL_IMPORT_PROPERTY, "true", //
                CONNECTION_NAME_PROPERTY, "exasol_connection", //
                EXASOL_CONNECTION_PROPERTY, "THE_EXA_CONNECTION"));
        final SqlDialect dialect = new ExasolSqlDialect(null, properties);
        final QueryRewriter queryRewriter = new ExasolFromExaQueryRewriter(dialect, null);
        assertThat(queryRewriter.rewrite(this.statement, EMPTY_SELECT_LIST_DATA_TYPES, EXA_METADATA, properties),
                equalTo("IMPORT FROM EXA AT \"THE_EXA_CONNECTION\"" + " STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
    }

    static class MySqlStatementFactory {
        private static final String SYSDUMMY = "SYSDUMMY1";

        static public SqlStatement selectGeometry() {
            return selectGeometry(SYSDUMMY);
        }

        private static SqlStatement selectGeometry(final String tableName) {
            final ColumnMetadata columnMetadata = ColumnMetadata.builder().name("the_column")
                    .type(DataType.createGeometry(18)).build();
            final TableMetadata tableMetadata = new TableMetadata(tableName, "", Arrays.asList(columnMetadata), "");
            final SqlNode fromClause = new SqlTable(tableName, tableMetadata);
            final SqlSelectList selectList = SqlSelectList
                    .createRegularSelectList(List.of(new SqlFunctionScalar(ScalarFunction.ST_POINTN, //
                            List.of( //
                                    new SqlLiteralExactnumeric(BigDecimal.valueOf(2)),
                                    new SqlLiteralExactnumeric(BigDecimal.valueOf(3))))));
            return SqlStatementSelect.builder().selectList(selectList).fromClause(fromClause).build();
        }
    }
}