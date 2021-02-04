package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.AdapterProperties.*;
import static com.exasol.adapter.capabilities.MainCapability.*;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_CONNECTION_PROPERTY;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_IMPORT_PROPERTY;
import static com.exasol.adapter.dialects.exasol.ExasolSqlDialect.EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH;
import static com.exasol.reflect.ReflectionUtils.getMethodReturnViaReflection;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import com.exasol.adapter.dialects.rewriting.SqlGenerationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exasol.adapter.AdapterException;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.adapternotes.ColumnAdapterNotes;
import com.exasol.adapter.adapternotes.ColumnAdapterNotesJsonConverter;
import com.exasol.adapter.capabilities.*;
import com.exasol.adapter.dialects.*;
import com.exasol.adapter.jdbc.ConnectionFactory;
import com.exasol.adapter.metadata.*;
import com.exasol.adapter.sql.*;
import com.exasol.sql.SqlNormalizer;

@ExtendWith(MockitoExtension.class)
class ExasolSqlDialectTest {
    @Mock
    private ConnectionFactory connectionFactoryMock;
    private ExasolSqlDialect dialect;
    private Map<String, String> rawProperties;

    @BeforeEach
    void beforeEach() {
        this.dialect = new ExasolSqlDialect(this.connectionFactoryMock, AdapterProperties.emptyProperties());
        this.rawProperties = new HashMap<>();
    }

    @CsvSource({ "A1, \"A1\"", //
            "A_1, \"A_1\"", //
            "A,\"A\"", //
            "A_a_1, \"A_a_1\"", //
            "1, \"1\"", //
            "1a, \"1a\", \"1a\"", //
            "'a\"b', \"a\"\"b\"" })
    @ParameterizedTest
    void testApplyQuote(final String identifier, final String expectedQuotingResult) {
        assertThat(this.dialect.applyQuote(identifier), equalTo(expectedQuotingResult));
    }

    @Test
    void testExasolSqlDialectSupportsAllCapabilities() {
        final Capabilities capabilities = this.dialect.getCapabilities();
        assertAll(
                () -> assertThat(capabilities.getMainCapabilities(),
                        containsInAnyOrder(SELECTLIST_PROJECTION, SELECTLIST_EXPRESSIONS, FILTER_EXPRESSIONS,
                                AGGREGATE_SINGLE_GROUP, AGGREGATE_GROUP_BY_COLUMN, AGGREGATE_GROUP_BY_EXPRESSION,
                                AGGREGATE_GROUP_BY_TUPLE, AGGREGATE_HAVING, ORDER_BY_COLUMN, ORDER_BY_EXPRESSION, LIMIT,
                                LIMIT_WITH_OFFSET, JOIN, JOIN_TYPE_INNER, JOIN_TYPE_LEFT_OUTER, JOIN_TYPE_RIGHT_OUTER,
                                JOIN_TYPE_FULL_OUTER, JOIN_CONDITION_EQUI)),
                () -> assertThat(capabilities.getLiteralCapabilities(), containsInAnyOrder(LiteralCapability.values())),
                () -> assertThat(capabilities.getPredicateCapabilities(),
                        containsInAnyOrder(PredicateCapability.values())),
                () -> assertThat(capabilities.getScalarFunctionCapabilities(),
                        containsInAnyOrder(ScalarFunctionCapability.values())),
                () -> assertThat(capabilities.getAggregateFunctionCapabilities(),
                        containsInAnyOrder(AggregateFunctionCapability.values())));
    }

    @Test
    void testSqlGenerator() throws AdapterException {
        final SqlNode node = getTestSqlNode();
        final String schemaName = "SCHEMA";
        final String expectedSql = "SELECT \"USER_ID\", COUNT(\"URL\") FROM \"" + schemaName + "\".\"CLICKS\""
                + " WHERE 1 < \"USER_ID\"" + " GROUP BY \"USER_ID\"" + " HAVING 1 < COUNT(\"URL\")"
                + " ORDER BY \"USER_ID\"" + " LIMIT 10";
        final SqlGenerationContext context = new SqlGenerationContext("", schemaName, false);
        final SqlGenerator generator = this.dialect.getSqlGenerator(context);
        final String actualSql = generator.generateSqlFor(node);
        assertThat(SqlNormalizer.normalizeSql(actualSql), equalTo(SqlNormalizer.normalizeSql(expectedSql)));
    }

    private SqlNode getTestSqlNode() {
        // SELECT USER_ID, count(URL) FROM CLICKS
        // WHERE 1 < USER_ID
        // GROUP BY USER_ID
        // HAVING 1 < COUNT(URL)
        // ORDER BY USER_ID
        // LIMIT 10;
        final TableMetadata clicksMeta = getClicksTableMetadata();
        final SqlTable fromClause = new SqlTable("CLICKS", clicksMeta);
        final SqlSelectList selectList = SqlSelectList.createRegularSelectList(
                List.of(new SqlColumn(0, clicksMeta.getColumns().get(0)), new SqlFunctionAggregate(
                        AggregateFunction.COUNT, List.of(new SqlColumn(1, clicksMeta.getColumns().get(1))), false)));
        final SqlNode whereClause = new SqlPredicateLess(new SqlLiteralExactnumeric(BigDecimal.ONE),
                new SqlColumn(0, clicksMeta.getColumns().get(0)));
        final SqlExpressionList groupBy = new SqlGroupBy(List.of(new SqlColumn(0, clicksMeta.getColumns().get(0))));
        final SqlNode countUrl = new SqlFunctionAggregate(AggregateFunction.COUNT,
                List.of(new SqlColumn(1, clicksMeta.getColumns().get(1))), false);
        final SqlNode having = new SqlPredicateLess(new SqlLiteralExactnumeric(BigDecimal.ONE), countUrl);
        final SqlOrderBy orderBy = new SqlOrderBy(List.of(new SqlColumn(0, clicksMeta.getColumns().get(0))),
                List.of(true), List.of(true));
        final SqlLimit limit = new SqlLimit(10);
        return SqlStatementSelect.builder().selectList(selectList).fromClause(fromClause).whereClause(whereClause)
                .groupBy(groupBy).having(having).orderBy(orderBy).limit(limit).build();
    }

    private TableMetadata getClicksTableMetadata() {
        final List<ColumnMetadata> columns = new ArrayList<>();
        final ColumnAdapterNotesJsonConverter converter = ColumnAdapterNotesJsonConverter.getInstance();
        columns.add(ColumnMetadata.builder().name("USER_ID")
                .adapterNotes(converter.convertToJson(ColumnAdapterNotes.builder().jdbcDataType(3).build()))
                .type(DataType.createDecimal(18, 0)).nullable(true).identity(false).defaultValue("").comment("")
                .build());
        columns.add(ColumnMetadata.builder().name("URL")
                .adapterNotes(converter.convertToJson(ColumnAdapterNotes.builder().jdbcDataType(12).build()))
                .type(DataType.createVarChar(10000, DataType.ExaCharset.UTF8)).nullable(true).identity(false)
                .defaultValue("").comment("").build());
        return new TableMetadata("CLICKS", "", columns, "");
    }

    @Test
    void testCreateRemoteMetadataReader(@Mock final Connection connectionMock) throws SQLException {
        when(this.connectionFactoryMock.getConnection()).thenReturn(connectionMock);
        assertThat(getMethodReturnViaReflection(this.dialect, "createRemoteMetadataReader"),
                instanceOf(ExasolMetadataReader.class));
    }

    @Test
    void testCreateJdbcQueryRewriter(@Mock final Connection connectionMock) throws SQLException {
        when(this.connectionFactoryMock.getConnection()).thenReturn(connectionMock);
        assertThat(getMethodReturnViaReflection(this.dialect, "createQueryRewriter"),
                instanceOf(ExasolJdbcQueryRewriter.class));
    }

    @Test
    void testCreateLocalQueryRewriter() {
        this.rawProperties.put(IS_LOCAL_PROPERTY, "true");
        final AdapterProperties properties = new AdapterProperties(this.rawProperties);
        final SqlDialect dialect = new ExasolSqlDialect(this.connectionFactoryMock, properties);
        assertThat(getMethodReturnViaReflection(dialect, "createQueryRewriter"),
                instanceOf(ExasolLocalQueryRewriter.class));
    }

    @Test
    void testCreateFromExaQueryRewriter(@Mock final Connection connectionMock) throws SQLException {
        when(this.connectionFactoryMock.getConnection()).thenReturn(connectionMock);
        this.rawProperties.put(EXASOL_IMPORT_PROPERTY, "true");
        final AdapterProperties properties = new AdapterProperties(this.rawProperties);
        final SqlDialect dialect = new ExasolSqlDialect(this.connectionFactoryMock, properties);
        assertThat(getMethodReturnViaReflection(dialect, "createQueryRewriter"),
                instanceOf(ExasolFromExaQueryRewriter.class));
    }

    @Test
    void checkValidBoolOptionsWithExaConnection() throws PropertyValidationException {
        setMandatoryProperties();
        this.rawProperties.put(EXASOL_IMPORT_PROPERTY, "TrUe");
        this.rawProperties.put(EXASOL_CONNECTION_PROPERTY, "MY_EXA_CONNECTION");
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final SqlDialect sqlDialect = new ExasolSqlDialect(null, adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void checkValidBoolOptionsWithExaConnectionExplicitlyDisabled() throws PropertyValidationException {
        setMandatoryProperties();
        this.rawProperties.put(EXASOL_IMPORT_PROPERTY, "FalSe");
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final SqlDialect sqlDialect = new ExasolSqlDialect(null, adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void testInconsistentExasolProperties() {
        setMandatoryProperties();
        this.rawProperties.put(EXASOL_CONNECTION_PROPERTY, "localhost:5555");
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final SqlDialect sqlDialect = new ExasolSqlDialect(null, adapterProperties);
        final PropertyValidationException exception = assertThrows(PropertyValidationException.class,
                sqlDialect::validateProperties);
        assertThat(exception.getMessage(),
                containsString("You defined the property 'EXA_CONNECTION' without setting 'IMPORT_FROM_EXA' "));
    }

    @Test
    void testInvalidExasolProperties() {
        setMandatoryProperties();
        this.rawProperties.put(EXASOL_IMPORT_PROPERTY, "True");
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final SqlDialect sqlDialect = new ExasolSqlDialect(null, adapterProperties);
        final PropertyValidationException exception = assertThrows(PropertyValidationException.class,
                sqlDialect::validateProperties);
        assertThat(exception.getMessage(),
                containsString("You defined the property 'IMPORT_FROM_EXA'. Please also define 'EXA_CONNECTION'"));
    }

    @Test
    void testValidateCatalogProperty() throws PropertyValidationException {
        setMandatoryProperties();
        this.rawProperties.put(CATALOG_NAME_PROPERTY, "MY_CATALOG");
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final SqlDialect sqlDialect = new ExasolSqlDialect(null, adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void testValidateSchemaProperty() throws PropertyValidationException {
        setMandatoryProperties();
        this.rawProperties.put(SCHEMA_NAME_PROPERTY, "MY_SCHEMA");
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final SqlDialect sqlDialect = new ExasolSqlDialect(null, adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void checkInvalidIsLocalProperty() {
        setMandatoryProperties();
        this.rawProperties.put(IS_LOCAL_PROPERTY, "asdasd");
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final SqlDialect sqlDialect = new ExasolSqlDialect(null, adapterProperties);
        final PropertyValidationException exception = assertThrows(PropertyValidationException.class,
                sqlDialect::validateProperties);
        assertThat(exception.getMessage(), containsString(
                "The value 'asdasd' for the property 'IS_LOCAL' is invalid. It has to be either 'true' or 'false' (case "
                        + "insensitive)"));
    }

    @Test
    void checkValidIsLocalProperty1() throws PropertyValidationException {
        setMandatoryProperties();
        this.rawProperties.put(IS_LOCAL_PROPERTY, "TrUe");
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final SqlDialect sqlDialect = new ExasolSqlDialect(null, adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void checkValidIsLocalProperty() throws PropertyValidationException {
        setMandatoryProperties();
        this.rawProperties.put(IS_LOCAL_PROPERTY, "FalSe");
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final SqlDialect sqlDialect = new ExasolSqlDialect(null, adapterProperties);
        sqlDialect.validateProperties();
    }

    private void setMandatoryProperties() {
        this.rawProperties.put(AdapterProperties.SQL_DIALECT_PROPERTY, "EXASOL");
        this.rawProperties.put(AdapterProperties.CONNECTION_NAME_PROPERTY, "MY_CONN");
    }

    @Test
    void testIsTimestampWithLocalTimeZoneEnabled() {
        setMandatoryProperties();
        this.rawProperties.put(IGNORE_ERRORS_PROPERTY, EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH);
        final AdapterProperties adapterProperties = new AdapterProperties(this.rawProperties);
        final ExasolSqlDialect exasolSqlDialect = new ExasolSqlDialect(null, adapterProperties);
        assertThat(exasolSqlDialect.isTimestampWithLocalTimeZoneEnabled(), equalTo(true));
    }
}