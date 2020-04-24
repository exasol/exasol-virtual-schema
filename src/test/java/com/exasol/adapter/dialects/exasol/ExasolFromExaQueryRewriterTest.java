package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.AdapterProperties.*;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_CONNECTION_STRING_PROPERTY;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_IMPORT_PROPERTY;
import static com.exasol.reflect.ReflectionUtils.getMethodReturnViaReflection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exasol.adapter.AdapterException;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.*;
import com.exasol.adapter.jdbc.ConnectionFactory;
import com.exasol.adapter.sql.TestSqlStatementFactory;

@ExtendWith(MockitoExtension.class)
class ExasolFromExaQueryRewriterTest extends AbstractQueryRewriterTestBase {
    @BeforeEach
    void beforeEach() {
        this.statement = TestSqlStatementFactory.createSelectOneFromDual();
    }

    @Test
    void testRewriteWithJdbcConnection(@Mock final ConnectionFactory connectionFactoryMock)
            throws AdapterException, SQLException {
        final Connection connectionMock = mockConnection();
        when(connectionFactoryMock.getConnection()).thenReturn(connectionMock);
        final AdapterProperties properties = new AdapterProperties(Map.of("CONNECTION_NAME", CONNECTION_NAME));
        final SqlDialectFactory dialectFactory = new ExasolSqlDialectFactory();
        final SqlDialect dialect = dialectFactory.createSqlDialect(connectionFactoryMock, properties);
        final ExasolMetadataReader metadataReader = new ExasolMetadataReader(connectionMock, properties);
        final QueryRewriter queryRewriter = new ExasolJdbcQueryRewriter(dialect, metadataReader, connectionFactoryMock);
        assertThat(queryRewriter.rewrite(this.statement, EXA_METADATA, properties),
                equalTo("IMPORT INTO (c1 DECIMAL(18, 0)) FROM JDBC AT " + CONNECTION_NAME
                        + " STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
    }

    @Test
    void testRewriteLocal() throws AdapterException, SQLException {
        final AdapterProperties properties = new AdapterProperties(Map.of(IS_LOCAL_PROPERTY, "true"));
        final SqlDialect dialect = new ExasolSqlDialect(null, properties);
        final QueryRewriter queryRewriter = new ExasolLocalQueryRewriter(dialect);
        assertThat(queryRewriter.rewrite(this.statement, EXA_METADATA, properties), equalTo("SELECT 1 FROM \"DUAL\""));
    }

    @Test
    void testRewriteToImportFromExaWithConnectionDetailsInProperties() throws AdapterException, SQLException {
        final AdapterProperties properties = new AdapterProperties(Map.of(EXASOL_IMPORT_PROPERTY, "true", //
                CONNECTION_NAME_PROPERTY, "exasol_connection", //
                EXASOL_CONNECTION_STRING_PROPERTY, "localhost:7861"));
        final SqlDialect dialect = new ExasolSqlDialect(null, properties);
        final QueryRewriter queryRewriter = new ExasolFromExaQueryRewriter(dialect, null, null);
        assertThat(queryRewriter.rewrite(this.statement, EXA_METADATA, properties),
                equalTo("IMPORT FROM EXA AT 'localhost:7861' USER 'connection_user' IDENTIFIED BY 'connection_secret'"
                        + " STATEMENT 'SELECT 1 FROM \"DUAL\"'"));
    }

    @Test
    void testConnectionDefinitionBuilderClass() {
        final SqlDialect dialect = new ExasolSqlDialect(null, AdapterProperties.emptyProperties());
        final QueryRewriter queryRewriter = new ExasolFromExaQueryRewriter(dialect, null, null);
        assertThat(getMethodReturnViaReflection(queryRewriter, "createConnectionDefinitionBuilder"),
                instanceOf(ExasolConnectionDefinitionBuilder.class));
    }
}