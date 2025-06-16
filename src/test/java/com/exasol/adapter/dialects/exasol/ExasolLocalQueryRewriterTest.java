package com.exasol.adapter.dialects.exasol;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.sql.SQLException;
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
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.sql.TestSqlStatementFactory;

@ExtendWith(MockitoExtension.class)
class ExasolLocalQueryRewriterTest {
    private static final List<DataType> EMPTY_SELECT_LIST_DATA_TYPES = emptyList();
    @Mock
    private ExaMetadata exaMetadataMock;

    @Test
    void rewriteLocal() throws AdapterException, SQLException {
        final AdapterProperties properties = new AdapterProperties(Map.of("IS_LOCAL", "true"));
        final SqlDialect dialect = new ExasolSqlDialect(null, properties, exaMetadataMock);
        final QueryRewriter queryRewriter = new ExasolLocalQueryRewriter(dialect);
        assertThat(queryRewriter.rewrite(TestSqlStatementFactory.createSelectOneFromDual(),
                EMPTY_SELECT_LIST_DATA_TYPES, exaMetadataMock, properties), equalTo("SELECT 1 FROM \"DUAL\""));
    }
}
