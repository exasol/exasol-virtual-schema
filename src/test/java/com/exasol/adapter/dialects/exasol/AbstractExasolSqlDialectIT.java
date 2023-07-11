package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.dialects.exasol.ExasolSqlDialect.EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH;
import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.PATH_TO_VIRTUAL_SCHEMAS_JAR;
import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION;
import static com.exasol.dbbuilder.dialects.exasol.ExasolObjectPrivilege.SELECT;
import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import org.testcontainers.containers.JdbcDatabaseContainer.NoDriverFoundException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.exasol.bucketfs.Bucket;
import com.exasol.bucketfs.BucketAccessException;
import com.exasol.containers.ExasolContainer;
import com.exasol.containers.ExasolDockerImageReference;
import com.exasol.dbbuilder.dialects.*;
import com.exasol.dbbuilder.dialects.exasol.*;
import com.exasol.dbbuilder.dialects.exasol.AdapterScript.Language;
import com.exasol.matcher.ResultSetStructureMatcher.Builder;
import com.exasol.matcher.TypeMatchMode;

@Tag("integration")
@Testcontainers
abstract class AbstractExasolSqlDialectIT {
    private static final String COLUMN1_NAME = "C1";

    @Container
    protected static final ExasolContainer<? extends ExasolContainer<?>> EXASOL = new ExasolContainer<>(
            IntegrationTestConfiguration.getDockerImageReference()).withReuse(true);
    private static ExasolSchema adapterSchema;
    protected static ExasolObjectFactory objectFactory;
    protected static Connection connection;
    protected static AdapterScript adapterScript;
    protected ExasolSchema sourceSchema;
    protected User user;
    protected VirtualSchema virtualSchema;
    private ConnectionDefinition jdbcConnection;
    private final Set<String> expectVarcharFor = expectVarcharFor();

    @BeforeAll
    static void beforeAll() throws BucketAccessException, TimeoutException, NoDriverFoundException, SQLException,
            FileNotFoundException {
        connection = EXASOL.createConnection("");
        objectFactory = new ExasolObjectFactory(connection);
        adapterSchema = objectFactory.createSchema("ADAPTER_SCHEMA");
        adapterScript = installVirtualSchemaAdapter(adapterSchema);
    }

    private static AdapterScript installVirtualSchemaAdapter(final ExasolSchema adapterSchema)
            throws BucketAccessException, TimeoutException, FileNotFoundException {
        final Bucket bucket = EXASOL.getDefaultBucket();
        bucket.uploadFile(PATH_TO_VIRTUAL_SCHEMAS_JAR, VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION);
        return adapterSchema.createAdapterScriptBuilder("EXASOL_ADAPTER") //
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
        final String jdbcUrl = getJdbcUrl();
        return objectFactory.createConnectionDefinition("JDBC_CONNECTION", jdbcUrl, user.getName(), user.getPassword());
    }

    private String getJdbcUrl() {
        final int port = EXASOL.getDefaultInternalDatabasePort();
        final String fingerprint = EXASOL.getTlsCertificateFingerprint().orElseThrow();
        return "jdbc:exa:localhost/" + fingerprint + ":" + port;
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

    @Test
    void testVarcharMappingUtf8() {
        final Table table = createSingleColumnTable("VARCHAR(20) UTF8").insert("Hello world!").insert("Grüße!");
        assertVirtualTableContents(table, table("VARCHAR").row("Hello world!").row("Grüße!").matches());
    }

    protected Table createSingleColumnTable(final String sourceType) {
        final String typeAsIdentifier = sourceType.replaceAll("[ ,]", "_").replaceAll("[()]", "");
        return this.sourceSchema.createTable("SINGLE_COLUMN_TABLE_" + typeAsIdentifier, COLUMN1_NAME, sourceType);
    }

    protected void assertVirtualTableContents(final Table table, final Matcher<ResultSet> matcher) {
        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        try {
            assertThat(selectAllFromCorrespondingVirtualTable(virtualSchema, table), matcher);
        } catch (final SQLException exception) {
            throw new AssertionFailedError("Unable to execute assertion query for table " + table.getName(), exception);
        } finally {
            virtualSchema.drop();
        }
    }

    protected VirtualSchema createVirtualSchema(final Schema sourceSchema) {
        return objectFactory.createVirtualSchemaBuilder("THE_VS") //
                .sourceSchema(sourceSchema) //
                .adapterScript(adapterScript) //
                .connectionDefinition(this.jdbcConnection) //
                .properties(getVirtualSchemaProperties()) //
                .build();
    }

    private Map<String, String> getVirtualSchemaProperties() {
        return getConnectionSpecificVirtualSchemaProperties();
    }

    protected ResultSet selectAllFromCorrespondingVirtualTable(final VirtualSchema virtualSchema, final Table table)
            throws SQLException {
        return selectAllFrom(getVirtualTableName(virtualSchema, table));
    }

    private ResultSet selectAllFrom(final String tableName) throws SQLException {
        return query("SELECT * FROM " + tableName);
    }

    private String getVirtualTableName(final VirtualSchema virtualSchema, final Table table) {
        return virtualSchema.getFullyQualifiedName() + ".\"" + table.getName() + "\"";
    }

    protected ResultSet query(final String sqlFormatString, final Object... args) throws SQLException {
        return query(MessageFormat.format(sqlFormatString, args));
    }

    protected ResultSet query(final String sql) throws SQLException {
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (final SQLException exception) {
            throw new SQLException("Error executing '" + sql + "': " + exception.getMessage(), exception);
        }
    }

    @Test
    void testVarcharMappingAscii() {
        final Table table = createSingleColumnTable("VARCHAR(20) ASCII").insert("Hello").insert("world");
        assertVirtualTableContents(table, table("VARCHAR").row("Hello").row("world").matches());
    }

    void verifyCharMappingUtf8(final String expectedColumnType) {
        final Table table = createSingleColumnTable("CHAR(20) UTF8").insert("Howdy.").insert("Grüzi.");
        assertVirtualTableContents(table,
                table(expectedColumnType).row(pad("Howdy.", 20)).row(pad("Grüzi.", 20)).matches());
    }

    @Test
    void testCharMappingUtf8() {
        verifyCharMappingUtf8("CHAR");
    }

    /**
     * Append {@code padTo} space characters {@code ""} to the given {@code text}
     *
     * @param text  the text to pad
     * @param padTo the number of spaces to append
     * @return the padded text
     */
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
                .matches(TypeMatchMode.NO_JAVA_TYPE_CHECK)); // required because the JDBC driver turns DECIMAL into
                                                             // BigInteger
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
        // Note that the JDBC driver reports the result as VARCHAR for Exasol database with major version < 8
        final Table table = createSingleColumnTable("GEOMETRY").insert("POINT (2 3)");
        assertVirtualTableContents(table, table(expectDataType("GEOMETRY")).row("POINT (2 3)").matches());
    }

    @Test
    void testDecimalLiteral() {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT 10.2 FROM " + getVirtualTableName(this.virtualSchema, table),
                table("DECIMAL").row(BigDecimal.valueOf(10.2)).matches());
    }

    @Test
    void testDoubleLiteral() {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT CAST(10.2 as DOUBLE) FROM " + getVirtualTableName(this.virtualSchema, table),
                table("DOUBLE PRECISION").row(10.2).matches());
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
            throw new AssertionFailedError("Unable to run assertion query: '" + sql + "'", exception);
        }
    }

    @Test
    void testSelectingUnquotedMixedCaseTableThrowsException() {
        final Table table = this.sourceSchema.createTable("MixedCaseTable", COLUMN1_NAME, "BOOLEAN").insert(true);
        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        final String virtualTableUnquotedName = virtualSchema.getFullyQualifiedName() + "." + table.getName();
        assertQueryFailsWithErrorMatching("SELECT * FROM " + virtualTableUnquotedName, ".*object.*not found.*");
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
    void testCastVarcharAsDecimal() {
        castFrom("VARCHAR(6)").to("DECIMAL(5,1)").input("1234.5").verify(BigDecimal.valueOf(1234.5));
    }

    @Test
    void testCastVarcharAsDouble() {
        castFrom("VARCHAR(6)").to("DOUBLE PRECISION").input("1234.5").verify(1234.5);
    }

    @Test
    void testCastVarcharAsGeometry() {
        castFrom("VARCHAR(20)").to("GEOMETRY(5)").input("POINT(2 5)").accept(expectDataType("GEOMETRY"))
                .verify("POINT (2 5)");
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
        this.sourceSchema.createTable("TL", COLUMN1_NAME, "VARCHAR(2)", "C2", "VARCHAR(2)") //
                .insert("K1", "L1").insert(null, "L2").insert("K3", "L3");
        this.sourceSchema.createTable("TR", COLUMN1_NAME, "VARCHAR(2)", "C2", "VARCHAR(2)") //
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
    void createVirtualSchemaWithNonexistentConnectionThrowsException() {
        assertQueryFailsWithErrorContaining("CREATE VIRTUAL SCHEMA VIRTUAL_SCHEMA_NONEXISTENT_CONNECTION\n" //
                + "USING " + adapterScript.getFullyQualifiedName() + " WITH\n" //
                + "CONNECTION_NAME = 'NONEXISTENT_CONNECTION'\n" //
                + "SCHEMA_NAME = '" + this.sourceSchema.getFullyQualifiedName() + "' ", //
                "Could not access the connection information of connection 'NONEXISTENT_CONNECTION'");
    }

    private void assertQueryFailsWithErrorContaining(final String sql, final String expectedErrorMessage) {
        final SQLException exception = assertThrows(SQLException.class, () -> runQueryExpectedToFail(sql));
        assertThat(exception.getMessage(), containsString(expectedErrorMessage));
    }

    private void assertQueryFailsWithErrorMatching(final String sql, final String expectedErrorMessage) {
        final SQLException exception = assertThrows(SQLException.class, () -> runQueryExpectedToFail(sql));
        assertThat(exception.getMessage(), matchesPattern(expectedErrorMessage));
    }

    // While the query run here is expected to fail, we still wrap it so that should it accidentally succeed, the
    // result set is correctly closed.
    private void runQueryExpectedToFail(final String sql) throws SQLException {
        final ResultSet irrelevant = query(sql);
        irrelevant.close();
    }

    @Test
    void testCreateVirtualSchemaWithIgnoreErrorsProperty() throws SQLException {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        final Map<String, String> properties = new HashMap<>(getConnectionSpecificVirtualSchemaProperties());
        properties.put("IGNORE_ERRORS", EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH);
        this.virtualSchema = objectFactory.createVirtualSchemaBuilder("VIRTUAL_SCHEMA_IGNORES_ERRORS") //
                .sourceSchema(this.sourceSchema) //
                .adapterScript(adapterScript) //
                .properties(properties) //
                .connectionDefinition(this.jdbcConnection) //
                .build();
        assertThat(query("SELECT NOW() - INTERVAL '1' MINUTE FROM " + getVirtualTableName(this.virtualSchema, table)),
                instanceOf(ResultSet.class));
    }

    @Test
    void testCreateVirtualSchemaWithoutIgnoreErrorsPropertyThrowsException() {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertQueryFailsWithErrorContaining(
                "SELECT NOW() - INTERVAL '1' MINUTE FROM " + getVirtualTableName(this.virtualSchema, table), //
                "Attention! Using literals and constant expressions with datatype " //
                        + "`TIMESTAMP WITH LOCAL TIME ZONE` in Virtual Schemas can produce incorrect results.");
    }

    // SELECT * tests
    @ParameterizedTest
    @ValueSource(strings = { "SELECT *", "SELECT BOOL_COL, VARCHAR_COL, DECIMAL_COL" })
    void testSelectAllColumnsWithExplicitSelectList(final String select) {
        final Table table = this.sourceSchema.createTable("TEST_TABLE", "BOOL_COL", "BOOLEAN", "VARCHAR_COL",
                "VARCHAR(100)", "DECIMAL_COL", "DECIMAL(18,0)");
        table.insert(true, "varchar_1", 10);
        table.insert(false, "varchar_2", -10);
        createVirtualSchemaWithoutSelectListProjectionCapability();
        assertVsQuery(select + " FROM " + getVirtualTableName(this.virtualSchema, table), //
                table() //
                        .row(true, "varchar_1", 10) //
                        .row(false, "varchar_2", -10) //
                        .matches(TypeMatchMode.NO_JAVA_TYPE_CHECK));
    }

    private void createVirtualSchemaWithoutSelectListProjectionCapability() {
        final Map<String, String> properties = new HashMap<>(getConnectionSpecificVirtualSchemaProperties());
        properties.put("EXCLUDED_CAPABILITIES", "SELECTLIST_PROJECTION");
        this.virtualSchema = objectFactory
                .createVirtualSchemaBuilder("VIRTUAL_SCHEMA_WITHOUT_SELECT_LIST_PROJECTION_CAPABILITY") //
                .sourceSchema(this.sourceSchema) //
                .adapterScript(adapterScript) //
                .properties(properties) //
                .connectionDefinition(this.jdbcConnection) //
                .build();
    }

    @Test
    void testSelectStarConvertedToColumnsListJoinSameTable() {
        final Table table = this.sourceSchema.createTable("TL", "L1", "VARCHAR(5)", "L2", "VARCHAR(5)");
        table.insert("L1_1", "L2_1");
        table.insert("L1_2", "L2_2");
        createVirtualSchemaWithoutSelectListProjectionCapability();
        assertVsQuery("SELECT * FROM TL JOIN TL AS TL_2 ON TL.L1 = TL_2.L1 ORDER BY TL.L1", //
                table() //
                        .row("L1_1", "L2_1", "L1_1", "L2_1") //
                        .row("L1_2", "L2_2", "L1_2", "L2_2") //
                        .matches());
    }

    @Test
    void testSelectStarConvertedToColumnsListJoinSameTableReversed() {
        final Table table = this.sourceSchema.createTable("TL", "L1", "VARCHAR(5)", "L2", "VARCHAR(5)");
        table.insert("L1_1", "L2_1");
        table.insert("L1_2", "L2_2");
        createVirtualSchemaWithoutSelectListProjectionCapability();
        assertVsQuery("SELECT * FROM TL AS TL_2 JOIN TL ON TL_2.L1 = TL.L1 ORDER BY TL_2.L1", //
                table() //
                        .row("L1_1", "L2_1", "L1_1", "L2_1") //
                        .row("L1_2", "L2_2", "L1_2", "L2_2") //
                        .matches());
    }

    @Test
    void testSelectStarConvertedToColumnsListJoin() {
        final Table tableLeft = this.sourceSchema.createTable("TL", "L1", "VARCHAR(5)", "L2", "VARCHAR(5)");
        final Table tableRight = this.sourceSchema.createTable("TR", "R1", "VARCHAR(5)", "R2", "VARCHAR(5)", "R3",
                "VARCHAR(5)");
        tableLeft.insert("ON", "L2_1");
        tableLeft.insert("ON", "L2_2");
        tableRight.insert("ON", "R2_1", "R3_1");
        tableRight.insert("ON", "R2_2", "R3_2");
        createVirtualSchemaWithoutSelectListProjectionCapability();
        assertVsQuery("SELECT * FROM TL JOIN TR ON TL.L1 = TR.R1 ORDER BY L2, R2", //
                table() //
                        .row("ON", "L2_1", "ON", "R2_1", "R3_1") //
                        .row("ON", "L2_1", "ON", "R2_2", "R3_2") //
                        .row("ON", "L2_2", "ON", "R2_1", "R3_1") //
                        .row("ON", "L2_2", "ON", "R2_2", "R3_2") //
                        .matches());
    }

    @Test
    void testSelectStarConvertedToColumnsListJoinReversed() {
        final Table tableLeft = this.sourceSchema.createTable("TL", "L1", "VARCHAR(5)", "L2", "VARCHAR(5)");
        final Table tableRight = this.sourceSchema.createTable("TR", "R1", "VARCHAR(5)", "R2", "VARCHAR(5)", "R3",
                "VARCHAR(5)");
        tableLeft.insert("ON", "L2_1");
        tableLeft.insert("ON", "L2_2");
        tableRight.insert("ON", "R2_1", "R3_1");
        tableRight.insert("ON", "R2_2", "R3_2");
        createVirtualSchemaWithoutSelectListProjectionCapability();
        assertVsQuery("SELECT * FROM TR JOIN TL ON TL.L1 = TR.R1 ORDER BY R2, L2", //
                table() //
                        .row("ON", "R2_1", "R3_1", "ON", "L2_1") //
                        .row("ON", "R2_1", "R3_1", "ON", "L2_2") //
                        .row("ON", "R2_2", "R3_2", "ON", "L2_1") //
                        .row("ON", "R2_2", "R3_2", "ON", "L2_2") //
                        .matches());
    }

    @Test
    void testSelectStarConvertedToColumnsListNestedJoin() {
        final Table tableLeft = this.sourceSchema.createTable("TL", "L1", "VARCHAR(5)", "L2", "VARCHAR(5)");
        final Table tableRight = this.sourceSchema.createTable("TR", "R1", "VARCHAR(5)", "R2", "VARCHAR(5)", "R3",
                "VARCHAR(5)");
        final Table tableMiddle = this.sourceSchema.createTable("TM", List.of("M1", "M2", "M3", "M4"),
                List.of("VARCHAR(5)", "VARCHAR(5)", "VARCHAR(5)", "VARCHAR(5)"));
        tableLeft.insert("ON", "L2_1");
        tableLeft.insert("ON", "L2_2");
        tableRight.insert("ON", "R2_1", "R3_1");
        tableRight.insert("ON", "R2_2", "R3_2");
        tableMiddle.insert("ON", "M2_1", "M3_1", "M4_1");
        tableMiddle.insert("ON", "M2_2", "M3_2", "M4_2");
        createVirtualSchemaWithoutSelectListProjectionCapability();
        assertVsQuery(
                "SELECT * FROM TM JOIN (SELECT * FROM TR JOIN TL ON TL.L1 = TR.R1) nested ON nested.R1 = TM.M1 ORDER BY M2, R2, L2", //
                table() //
                        .row("ON", "M2_1", "M3_1", "M4_1", "ON", "R2_1", "R3_1", "ON", "L2_1") //
                        .row("ON", "M2_1", "M3_1", "M4_1", "ON", "R2_1", "R3_1", "ON", "L2_2") //
                        .row("ON", "M2_1", "M3_1", "M4_1", "ON", "R2_2", "R3_2", "ON", "L2_1") //
                        .row("ON", "M2_1", "M3_1", "M4_1", "ON", "R2_2", "R3_2", "ON", "L2_2") //
                        .row("ON", "M2_2", "M3_2", "M4_2", "ON", "R2_1", "R3_1", "ON", "L2_1") //
                        .row("ON", "M2_2", "M3_2", "M4_2", "ON", "R2_1", "R3_1", "ON", "L2_2") //
                        .row("ON", "M2_2", "M3_2", "M4_2", "ON", "R2_2", "R3_2", "ON", "L2_1") //
                        .row("ON", "M2_2", "M3_2", "M4_2", "ON", "R2_2", "R3_2", "ON", "L2_2") //
                        .matches());
    }

    @Test
    void testSelectStarConvertedToColumnsListNestedJoinReversed() {
        final Table tableLeft = this.sourceSchema.createTable("TL", "L1", "VARCHAR(5)", "L2", "VARCHAR(5)");
        final Table tableRight = this.sourceSchema.createTable("TR", "R1", "VARCHAR(5)", "R2", "VARCHAR(5)", "R3",
                "VARCHAR(5)");
        final Table tableMiddle = this.sourceSchema.createTable("TM", List.of("M1", "M2", "M3", "M4"),
                List.of("VARCHAR(5)", "VARCHAR(5)", "VARCHAR(5)", "VARCHAR(5)"));
        tableLeft.insert("ON", "L2_1");
        tableLeft.insert("ON", "L2_2");
        tableRight.insert("ON", "R2_1", "R3_1");
        tableRight.insert("ON", "R2_2", "R3_2");
        tableMiddle.insert("ON", "M2_1", "M3_1", "M4_1");
        tableMiddle.insert("ON", "M2_2", "M3_2", "M4_2");
        createVirtualSchemaWithoutSelectListProjectionCapability();
        assertVsQuery(
                "SELECT * FROM (SELECT * FROM TR JOIN TL ON TL.L1 = TR.R1) nested JOIN TM ON TM.M1 = nested.R1 ORDER BY R2, L2, M2", //
                table() //
                        .row("ON", "R2_1", "R3_1", "ON", "L2_1", "ON", "M2_1", "M3_1", "M4_1") //
                        .row("ON", "R2_1", "R3_1", "ON", "L2_1", "ON", "M2_2", "M3_2", "M4_2") //
                        .row("ON", "R2_1", "R3_1", "ON", "L2_2", "ON", "M2_1", "M3_1", "M4_1") //
                        .row("ON", "R2_1", "R3_1", "ON", "L2_2", "ON", "M2_2", "M3_2", "M4_2") //
                        .row("ON", "R2_2", "R3_2", "ON", "L2_1", "ON", "M2_1", "M3_1", "M4_1") //
                        .row("ON", "R2_2", "R3_2", "ON", "L2_1", "ON", "M2_2", "M3_2", "M4_2") //
                        .row("ON", "R2_2", "R3_2", "ON", "L2_2", "ON", "M2_1", "M3_1", "M4_1") //
                        .row("ON", "R2_2", "R3_2", "ON", "L2_2", "ON", "M2_2", "M3_2", "M4_2") //
                        .matches());
    }

    @Test
    void testDefaultHashType() {
        typeAssertionFor("HASHTYPE").withValue("550e8400-e29b-11d4-a716-446655440000")
                .expectDescribeType("HASHTYPE(16 BYTE)") //
                .expectTypeOf("HASHTYPE(16 BYTE)") //
                .expectResultSetType(expectDataType("HASHTYPE")) //
                .expectValue("550e8400e29b11d4a716446655440000") //
                .runAssert();
    }

    @Test
    void testNonDefaultHashType() {
        typeAssertionFor("HASHTYPE(4 BYTE)").withValue("550e8400") //
                .expectDescribeType("HASHTYPE(4 BYTE)") //
                .expectTypeOf("HASHTYPE(4 BYTE)") //
                .expectResultSetType(expectDataType("HASHTYPE")) //
                .runAssert();
    }

    @Test
    void testHashTypeWithBitSize() {
        typeAssertionFor("HASHTYPE(16 BIT)").withValue("550e") //
                .expectDescribeType("HASHTYPE(2 BYTE)") //
                .expectTypeOf("HASHTYPE(2 BYTE)") //
                .expectResultSetType(expectDataType("HASHTYPE")) //
                .runAssert();
    }

    @Test
    void testDefaultGeometry() {
        typeAssertionFor("GEOMETRY").withValue("POINT (2 5)") //
                .expectTypeOf("GEOMETRY") //
                .expectDescribeType("GEOMETRY") //
                .expectResultSetType(expectDataType("GEOMETRY")) //
                .runAssert();
    }

    @Test
    void testNonDefaultGeometry() {
        typeAssertionFor("GEOMETRY(4321)").withValue("POINT (2 5)") //
                .expectTypeOf("GEOMETRY(4321)") //
                .expectDescribeType("GEOMETRY(4321)") //
                .expectResultSetType(expectDataType("GEOMETRY")) //
                .runAssert();
    }

    @Test
    void testDefaultIntervalYearToMonth() {
        typeAssertionFor("INTERVAL YEAR TO MONTH").withValue("5-3") //
                .expectTypeOf("INTERVAL YEAR(2) TO MONTH") //
                .expectDescribeType("INTERVAL YEAR(2) TO MONTH") //
                .expectResultSetType(expectDataType("INTERVAL YEAR TO MONTH")) //
                .expectValue("+05-03") //
                .runAssert();
    }

    @Test
    void testNonDefaultIntervalYearToMonth() {
        typeAssertionFor("INTERVAL YEAR(4) TO MONTH") // 4 digits for year
                .withValue("5-3") // sample interval of 5 years and 3 months
                .expectTypeOf("INTERVAL YEAR(4) TO MONTH") //
                .expectDescribeType("INTERVAL YEAR(4) TO MONTH") //
                .expectResultSetType(expectDataType("INTERVAL YEAR TO MONTH")) //
                .expectValue("+0005-03") // 4 digits for year
                .runAssert();
    }

    @Test
    void testDefaultIntervalDayToSecond() {
        typeAssertionFor("INTERVAL DAY TO SECOND").withValue("2 12:50:10.123") //
                // day: 2 digits, seconds: 3 digits after decimal point
                .expectTypeOf("INTERVAL DAY(2) TO SECOND(3)") //
                .expectDescribeType("INTERVAL DAY(2) TO SECOND(3)") //
                .expectResultSetType(expectDataType("INTERVAL DAY TO SECOND")) //
                .expectValue("+02 12:50:10.123") //
                .runAssert();
    }

    @Test
    void testNonDefaultIntervalDayToSecond() {
        // day: 4 digits, seconds: 6 digits after decimal point
        typeAssertionFor("INTERVAL DAY(4) TO SECOND(6)").withValue("2 12:50:10.123") //
                .expectTypeOf("INTERVAL DAY(4) TO SECOND(6)") //
                .expectDescribeType("INTERVAL DAY(4) TO SECOND(6)") //
                .expectResultSetType(expectDataType("INTERVAL DAY TO SECOND")) //
                .expectValue("+0002 12:50:10.123000") //
                .runAssert();
    }

    @Test
    void testCurrentClusterFunction() throws SQLException {
        final Table table = createSingleColumnTable("VARCHAR(20) UTF8").insert("_cluster");
        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        try {
            final String sql = "select CURRENT_CLUSTER || " + COLUMN1_NAME + " from "
                    + this.sourceSchema.getFullyQualifiedName() + ".\"" + table.getName() + "\"";
            assertThat(query(sql), table("VARCHAR").row("MAIN_cluster").matches());
        } finally {
            table.drop();
            virtualSchema.drop();
        }
    }

    @Test
    void testWildcards() throws SQLException {
        final String nameWithWildcard = "A_A";
        this.sourceSchema.createTable(nameWithWildcard, "A", "VARCHAR(20)");
        this.sourceSchema.createTable("AXA", "X", "VARCHAR(20)");
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("describe " + this.virtualSchema.getFullyQualifiedName() + ".\"" + nameWithWildcard + "\"",
                table().row("A", "VARCHAR(20) UTF8", null, null, null).matches());
    }

    boolean isVersionOrHigher(final int majorVersion, final int minorVersion, final int fixVersion) {
        final ExasolDockerImageReference version = EXASOL.getDockerImageReference();
        final long comparableImageVersion = calculatedComparableVersion((version.hasMajor() ? version.getMajor() : 0),
                (version.hasMinor() ? version.getMinor() : 0), (version.hasFix() ? version.getFixVersion() : 0));
        final long comparableRequiredVersion = calculatedComparableVersion(majorVersion, minorVersion, fixVersion);
        return comparableImageVersion >= comparableRequiredVersion;
    }

    private static long calculatedComparableVersion(final int majorVersion, final int minorVersion,
            final int fixVersion) {
        return (majorVersion * 1000000L) + (minorVersion * 1000L) + fixVersion;
    }

    protected com.exasol.adapter.dialects.exasol.DataTypeAssertion.Builder typeAssertionFor(final String columnType) {
        return DataTypeAssertion.builder(this).withColumnType(columnType);
    }

    void assertVirtualSchemaTypes(final DataTypeAssertion assertion) {
        final Table table = createSingleColumnTable(assertion.getColumnType()).insert(assertion.getValue());
        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        try {
            assertAll( //
                    () -> assertTypeofColumn(virtualSchema, table, assertion.getExpectedTypeOf()),
                    () -> assertDescribeColumnType(virtualSchema, table, assertion.getExpectedDescribeType()),
                    () -> assertResultSetType(virtualSchema, table, assertion.getExpectedResultSetType(),
                            assertion.getExpectedValue()));
        } finally {
            virtualSchema.drop();
            table.drop();
        }
    }

    private void assertResultSetType(final VirtualSchema virtualSchema, final Table table, final String expectedType,
            final Object expectedValue) throws SQLException {
        try (final ResultSet result = query(
                "SELECT " + COLUMN1_NAME + " FROM " + getVirtualTableName(virtualSchema, table))) {
            assertThat("ResultSet type and value", result, table(expectedType).row(expectedValue).matches());
        }
    }

    private void assertDescribeColumnType(final VirtualSchema virtualSchema, final Table table,
            final String expectedType) throws SQLException {
        try (final ResultSet result = query("DESCRIBE " + getVirtualTableName(virtualSchema, table))) {
            assertTrue(result.next(), "DESCRIBE query did not return any rows");
            final String columnName = result.getString("COLUMN_NAME");
            assertThat(columnName, equalTo(table.getColumns().get(0).getName()));
            final String actualType = result.getString("SQL_TYPE");
            assertThat("DESCRIBE column type", actualType, equalTo(expectedType));
        }
    }

    private void assertTypeofColumn(final VirtualSchema virtualSchema, final Table table, final String expectedType)
            throws SQLException {
        if (!typeofFunctionSupported()) {
            return;
        }
        try (final ResultSet result = query(
                "SELECT TYPEOF(" + COLUMN1_NAME + ") AS TYPE FROM " + getVirtualTableName(virtualSchema, table))) {
            assertThat("TYPEOF result", result, table("VARCHAR").row(expectedType).matches());
        }
    }

    private boolean typeofFunctionSupported() {
        final ExasolDockerImageReference version = EXASOL.getDockerImageReference();
        return ((version.getMajor() == 7) && (version.getMinor() >= 1)) || (version.getMajor() > 7);
    }

    protected String expectDataType(final String original) {
        return this.expectVarcharFor.contains(original) ? "VARCHAR" : original;
    }

    /**
     * Provide the Virtual Schema properties that switch between different connection variants.
     *
     * @return raw properties
     */
    protected abstract Map<String, String> getConnectionSpecificVirtualSchemaProperties();

    /**
     * @return Set of strings with data type names to expect VARCHAR for in tests
     */
    protected abstract Set<String> expectVarcharFor();

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
