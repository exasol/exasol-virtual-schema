package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.dialects.exasol.ExasolSqlDialect.EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH;
import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.PATH_TO_VIRTUAL_SCHEMAS_JAR;
import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION;
import static com.exasol.dbbuilder.dialects.exasol.ExasolObjectPrivilege.SELECT;
import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.JdbcDatabaseContainer.NoDriverFoundException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.exasol.bucketfs.Bucket;
import com.exasol.bucketfs.BucketAccessException;
import com.exasol.containers.ExasolContainer;
import com.exasol.dbbuilder.dialects.*;
import com.exasol.dbbuilder.dialects.exasol.*;
import com.exasol.dbbuilder.dialects.exasol.AdapterScript.Language;
import com.exasol.matcher.ResultSetStructureMatcher.Builder;

@Tag("integration")
@Testcontainers
abstract class AbstractExasolSqlDialectIT {
    @Container
    protected static final ExasolContainer<? extends ExasolContainer<?>> CONTAINER = new ExasolContainer<>(
            IntegrationTestConfiguration.getDockerImageReference()).withReuse(true);
    private static final String EXASOL_DIALECT = "EXASOL";
    private static ExasolSchema adapterSchema;
    protected static ExasolObjectFactory objectFactory;
    protected static Connection connection;
    protected static AdapterScript adapterScript;
    protected ExasolSchema sourceSchema;
    protected User user;
    protected VirtualSchema virtualSchema;
    private ConnectionDefinition jdbcConnection;

    @BeforeAll
    static void beforeAll() throws BucketAccessException, InterruptedException, TimeoutException, IOException,
            NoDriverFoundException, SQLException {
        connection = CONTAINER.createConnection("");
        objectFactory = new ExasolObjectFactory(connection);
        adapterSchema = objectFactory.createSchema("ADAPTER_SCHEMA");
        adapterScript = installVirtualSchemaAdapter(adapterSchema);
    }

    private static AdapterScript installVirtualSchemaAdapter(final ExasolSchema adapterSchema)
            throws InterruptedException, BucketAccessException, TimeoutException {
        final Bucket bucket = CONTAINER.getDefaultBucket();
        bucket.uploadFile(PATH_TO_VIRTUAL_SCHEMAS_JAR, VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION);
        return adapterSchema.createAdapterScriptBuilder() //
                .name("EXASOL_ADAPTER") //
                .language(Language.JAVA) //
                .bucketFsContent("com.exasol.adapter.RequestDispatcher",
                        "/buckets/bfsdefault/default/" + VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION)
                .build();
    }

    @AfterAll
    static void afterAll() throws SQLException {
        dropAll(adapterScript, adapterSchema);
        adapterScript = null;
        adapterSchema = null;
        connection.close();
    }

    @BeforeEach
    void beforeEach() {
        this.sourceSchema = objectFactory.createSchema("SOURCE_SCHEMA");
        this.user = objectFactory.createLoginUser("VS_USER", "VS_USER_PWD").grant(this.sourceSchema, SELECT);
        this.jdbcConnection = createAdapterConnectionDefinition(this.user);
        this.virtualSchema = null;
    }

    private ConnectionDefinition createAdapterConnectionDefinition(final User user) {
        final String jdbcUrl = "jdbc:exa:localhost:" + CONTAINER.getDefaultInternalDatabasePort();
        return objectFactory.createConnectionDefinition("JDBC_CONNECTION", jdbcUrl, user.getName(), user.getPassword());
    }

    @AfterEach
    void afterEach() {
        dropAll(this.virtualSchema, this.jdbcConnection, this.user, this.sourceSchema);
        this.virtualSchema = null;
        this.jdbcConnection = null;
        this.user = null;
        this.sourceSchema = null;
    }

    /**
     * Drop all given database object if it is not already assigned to {@code null}.
     * <p>
     * The method is {@code static} so that it can be used in {@code afterAll()} too.
     * </p>
     *
     * @param databaseObjects database objects to be dropped
     */
    protected static void dropAll(final DatabaseObject... databaseObjects) {
        for (final DatabaseObject databaseObject : databaseObjects) {
            if (databaseObject != null) {
                databaseObject.drop();
            }
        }
    }

    /**
     * Provide the Virtual Schema properties that switch between different connection variants.
     *
     * @return raw properties
     */
    protected abstract Map<String, String> getConnectionSpecificVirtualSchemaProperties();

    @Test
    void testVarcharMappingUtf8() {
        final Table table = createSingleColumnTable("VARCHAR(20) UTF8").insert("Hello world!").insert("Grüße!");
        assertVirtualTableContents(table, table("VARCHAR").row("Hello world!").row("Grüße!").matches());
    }

    protected Table createSingleColumnTable(final String sourceType) {
        final String typeAsIdentifier = sourceType.replaceAll("[ ,]", "_").replaceAll("[()]", "");
        return this.sourceSchema.createTable("SINGLE_COLUMN_TABLE_" + typeAsIdentifier, "C1", sourceType);
    }

    protected void assertVirtualTableContents(final Table table, final Matcher<ResultSet> matcher) {
        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        try {
            assertThat(selectAllFromCorrespondingVirtualTable(virtualSchema, table), matcher);
        } catch (final SQLException exception) {
            fail("Unable to execute assertion query. Caused by: " + exception.getMessage());
        } finally {
            virtualSchema.drop();
        }
    }

    protected VirtualSchema createVirtualSchema(final Schema sourceSchema) {
        return objectFactory.createVirtualSchemaBuilder("THE_VS").dialectName(EXASOL_DIALECT) //
                .sourceSchema(sourceSchema) //
                .adapterScript(adapterScript) //
                .connectionDefinition(this.jdbcConnection) //
                .properties(getConnectionSpecificVirtualSchemaProperties()) //
                .build();
    }

    private ResultSet selectAllFromCorrespondingVirtualTable(final VirtualSchema virtualSchema, final Table table)
            throws SQLException {
        return selectAllFrom(getVirtualTableName(virtualSchema, table));
    }

    private ResultSet selectAllFrom(final String tableName) throws SQLException {
        return query("SELECT * FROM " + tableName);
    }

    private String getVirtualTableName(final VirtualSchema virtualSchema, final Table table) {
        return virtualSchema.getFullyQualifiedName() + ".\"" + table.getName() + "\"";
    }

    protected ResultSet query(final String sql) throws SQLException {
        return connection.createStatement().executeQuery(sql);
    }

    @Test
    void testVarcharMappingAscii() {
        final Table table = createSingleColumnTable("VARCHAR(20) ASCII").insert("Hello").insert("world");
        assertVirtualTableContents(table, table("VARCHAR").row("Hello").row("world").matches());
    }

    @Test
    void testCharMappingUtf8() {
        final Table table = createSingleColumnTable("CHAR(20) UTF8").insert("Howdy.").insert("Grüzi.");
        assertVirtualTableContents(table, table("CHAR").row(pad("Howdy.", 20)).row(pad("Grüzi.", 20)).matches());
    }

    protected String pad(final String text, final int padTo) {
        return text + " ".repeat(padTo - text.length());
    }

    @Test
    void testCharMappingAscii() {
        final Table table = createSingleColumnTable("CHAR(20) ASCII").insert("sun").insert("rain");
        assertVirtualTableContents(table, table("CHAR").row(pad("sun", 20)).row(pad("rain", 20)).matches());
    }

    @Test
    void testDecimalMapping() {
        final Table table = createSingleColumnTable("DECIMAL(5,1)") //
                .insert(-9999.9) //
                .insert(-1.2) //
                .insert(0.0) //
                .insert(1.2) //
                .insert(9999.9);
        assertVirtualTableContents(table, table("DECIMAL") //
                .row(-9999.9) //
                .row(-1.2) //
                .row(0.0) //
                .row(1.2) //
                .row(9999.9) //
                .matchesFuzzily()); // required because the JDBC driver turns DECIMAL into BigInteger
    }

    @Test
    void testDecimalIntegerMapping() {
        final Table table = createSingleColumnTable("DECIMAL(5,0)") //
                .insert(-99999) //
                .insert(-1) //
                .insert(0) //
                .insert(1) //
                .insert(99999);
        assertVirtualTableContents(table, table("INTEGER") //
                .row(-99999) //
                .row(-1) //
                .row(0) //
                .row(1) //
                .row(99999) //
                .matches());
    }

    @Test
    void testDoubleMapping() {
        final Table table = createSingleColumnTable("DOUBLE PRECISION") //
                .insert(-123.456) //
                .insert(-Math.PI) //
                .insert(-1.0).insert(0.0) //
                .insert(Math.PI) //
                .insert(123.456);
        assertVirtualTableContents(table, table("DOUBLE PRECISION") //
                .row(-123.456) //
                .row(-Math.PI) //
                .row(-1.0) //
                .row(0.0) //
                .row(Math.PI) //
                .row(123.456) //
                .matches());
    }

    @Test
    void testBooleanMapping() {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true).insert(false);
        assertVirtualTableContents(table, table("BOOLEAN").row(true).row(false).matches());
    }

    @Test
    void testDateMapping() {
        final java.sql.Date date = java.sql.Date.valueOf("2020-01-01");
        final Table table = createSingleColumnTable("DATE").insert(date);
        assertVirtualTableContents(table, table("DATE").row(date).matches());
    }

    @Test
    void testTimestampMapping() {
        final Timestamp timestamp = Timestamp.valueOf("2020-02-02 01:23:45.678");
        final Table table = createSingleColumnTable("TIMESTAMP").insert(timestamp);
        assertVirtualTableContents(table, table("TIMESTAMP").row(timestamp).matches());
    }

    @Test
    void testTimestampWithLocalTimeZoneMapping() {
        final Timestamp timestamp = Timestamp.valueOf("3030-03-03 12:34:56.789");
        final Table table = createSingleColumnTable("TIMESTAMP WITH LOCAL TIME ZONE").insert(timestamp);
        // Note that the JDBC driver reports the timestamp as regular timestamp in the result set.
        assertVirtualTableContents(table, table("TIMESTAMP").row(timestamp).matches());
    }

    @Test
    void testGeometryMapping() {
        final Table table = createSingleColumnTable("GEOMETRY").insert("POINT (2 3)");
        // Note that the JDBC driver reports the result as VARCHAR
        assertVirtualTableContents(table, table("VARCHAR").row("POINT (2 3)").matches());
    }

    @Test
    void testIdentifierCaseSensitivityOnTable() {
        Schema mixedCaseSchema = null;
        try {
            mixedCaseSchema = objectFactory.createSchema("MixedCaseSchema");
            this.user.grant(mixedCaseSchema, SELECT);
            final Table table = mixedCaseSchema.createTable("MixedCaseTable", "Column1", "VARCHAR(20)", "column2",
                    "VARCHAR(20)", "COLUMN3", "VARCHAR(20)").insert("foo", "bar", "baz");
            this.virtualSchema = createVirtualSchema(mixedCaseSchema);
            assertVsQuery(
                    "SELECT \"Column1\", \"column2\", COLUMN3 FROM " + getVirtualTableName(this.virtualSchema, table),
                    table().row("foo", "bar", "baz").matches());
        } finally {
            dropAll(mixedCaseSchema);
        }
    }

    private void assertVsQuery(final String sql, final Matcher<ResultSet> expected) {
        try {
            assertThat(query(sql), expected);
        } catch (final SQLException exception) {
            fail("Unable to run assertion query: " + sql + "\nCaused by: " + exception.getMessage());
        }
    }

    @Test
    void testSelectingUnquotedMixedCaseTableThrowsException() {
        final Table table = this.sourceSchema.createTable("MixedCaseTable", "C1", "BOOLEAN").insert(true);
        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        final String virtualTableUnquotedName = virtualSchema.getFullyQualifiedName() + "." + table.getName();
        final SQLException exception = assertThrows(SQLException.class, () -> selectAllFrom(virtualTableUnquotedName));
        assertThat(exception.getMessage(), matchesPattern(".*object.*not found.*"));
        virtualSchema.drop();
    }

    @CsvSource(value = { //
            "GROUP_CONCAT(A)                            : 101,102,103,104",
            "GROUP_CONCAT(A ORDER BY B)                 : 102,103,101,104",
            "GROUP_CONCAT(A ORDER BY B DESC)            : 104,101,103,102",
            "GROUP_CONCAT(A ORDER BY B DESC NULLS LAST) : 101,103,102,104",
            "GROUP_CONCAT(A SEPARATOR ';' || ' ')       : 101; 102; 103; 104" } //
            , delimiter = ':')
    @ParameterizedTest
    void testGroupConcatOrderBy(final String concat, final String concatResult) {
        final Table table = this.sourceSchema.createTable("GROUP_CONCAT_TABLE", "A", "INTEGER", "B", "INTEGER")
                .insert(101, 3) //
                .insert(102, 1) //
                .insert(103, 2) //
                .insert(104, null);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        final String sql = "SELECT " + concat + "FROM " + getVirtualTableName(this.virtualSchema, table);
        assertVsQuery(sql, table().row(concatResult).matches());
    }

    @Test
    void testExtractFromDate() {
        final Table table = createSingleColumnTable("DATE").insert("2000-11-01");
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT EXTRACT(MONTH FROM " + getColumnName(table, 0) + ") FROM "
                + getVirtualTableName(this.virtualSchema, table), table().row((short) 11).matches());
    }

    private String getColumnName(final Table table, final int index) {
        return table.getColumns().get(index).getName();
    }

    @Test
    void testExtractFromInterval() {
        final Table table = createSingleColumnTable("INTERVAL YEAR(3) TO MONTH").insert("123-09");
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT EXTRACT(MONTH FROM " + getColumnName(table, 0) + ") FROM "
                + getVirtualTableName(this.virtualSchema, table), table().row((short) 9).matches());
    }

    @Test
    void testCastVarcharToChar() {
        // assertCast("VARCHAR(20)", "CHAR(40)", "Hello.", pad("Hello.", 40));
        castFrom("VARCHAR(20)").to("CHAR(40)").input("Hello.").verify(pad("Hello.", 40));
    }

    protected CastAssertionBuilder castFrom(final String from) {
        return new CastAssertionBuilder(this, from);
    }

    @Test
    void testCastBooleanToVarchar() {
        castFrom("BOOLEAN").to("VARCHAR(5)").input(true, false).verify("TRUE", "FALSE");
    }

    @Test
    void testCastVarcharToDate() {
        castFrom("VARCHAR(30)").to("DATE").input("2020-10-17").verify(java.sql.Date.valueOf("2020-10-17"));
    }

    @Test
    void testCastVarcharasDecimal() {
        castFrom("VARCHAR(6)").to("DECIMAL(5,1)").input("1234.5").verify(BigDecimal.valueOf(1234.5));
    }

    @Test
    void testCastVarcharAsDouble() {
        castFrom("VARCHAR(6)").to("DOUBLE PRECISION").input("1234.5").verify(1234.5);
    }

    @Test
    void testCastVarcharAsGeometry() {
        castFrom("VARCHAR(20)").to("GEOMETRY(5)").input("POINT(2 5)").accept("VARCHAR").verify("POINT (2 5)");
    }

    @Test
    void testCastVarcharAsTimestamp() {
        final String timestampAsString = "2016-06-01 13:17:02.081";
        castFrom("VARCHAR(30)").to("TIMESTAMP").input(timestampAsString).verify(Timestamp.valueOf(timestampAsString));
    }

    @Test
    void testCastVarcharAsTimestampWithLocalTimezone() {
        final String timestamp = "2017-11-03 14:18:02.081";
        castFrom("VARCHAR(30)").to("TIMESTAMP WITH LOCAL TIME ZONE").input(timestamp).accept("TIMESTAMP")
                .verify(Timestamp.valueOf(timestamp));
    }

    @Test
    void castIntegerAsVarchar() {
        castFrom("INTEGER").to("VARCHAR(5)").input(12345).verify("12345");
    }

    @Test
    void testCaseEqual() {
        final Table table = createSingleColumnTable("INTEGER").insert(1).insert(2).insert(3);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT CASE C1 WHEN 1 THEN 'YES' WHEN 2 THEN 'PERHAPS' ELSE 'NO' END FROM " //
                + getVirtualTableName(this.virtualSchema, table), //
                table().row("YES").row("PERHAPS").row("NO").matches());
    }

    @Test
    void testCaseGreaterThan() {
        final Table table = createSingleColumnTable("INTEGER").insert(1).insert(2).insert(3);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT CASE WHEN C1 > 1 THEN 'YES' ELSE 'NO' END FROM " //
                + getVirtualTableName(this.virtualSchema, table), //
                table().row("NO").row("YES").row("YES").matches());
    }

    @Test
    void testLeftJoin() {
        createVirtualSchemaWithTablesForJoinTest();
        assertVsQuery("SELECT * FROM TL LEFT JOIN TR ON TL.C1 = TR.C1 ORDER BY TL.C1", //
                table() //
                        .row("K1", "L1", "K1", "R1") //
                        .row("K3", "L3", null, null) //
                        .row(null, "L2", null, null) //
                        .matches());
    }

    private void createVirtualSchemaWithTablesForJoinTest() {
        this.sourceSchema.createTable("TL", "C1", "VARCHAR(2)", "C2", "VARCHAR(2)") //
                .insert("K1", "L1").insert(null, "L2").insert("K3", "L3");
        this.sourceSchema.createTable("TR", "C1", "VARCHAR(2)", "C2", "VARCHAR(2)") //
                .insert("K1", "R1").insert("K2", "R2").insert(null, "R3");
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
    }

    @Test
    void testLeftJoinWithProjection() {
        createVirtualSchemaWithTablesForJoinTest();
        assertVsQuery("SELECT TL.C1, TL.C2, TR.C2 FROM TL LEFT JOIN TR ON TL.C1 = TR.C1 ORDER BY TL.C1", //
                table() //
                        .row("K1", "L1", "R1") //
                        .row("K3", "L3", null) //
                        .row(null, "L2", null) //
                        .matches());
    }

    @Test
    void testRightJoin() {
        createVirtualSchemaWithTablesForJoinTest();
        assertVsQuery("SELECT * FROM TL RIGHT JOIN TR ON TL.C1 = TR.C1 ORDER BY TL.C1, TR.C1", //
                table() //
                        .row("K1", "L1", "K1", "R1") //
                        .row(null, null, "K2", "R2") //
                        .row(null, null, null, "R3") //
                        .matches());
    }

    @Test
    void testInnerJoin() {
        createVirtualSchemaWithTablesForJoinTest();
        assertVsQuery("SELECT * FROM TL INNER JOIN TR ON TL.C1 = TR.C1 ORDER BY TL.C1", //
                table().row("K1", "L1", "K1", "R1").matches());
    }

    @Test
    void testFullOuterJoin() {
        createVirtualSchemaWithTablesForJoinTest();
        assertVsQuery("SELECT * FROM TL FULL OUTER JOIN TR ON TL.C1 = TR.C1 ORDER BY TL.C1, TL.C2", //
                table() //
                        .row("K1", "L1", "K1", "R1") //
                        .row("K3", "L3", null, null) //
                        .row(null, "L2", null, null) //
                        .row(null, null, "K2", "R2") //
                        .row(null, null, null, "R3") //
                        .matches());
    }

    @Test
    void CreateVirtualSchemaWithNonexistentConnectionThrowsException() {
        final String sql = "CREATE VIRTUAL SCHEMA VIRTUAL_SCHEMA_NONEXISTENT_CONNECTION\n" //
                + "USING " + adapterScript.getFullyQualifiedName() + " WITH\n" //
                + "SQL_DIALECT = 'EXASOL'\n" //
                + "CONNECTION_NAME = 'NONEXISTENT_CONNECTION'\n" //
                + "SCHEMA_NAME = '" + this.sourceSchema.getFullyQualifiedName() + "' ";
        final SQLException exception = assertThrows(SQLException.class, () -> query(sql));
        assertThat(exception.getMessage(),
                containsString("Could not access the connection information of connection \"NONEXISTENT_CONNECTION\""));
    }

    @Test
    void testCreateVirtualSchemaWithIgnoreErrorsProperty() throws SQLException {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        final Map<String, String> properties = new HashMap<>();
        properties.putAll(getConnectionSpecificVirtualSchemaProperties());
        properties.put("IGNORE_ERRORS", EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH);
        this.virtualSchema = objectFactory.createVirtualSchemaBuilder("VIRTUAL_SCHEMA_IGNORES_ERRORS") //
                .sourceSchema(this.sourceSchema) //
                .adapterScript(adapterScript) //
                .dialectName(EXASOL_DIALECT) //
                .properties(properties) //
                .connectionDefinition(this.jdbcConnection) //
                .build();
        assertThat(query("SELECT NOW() - INTERVAL '1' MINUTE FROM " + getVirtualTableName(this.virtualSchema, table)),
                instanceOf(ResultSet.class));
    }

    @Test
    void testCreateVirtualSchemaWithoutIgnoreErrorsPropertyThrowsException() throws SQLException {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        final SQLException exception = assertThrows(SQLException.class, () -> query(
                "SELECT NOW() - INTERVAL '1' MINUTE FROM " + getVirtualTableName(this.virtualSchema, table)));
        assertThat(exception.getMessage(),
                containsString("Attention! Using literals and constant expressions with datatype "
                        + "`TIMESTAMP WITH LOCAL TIME ZONE` in Virtual Schemas can produce an incorrect results"));
    }

    /**
     * The CastAssertionBuilder is a convenience class that helps formulating cast assertions in a more readable way.
     */
    static class CastAssertionBuilder {
        private final String fromType;
        private String castToType;
        private String acceptType;
        private Object[] inputValues;
        private final AbstractExasolSqlDialectIT parent;

        public CastAssertionBuilder(final AbstractExasolSqlDialectIT parent, final String from) {
            this.parent = parent;
            this.fromType = from;
        }

        public CastAssertionBuilder to(final String castToType) {
            this.castToType = castToType;
            return this;
        }

        public CastAssertionBuilder accept(final String acceptedType) {
            this.acceptType = acceptedType;
            return this;
        }

        public CastAssertionBuilder input(final Object... input) {
            this.inputValues = input;
            return this;
        }

        public void verify(final Object... expectedValues) {
            final Table table = prepareInputTable();
            this.parent.virtualSchema = this.parent.createVirtualSchema(this.parent.sourceSchema);
            final Builder expectedTable = prepareExpectation(expectedValues);
            this.parent.assertVsQuery(createCastQuery(table), expectedTable.matches());
        }

        private Table prepareInputTable() {
            final Table table = this.parent.createSingleColumnTable(this.fromType);
            for (final Object sourceValue : this.inputValues) {
                table.insert(sourceValue);
            }
            return table;
        }

        private Builder prepareExpectation(final Object... expectedValues) {
            this.acceptType = this.acceptType == null //
                    ? deriveResultTypeFromCastType(this.castToType) //
                    : this.acceptType;
            final Builder expectedTable = table(this.acceptType);
            for (final Object expectedValue : expectedValues) {
                expectedTable.row(expectedValue);
            }
            return expectedTable;
        }

        private String deriveResultTypeFromCastType(final String castToType) {
            return castToType.replaceAll("\\(.*?\\)", "").replaceAll(" +", " ").trim();
        }

        private String createCastQuery(final Table table) {
            return "SELECT CAST(" + this.parent.getColumnName(table, 0) + " AS " + this.castToType + ") FROM "
                    + this.parent.getVirtualTableName(this.parent.virtualSchema, table);
        }
    }
}