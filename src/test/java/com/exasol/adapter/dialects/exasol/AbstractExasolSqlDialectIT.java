package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.dialects.exasol.ExasolSqlDialect.EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH;
import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.PATH_TO_VIRTUAL_SCHEMAS_JAR;
import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION;
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
import java.util.*;
import java.util.concurrent.TimeoutException;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.*;
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
    public static final String EXASOL_DIALECT = "EXASOL";
    @Container
    protected static final ExasolContainer<? extends ExasolContainer<?>> CONTAINER = new ExasolContainer<>(
            IntegrationTestConfiguration.getDockerImageReference()).withReuse(true);
    protected static ExasolObjectFactory objectFactory;
    private static Connection connection;
    private static ConnectionDefinition jdbcConnection;
    private static AdapterScript adapterScript;
    private ExasolSchema sourceSchema;
    private VirtualSchema virtualSchema;

    @BeforeAll
    static void beforeAll() throws BucketAccessException, InterruptedException, TimeoutException, IOException,
            NoDriverFoundException, SQLException {
        connection = CONTAINER.createConnection("");
        objectFactory = new ExasolObjectFactory(connection);
        createAdapterConnectionDefinition();
        final ExasolSchema adapterSchema = objectFactory.createSchema("ADAPTER_SCHEMA");
        installVirtualSchemaAdapter(adapterSchema);
    }

    private static void createAdapterConnectionDefinition() {
        final String jdbcUrl = "jdbc:exa:localhost:" + CONTAINER.getDefaultInternalDatabasePort();
        jdbcConnection = objectFactory.createConnectionDefinition("JDBC_CONNECTION", jdbcUrl, CONTAINER.getUsername(),
                CONTAINER.getPassword());
    }

    private static void installVirtualSchemaAdapter(final ExasolSchema adapterSchema)
            throws InterruptedException, BucketAccessException, TimeoutException {
        final Bucket bucket = CONTAINER.getDefaultBucket();
        bucket.uploadFile(PATH_TO_VIRTUAL_SCHEMAS_JAR, VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION);
        adapterScript = adapterSchema.createAdapterScriptBuilder() //
                .name("EXASOL_ADAPTER") //
                .language(Language.JAVA) //
                .bucketFsContent("com.exasol.adapter.RequestDispatcher",
                        "/buckets/bfsdefault/default/" + VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION)
                .build();
    }

    @AfterAll
    static void afterAll() throws SQLException {
        connection.close();
        CONTAINER.stop();
    }

    @BeforeEach
    void beforeEach() {
        this.sourceSchema = objectFactory.createSchema("SOURCE_SCHEMA");
        this.virtualSchema = null;
    }

    @AfterEach
    void afterEach() {
        dropAll(this.sourceSchema, this.virtualSchema);
        this.sourceSchema = null;
        this.virtualSchema = null;
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
    protected abstract Map<String, String> getConnectionSepcificVirtualSchemaProperties();

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

    private VirtualSchema createVirtualSchema(final Schema sourceSchema) {
        return objectFactory.createVirtualSchemaBuilder("THE_VS").dialectName(EXASOL_DIALECT) //
                .sourceSchema(sourceSchema) //
                .adapterScript(adapterScript) //
                .connectionDefinition(jdbcConnection) //
                .properties(getConnectionSepcificVirtualSchemaProperties()) //
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

    private ResultSet query(final String sql) throws SQLException {
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

    private String pad(final String text, final int padTo) {
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
        mixedCaseSchema = objectFactory.createSchema("MixedCaseSchema");
        final Table table = mixedCaseSchema.createTable("MixedCaseTable", "Column1", "VARCHAR(20)", "column2",
                "VARCHAR(20)", "COLUMN3", "VARCHAR(20)").insert("foo", "bar", "baz");
        this.virtualSchema = createVirtualSchema(mixedCaseSchema);
        assertVsQuery("SELECT \"Column1\", \"column2\", COLUMN3 FROM " + getVirtualTableName(this.virtualSchema, table),
                table().row("foo", "bar", "baz").matches());
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

    @Test
    void testGroupConcat() {
        final Table table = createSingleColumnTable("INTEGER").insert(101).insert(102).insert(103).insert(104);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT GROUP_CONCAT(" + getColumnName(table, 0) + ") FROM "
                + getVirtualTableName(this.virtualSchema, table), table().row("101,102,103,104").matches());
    }

    private String getColumnName(final Table table, final int index) {
        return table.getColumns().get(index).getName();
    }

    @Test
    void testGroupConcatOrderBy() {
        final Table table = this.sourceSchema.createTable("GROUP_CONCAT_TABLE", "A", "INTEGER", "B", "INTEGER")
                .insert(101, 3).insert(102, 1).insert(103, 2).insert(104, null);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT GROUP_CONCAT(A ORDER BY B) FROM " + getVirtualTableName(this.virtualSchema, table), //
                table().row("102,103,101,104").matches());
    }

    @Test
    void testGroupConcatOrderByDesc() {
        final Table table = this.sourceSchema.createTable("GROUP_CONCAT_TABLE", "A", "INTEGER", "B", "INTEGER")
                .insert(101, 3).insert(102, 1).insert(103, 2).insert(104, null);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT GROUP_CONCAT(A ORDER BY B DESC NULLS LAST) FROM "
                + getVirtualTableName(this.virtualSchema, table), table().row("101,103,102,104").matches());
    }

    @Test
    void testGroupConcatOrderByDescNullsLast() {
        final Table table = this.sourceSchema.createTable("GROUP_CONCAT_TABLE", "A", "INTEGER", "B", "INTEGER")
                .insert(101, 3).insert(102, 1).insert(103, 2).insert(104, null);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT GROUP_CONCAT(A ORDER BY B DESC NULLS LAST) FROM "
                + getVirtualTableName(this.virtualSchema, table), table().row("101,103,102,104").matches());
    }

    @Test
    void testGroupConcatSeparator() {
        final Table table = createSingleColumnTable("INTEGER").insert(101).insert(102).insert(103).insert(104);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT GROUP_CONCAT(" + getColumnName(table, 0) + " SEPARATOR ';'||' ') FROM "
                + getVirtualTableName(this.virtualSchema, table), table().row("101; 102; 103; 104").matches());
    }

    @Test
    void testExtractFromDate() {
        final Table table = createSingleColumnTable("DATE").insert("2000-11-01");
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT EXTRACT(MONTH FROM " + getColumnName(table, 0) + ") FROM "
                + getVirtualTableName(this.virtualSchema, table), table().row((short) 11).matches());
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
        assertCast("VARCHAR(20)", "CHAR(40)", "Hello.", pad("Hello.", 40));
    }

    protected void assertCast(final String sourceType, final String castToType, final Object sourceValue,
            final Object expectedValue) {
        assertCast(sourceType, castToType, deriveResultTypeFromCastType(castToType), sourceValue, expectedValue);
    }

    protected String deriveResultTypeFromCastType(final String castToType) {
        return castToType.replaceAll("\\(.*?\\)", "").replaceAll(" +", " ").trim();
    }

    protected void assertCast(final String sourceType, final String castToType, final String resultType,
            final Object sourceValue, final Object expectedValue) {
        assertCast(sourceType, castToType, resultType, List.of(sourceValue), List.of(expectedValue));
    }

    protected void assertCast(final String sourceType, final String castToType, final List<Object> sourceValues,
            final List<Object> expectedValues) {
        assertCast(sourceType, castToType, deriveResultTypeFromCastType(castToType), sourceValues, expectedValues);
    }

    protected void assertCast(final String sourceType, final String castToType, final String resultType,
            final List<Object> sourceValues, final List<Object> expectedValues) {
        final Table table = createSingleColumnTable(sourceType);
        for (final Object sourceValue : sourceValues) {
            table.insert(sourceValue);
        }
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        final Builder expected = table(resultType);
        for (final Object expectedValue : expectedValues) {
            expected.row(expectedValue);
        }
        assertVsQuery("SELECT CAST(" + getColumnName(table, 0) + " AS " + castToType + ") FROM "
                + getVirtualTableName(this.virtualSchema, table), expected.matches());
    }

    @Test
    void testCastBooleanToVarchar() {
        assertCast("BOOLEAN", "VARCHAR(5)", List.of(true, false), List.of("TRUE", "FALSE"));
    }

    @Test
    void testCastVarcharToDate() {
        assertCast("VARCHAR(30)", "DATE", "2020-10-17", java.sql.Date.valueOf("2020-10-17"));
    }

    @Test
    void testCastVarcharasDecimal() {
        assertCast("VARCHAR(6)", "DECIMAL(5,1)", "1234.5", BigDecimal.valueOf(1234.5));
    }

    @Test
    void testCastVarcharAsDouble() {
        assertCast("VARCHAR(6)", "DOUBLE PRECISION", "1234.5", 1234.5);
    }

    @Test
    void testCastVarcharAsGeometry() {
        assertCast("VARCHAR(20)", "GEOMETRY(5)", "VARCHAR", "POINT(2 5)", "POINT (2 5)");
    }

    @Test
    void testCastVarcharAsTimestamp() {
        final String timestampAsString = "2016-06-01 13:17:02.081";
        assertCast("VARCHAR(30)", "TIMESTAMP", timestampAsString, Timestamp.valueOf(timestampAsString));
    }

    @Test
    void testCastVarcharAsTimestampWithLocalTimezone() {
        final String timestamp = "2017-11-03 14:18:02.081";
        assertCast("VARCHAR(30)", "TIMESTAMP WITH LOCAL TIME ZONE", "TIMESTAMP", timestamp,
                Timestamp.valueOf(timestamp));
    }

    @Test
    void castIntegerAsVarchar() {
        assertCast("INTEGER", "VARCHAR(5)", 12345, "12345");
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

    private void createVirtualSchemaWithTablesForJoinTest() {
        this.sourceSchema.createTable("TL", "C1", "VARCHAR(2)", "C2", "VARCHAR(2)") //
                .insert("K1", "L1").insert(null, "L2").insert("K3", "L3");
        this.sourceSchema.createTable("TR", "C1", "VARCHAR(2)", "C2", "VARCHAR(2)") //
                .insert("K1", "R1").insert("K2", "R2").insert(null, "R3");
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
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
        properties.putAll(getConnectionSepcificVirtualSchemaProperties());
        properties.put("IGNORE_ERRORS", EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH);
        this.virtualSchema = objectFactory.createVirtualSchemaBuilder("VIRTUAL_SCHEMA_IGNORES_ERRORS") //
                .sourceSchema(this.sourceSchema) //
                .adapterScript(adapterScript) //
                .dialectName(EXASOL_DIALECT) //
                .properties(properties) //
                .connectionDefinition(jdbcConnection) //
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
}