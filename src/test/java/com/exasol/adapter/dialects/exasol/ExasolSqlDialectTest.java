package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.AdapterProperties.*;
import static com.exasol.adapter.capabilities.MainCapability.*;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.*;
import static com.exasol.adapter.dialects.exasol.ExasolSqlDialect.EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterException;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.adapternotes.ColumnAdapterNotes;
import com.exasol.adapter.adapternotes.ColumnAdapterNotesJsonConverter;
import com.exasol.adapter.capabilities.*;
import com.exasol.adapter.dialects.QueryRewriter;
import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.SqlGenerator;
import com.exasol.adapter.dialects.rewriting.SqlGenerationContext;
import com.exasol.adapter.jdbc.ConnectionFactory;
import com.exasol.adapter.metadata.ColumnMetadata;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.metadata.TableMetadata;
import com.exasol.adapter.properties.PropertyValidationException;
import com.exasol.adapter.sql.*;
import com.exasol.sql.SqlNormalizer;

@ExtendWith(MockitoExtension.class)
class ExasolSqlDialectTest {
    @Mock
    private ConnectionFactory connectionFactoryMock;
    @Mock
    private ExaMetadata exaMetadataMock;
    private ExasolSqlDialect dialect;

    @BeforeEach
    void beforeEach() {
        this.dialect = testee(AdapterProperties.emptyProperties());
        lenient().when(exaMetadataMock.getDatabaseVersion()).thenReturn("8.34.0");
    }

    private ExasolSqlDialect testee(final Map<String, String> rawProperties) {
        return testee(new AdapterProperties(rawProperties));
    }

    private ExasolSqlDialect testee(final AdapterProperties properties) {
        return new ExasolSqlDialect(this.connectionFactoryMock, properties, this.exaMetadataMock);
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
        /**
         * SELECT USER_ID, count(URL) FROM CLICKS
         * WHERE 1 < USER_ID
         * GROUP BY USER_ID
         * HAVING 1 < COUNT(URL)
         * ORDER BY USER_ID
         * LIMIT 10;
         */
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
        assertThat(this.dialect.createRemoteMetadataReader(), instanceOf(ExasolMetadataReader.class));
    }

    @Test
    void createQueryRewriterForLocal() {
        assertThat(createQueryRewriter(Map.of("IS_LOCAL", "true")), instanceOf(ExasolLocalQueryRewriter.class));
    }

    @Test
    void createQueryRewriterForExa() {
        assertThat(createQueryRewriter(Map.of("IMPORT_FROM_EXA", "true")),
                instanceOf(ExasolFromExaQueryRewriter.class));
    }

    @Test
    void createQueryRewriterForExaWithDataType() {
        assertThat(
                createQueryRewriter(
                        Map.of("IMPORT_FROM_EXA", "true", "GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA", "true")),
                instanceOf(ExasolFromExaWithDataTypeQueryRewriter.class));
    }

    @Test
    void createQueryRewriterForJdbc() {
        assertThat(createQueryRewriter(Map.of()), instanceOf(ExasolJdbcQueryRewriter.class));
    }

    private QueryRewriter createQueryRewriter(final Map<String, String> rawProperties) {
        return testee(rawProperties).createQueryRewriter();
    }

    @Test
    void checkValidBoolOptionsWithExaConnection() throws PropertyValidationException {
        final AdapterProperties adapterProperties = mandatory() //
                .with(EXASOL_IMPORT_PROPERTY, "TrUe") //
                .with(EXASOL_CONNECTION_PROPERTY, "MY_EXA_CONNECTION") //
                .build();
        final SqlDialect sqlDialect = testee(adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void checkValidBoolOptionsWithExaConnectionExplicitlyDisabled() throws PropertyValidationException {
        final AdapterProperties adapterProperties = mandatory().with(EXASOL_IMPORT_PROPERTY, "FalSe").build();
        final SqlDialect sqlDialect = testee(adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void testInconsistentExasolProperties() {
        final AdapterProperties adapterProperties = mandatory().with(EXASOL_CONNECTION_PROPERTY, "localhost:5555")
                .build();
        final SqlDialect sqlDialect = testee(adapterProperties);
        final PropertyValidationException exception = assertThrows(PropertyValidationException.class,
                sqlDialect::validateProperties);
        assertThat(exception.getMessage(),
                containsString("You defined the property 'EXA_CONNECTION' without setting 'IMPORT_FROM_EXA' "));
    }

    @Test
    void testInvalidExasolProperties() {
        final AdapterProperties adapterProperties = mandatory() //
                .with(EXASOL_IMPORT_PROPERTY, "True") //
                .build();
        final SqlDialect sqlDialect = testee(adapterProperties);
        final PropertyValidationException exception = assertThrows(PropertyValidationException.class,
                sqlDialect::validateProperties);
        assertThat(exception.getMessage(),
                containsString("You defined the property 'IMPORT_FROM_EXA'. Please also define 'EXA_CONNECTION'"));
    }

    @Test
    void testValidateCatalogProperty() throws PropertyValidationException {
        final AdapterProperties adapterProperties = mandatory() //
                .with(CATALOG_NAME_PROPERTY, "MY_CATALOG") //
                .build();
        final SqlDialect sqlDialect = testee(adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void testValidateSchemaProperty() throws PropertyValidationException {
        final AdapterProperties adapterProperties = mandatory() //
                .with(SCHEMA_NAME_PROPERTY, "MY_SCHEMA") //
                .build();
        final SqlDialect sqlDialect = testee(adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void checkInvalidIsLocalProperty() {
        final AdapterProperties adapterProperties = mandatory() //
                .with(EXASOL_IS_LOCAL_PROPERTY, "asdasd") //
                .build();
        final SqlDialect sqlDialect = testee(adapterProperties);
        final PropertyValidationException exception = assertThrows(PropertyValidationException.class,
                sqlDialect::validateProperties);
        assertThat(exception.getMessage(), containsString("The value 'asdasd' for property 'IS_LOCAL' is invalid."
                + " It has to be either 'true' or 'false' (case insensitive)"));
    }

    @Test
    void checkValidIsLocalProperty1() throws PropertyValidationException {
        final AdapterProperties adapterProperties = mandatory().with(EXASOL_IS_LOCAL_PROPERTY, "TrUe").build();
        final SqlDialect sqlDialect = testee(adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void checkValidIsLocalProperty() throws PropertyValidationException {
        final AdapterProperties adapterProperties = mandatory().with(EXASOL_IS_LOCAL_PROPERTY, "FalSe").build();
        final SqlDialect sqlDialect = testee(adapterProperties);
        sqlDialect.validateProperties();
    }

    @Test
    void testIsTimestampWithLocalTimeZoneEnabled() {
        final AdapterProperties adapterProperties = mandatory() //
                .with(IGNORE_ERRORS_PROPERTY, EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH) //
                .build();
        final ExasolSqlDialect exasolSqlDialect = testee(adapterProperties);
        assertThat(exasolSqlDialect.isTimestampWithLocalTimeZoneEnabled(), equalTo(true));
    }

    @Test
    void testMissing() {
        final AdapterProperties adapterProperties = mandatory() //
                .remove(AdapterProperties.SCHEMA_NAME_PROPERTY) //
                .build();
        final ExasolSqlDialect sqlDialect = testee(adapterProperties);
        final Exception exception = assertThrows(PropertyValidationException.class,
                sqlDialect::validateProperties);
        assertThat(exception.getMessage(),
                equalTo("E-VSEXA-6: EXASOL virtual schema dialect requires to specify a schema name."
                        + " Please specify a schema name using property 'SCHEMA_NAME'."));
    }

    AdapterProperties mandatoryWith(final String key, final String value) {
        return mandatory().with(key, value).build();
    }

    AdapterPropertiesBuilder mandatory() {
        return adapterPropertiesBuilder() //
                .with(AdapterProperties.CONNECTION_NAME_PROPERTY, "MY_CONN") //
                .with(AdapterProperties.SCHEMA_NAME_PROPERTY, "MY_SCHEMA");
    }

    AdapterPropertiesBuilder adapterPropertiesBuilder() {
        return new AdapterPropertiesBuilder();
    }

    static class AdapterPropertiesBuilder {
        private final Map<String, String> raw = new HashMap<>();

        AdapterPropertiesBuilder with(final String key, final String value) {
            this.raw.put(key, value);
            return this;
        }

        AdapterPropertiesBuilder remove(final String key) {
            this.raw.remove(key);
            return this;
        }

        AdapterProperties build() {
            return new AdapterProperties(this.raw);
        }
    }
}
