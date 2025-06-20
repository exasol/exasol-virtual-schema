package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.dialects.exasol.ExasolSqlDialect.EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH;
import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.PATH_TO_VIRTUAL_SCHEMAS_JAR;
import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION;
import static com.exasol.dbbuilder.dialects.exasol.ExasolObjectPrivilege.SELECT;
import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.exasol.adapter.dialects.exasol.release.ExasolDbVersion;
import com.exasol.bucketfs.Bucket;
import com.exasol.bucketfs.BucketAccessException;
import com.exasol.containers.ExasolContainer;
import com.exasol.containers.ExasolDockerImageReference;
import com.exasol.dbbuilder.dialects.DatabaseObject;
import com.exasol.dbbuilder.dialects.Schema;
import com.exasol.dbbuilder.dialects.Table;
import com.exasol.dbbuilder.dialects.User;
import com.exasol.dbbuilder.dialects.exasol.*;
import com.exasol.dbbuilder.dialects.exasol.AdapterScript.Language;
import com.exasol.matcher.ResultSetStructureMatcher.Builder;
import com.exasol.matcher.TypeMatchMode;
import com.exasol.udfdebugging.UdfTestSetup;
import com.github.dockerjava.api.model.ContainerNetwork;

@Tag("integration")
@Testcontainers
abstract class AbstractExasolSqlDialectIT {
    private static final Logger LOG = Logger.getLogger(AbstractExasolSqlDialectIT.class.getName());
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
    protected VirtualSchema testVirtualSchema;
    private ConnectionDefinition jdbcConnection;
    private final Set<String> expectVarcharFor = expectVarcharFor();

    @BeforeAll
    static void beforeAll() {
        try {
            connection = EXASOL.createConnection("");
            final UdfTestSetup udfTestSetup = new UdfTestSetup(getTestHostIpFromInsideExasol(),
                    EXASOL.getDefaultBucket(), connection);
            objectFactory = new ExasolObjectFactory(connection,
                    ExasolObjectConfiguration.builder().withJvmOptions(udfTestSetup.getJvmOptions()).build());
            adapterSchema = objectFactory.createSchema("ADAPTER_SCHEMA");
            adapterScript = installVirtualSchemaAdapter(adapterSchema);

            setNlsTimestampFormat();
        } catch (final SQLException | BucketAccessException | TimeoutException | FileNotFoundException exception) {
            throw new IllegalStateException("Failed to created test setup.", exception);
        }
    }

    /**
     * If the Exasol database version supports fractional‐second precision beyond 3 digits,
     * configures the system‐wide default timestamp format to include up to 9 fractional digits.
     * <p>
     * This issues an ALTER SYSTEM command, so it must be run by a user with the appropriate
     * system‐wide privileges. All subsequent sessions (including UDF/Virtual Schema sessions)
     * will inherit this NLS_TIMESTAMP_FORMAT setting.
     *
     * @throws SQLException if executing the ALTER SYSTEM statement fails
     */
    private static void setNlsTimestampFormat() throws SQLException {
        if (supportTimestampPrecision()) {
            modifyQuery("ALTER SYSTEM SET NLS_TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS.FF9'");
            modifyQuery("ALTER SESSION SET NLS_TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS.FF9'");
        }
    }

    /**
     * Determines the host IP address that should be used inside the Exasol container
     * to refer to the test host (typically the machine running the integration tests).
     * <p>
     * This method supports two environments:
     * <ul>
     *     <li><b>CI environments (e.g., GitHub Actions):</b> It retrieves the Docker gateway IP address
     *         from the Exasol container's network settings.</li>
     *     <li><b>Local macOS environments:</b> If the environment variable {@code LOCAL_MACOS_ENV=true}
     *         is set, it attempts to find the first available non-loopback IPv4 address of the local machine.</li>
     * </ul>
     *
     * @return the IP address as seen from inside the Exasol container, or {@code null} if it cannot be determined
     */
    private static String getTestHostIpFromInsideExasol() {
        String localMacosEnv = System.getenv("LOCAL_MACOS_ENV");

        if (localMacosEnv == null || !localMacosEnv.equalsIgnoreCase("true")) {
            // This works inside GitHub Actions container environment
            final Map<String, ContainerNetwork> networks = EXASOL.getContainerInfo().getNetworkSettings().getNetworks();
            if (networks.isEmpty()) {
                return null;
            }
            return networks.values().iterator().next().getGateway();
        } else {
            // Fallback for local machine: find a non-loopback IPv4 address
            try {
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets)) {
                    if (netint.isUp() && !netint.isLoopback()) {
                        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // If everything fails, return null or throw
            return null;
        }
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
    void logTestName(final TestInfo testInfo) {
        LOG.fine(() -> "Running test " + testInfo.getDisplayName() + "...");
    }

    @BeforeEach
    void beforeEach() {
        this.sourceSchema = objectFactory.createSchema("SOURCE_SCHEMA");
        this.user = objectFactory.createLoginUser("VS_USER", "VS_USER_PWD").grant(this.sourceSchema, SELECT);
        this.jdbcConnection = createAdapterConnectionDefinition(this.user);
        this.testVirtualSchema = null;
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
        dropAll(this.testVirtualSchema, this.jdbcConnection, this.user, this.sourceSchema);
        this.testVirtualSchema = null;
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

    /**
     * Get properties for the virtual schema. Note: if you want to enable debug output, you can set <a href=
     * "https://github.com/exasol/test-db-builder-java/blob/main/doc/user_guide/user_guide.md#debug-output">system
     * properties defined by test-db-builder-java</a>.
     * 
     * @return properties for the virtual schema
     */
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

    protected String getVirtualTableName(final VirtualSchema virtualSchema, final Table table) {
        return virtualSchema.getFullyQualifiedName() + ".\"" + table.getName() + "\"";
    }

    protected static ResultSet query(final String sqlFormatString, final Object... args) throws SQLException {
        return query(MessageFormat.format(sqlFormatString, args));
    }

    protected static ResultSet query(final String sql) throws SQLException {
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (final SQLException exception) {
            throw new SQLException("Error executing '" + sql + "': " + exception.getMessage(), exception);
        }
    }

    protected static void modifyQuery(final String sql) throws SQLException {
        try {
            connection.createStatement().executeUpdate(sql);
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

    @ParameterizedTest
    @CsvSource({
            "'TIMESTAMP', 'TIMESTAMP', '3030-03-03 12:34:56.123'",
            "'TIMESTAMP WITH LOCAL TIME ZONE', 'TIMESTAMP', '3030-03-03 12:34:56.123'"
    })
    void testTimestampWithDefaultPrecisionMapping(String columnTypeWithPrecision, String actualColumnType, String timestampAsString) {
        assumeFalse(supportTimestampPrecision());
        Timestamp timestamp = Timestamp.valueOf(timestampAsString);
        final Table table = createSingleColumnTable(columnTypeWithPrecision).insert(timestamp);
        assertVirtualTableContents(table, table(actualColumnType).row(timestamp).matches());
    }

    @ParameterizedTest
    @CsvSource({
            "'TIMESTAMP(3)', 'TIMESTAMP', '3030-03-03 12:34:56.123'",
            "'TIMESTAMP(5)', 'TIMESTAMP', '3030-03-03 12:34:56.12345'",
            "'TIMESTAMP(9)', 'TIMESTAMP', '3030-03-03 12:34:56.123456789'",
            "'TIMESTAMP(3) WITH LOCAL TIME ZONE', 'TIMESTAMP', '3030-03-03 12:34:56.123'",
            "'TIMESTAMP(5) WITH LOCAL TIME ZONE', 'TIMESTAMP', '3030-03-03 12:34:56.12345'",
            "'TIMESTAMP(9) WITH LOCAL TIME ZONE', 'TIMESTAMP', '3030-03-03 12:34:56.123456789'"
    })
    void testTimestampWithCustomPrecisionMapping(String columnTypeWithPrecision, String actualColumnType, String timestampAsString) {
        assumeTrue(supportTimestampPrecision());
        Timestamp timestamp = Timestamp.valueOf(timestampAsString);
        final Table table = createSingleColumnTable(columnTypeWithPrecision).insert(timestamp);
        assertVirtualTableContents(table, table(actualColumnType).row(timestamp).matches());
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
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT 10.2 FROM " + getVirtualTableName(this.testVirtualSchema, table),
                table("DECIMAL").row(BigDecimal.valueOf(10.2)).matches());
    }

    @Test
    void testDoubleLiteral() {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT CAST(10.2 as DOUBLE) FROM " + getVirtualTableName(this.testVirtualSchema, table),
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
            this.testVirtualSchema = createVirtualSchema(mixedCaseSchema);
            assertVsQuery(
                    "SELECT \"Column1\", \"column2\", COLUMN3 FROM " + getVirtualTableName(this.testVirtualSchema, table),
                    table().row("foo", "bar", "baz").matches());
        } finally {
            dropAll(mixedCaseSchema);
        }
    }

    protected void assertVsQuery(final String sql, final Matcher<ResultSet> expected) {
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
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        final String sql = "SELECT " + concat + "FROM " + getVirtualTableName(this.testVirtualSchema, table);
        assertVsQuery(sql, table().row(concatResult).matches());
    }

    @Test
    void testExtractFromDate() {
        final Table table = createSingleColumnTable("DATE").insert("2000-11-01");
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT EXTRACT(MONTH FROM " + getColumnName(table, 0) + ") FROM "
                + getVirtualTableName(this.testVirtualSchema, table), table().row((short) 11).matches());
    }

    private String getColumnName(final Table table, final int index) {
        return table.getColumns().get(index).getName();
    }

    @Test
    void testExtractFromInterval() {
        final Table table = createSingleColumnTable("INTERVAL YEAR(3) TO MONTH").insert("123-09");
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT EXTRACT(MONTH FROM " + getColumnName(table, 0) + ") FROM "
                + getVirtualTableName(this.testVirtualSchema, table), table().row((short) 9).matches());
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

    @ParameterizedTest
    @CsvSource({
            "'TIMESTAMP', '2020-02-02 01:23:45.678'",
            "'TIMESTAMP WITH LOCAL TIME ZONE', '2020-02-02 01:23:45.678'"
    })
    void testCastVarcharAsTimestampWithDefaultPrecision(String timestampType, String timestampAsString) {
        assumeFalse(supportTimestampPrecision());
        Timestamp timestamp = Timestamp.valueOf(timestampAsString);
        castFrom("VARCHAR(30)").to(timestampType).input(timestampAsString).accept("TIMESTAMP").verify(timestamp);
    }

    /**
     * Verifies that casting from {@code VARCHAR} to {@code TIMESTAMP(n)} and {@code TIMESTAMP(n) WITH LOCAL TIME ZONE}
     * behaves as expected for different levels of fractional second precision.
     */
    @ParameterizedTest
    @CsvSource({
            "'TIMESTAMP(3)', '3030-03-03 12:34:56.123'",
            "'TIMESTAMP(5)', '3030-03-03 12:34:56.12345'",
            "'TIMESTAMP(7)', '3030-03-03 12:34:56.1234567'",
            "'TIMESTAMP(9)', '3030-03-03 12:34:56.123456789'",
            "'TIMESTAMP(3) WITH LOCAL TIME ZONE', '3030-03-03 12:34:56.123'",
            "'TIMESTAMP(5) WITH LOCAL TIME ZONE', '3030-03-03 12:34:56.12345'",
            "'TIMESTAMP(7) WITH LOCAL TIME ZONE', '3030-03-03 12:34:56.1234567'",
            "'TIMESTAMP(9) WITH LOCAL TIME ZONE', '3030-03-03 12:34:56.123456789'"
    })
    void testCastVarcharAsTimestampWithCustomPrecision(String timestampType, String timestampAsString) {
        assumeTrue(supportTimestampPrecision());
        Timestamp timestamp = Timestamp.valueOf(timestampAsString);
        castFrom("VARCHAR(30)").to(timestampType).input(timestampAsString).accept("TIMESTAMP").verify(timestamp);
    }

    private static boolean supportTimestampPrecision() {
        final ExasolDockerImageReference dockerImage = EXASOL.getDockerImageReference();
        if (!dockerImage.hasMajor() || !dockerImage.hasMinor() || !dockerImage.hasFix()) {
            return false;
        }
        final ExasolDbVersion exasolDbVersion = ExasolDbVersion.of(dockerImage.getMajor(), dockerImage.getMinor(), dockerImage.getFixVersion());
        if ((dockerImage.getMajor() == 8) && exasolDbVersion.isGreaterOrEqualThan(ExasolDbVersion.parse("8.32.0"))) {
            return true;
        }
        return false;
    }

    @Test
    void castIntegerAsVarchar() {
        castFrom("INTEGER").to("VARCHAR(5)").input(12345).verify("12345");
    }

    @Test
    void testCaseEqual() {
        final Table table = createSingleColumnTable("INTEGER").insert(1).insert(2).insert(3);
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT CASE C1 WHEN 1 THEN 'YES' WHEN 2 THEN 'PERHAPS' ELSE 'NO' END FROM " //
                + getVirtualTableName(this.testVirtualSchema, table), //
                table().row("YES").row("PERHAPS").row("NO").matches());
    }

    @Test
    void testCaseGreaterThan() {
        final Table table = createSingleColumnTable("INTEGER").insert(1).insert(2).insert(3);
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT CASE WHEN C1 > 1 THEN 'YES' ELSE 'NO' END FROM " //
                + getVirtualTableName(this.testVirtualSchema, table), //
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
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
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
        this.testVirtualSchema = objectFactory.createVirtualSchemaBuilder("VIRTUAL_SCHEMA_IGNORES_ERRORS") //
                .sourceSchema(this.sourceSchema) //
                .adapterScript(adapterScript) //
                .properties(properties) //
                .connectionDefinition(this.jdbcConnection) //
                .build();
        assertThat(query("SELECT NOW() - INTERVAL '1' MINUTE FROM " + getVirtualTableName(this.testVirtualSchema, table)),
                instanceOf(ResultSet.class));
    }

    @Test
    void testCreateVirtualSchemaWithoutIgnoreErrorsPropertyThrowsException() {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertQueryFailsWithErrorContaining(
                "SELECT NOW() - INTERVAL '1' MINUTE FROM " + getVirtualTableName(this.testVirtualSchema, table), //
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
        assertVsQuery(select + " FROM " + getVirtualTableName(this.testVirtualSchema, table), //
                table() //
                        .row(true, "varchar_1", 10) //
                        .row(false, "varchar_2", -10) //
                        .matches(TypeMatchMode.NO_JAVA_TYPE_CHECK));
    }

    private void createVirtualSchemaWithoutSelectListProjectionCapability() {
        final Map<String, String> properties = new HashMap<>(getConnectionSpecificVirtualSchemaProperties());
        properties.put("EXCLUDED_CAPABILITIES", "SELECTLIST_PROJECTION");
        this.testVirtualSchema = objectFactory
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

    static void assumeExasol8OrHigher() {
        assumeTrue(isExasol8OrHigher(), "is Exasol version 8 or higher");
    }

    static void assumeExasol7OrLower() {
        assumeTrue(isExasol7OrLower(), "is Exasol version 7 or lower");
    }

    static boolean isExasol8OrHigher() {
        final ExasolDockerImageReference imageReference = EXASOL.getDockerImageReference();
        return imageReference.hasMajor() && (imageReference.getMajor() >= 8);
    }

    static boolean isExasol7OrLower() {
        final ExasolDockerImageReference imageReference = EXASOL.getDockerImageReference();
        return imageReference.hasMajor() && (imageReference.getMajor() <= 7);
    }

    @Test
    void testWildcards() {
        assumeExasol7OrLower();
        final String nameWithWildcard = "A_A";
        this.sourceSchema.createTable(nameWithWildcard, "A", "VARCHAR(20)");
        this.sourceSchema.createTable("AXA", "X", "VARCHAR(20)");
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("describe " + this.testVirtualSchema.getFullyQualifiedName() + ".\"" + nameWithWildcard + "\"",
                table().row("A", "VARCHAR(20) UTF8", null, null, null).matches());
    }

    @Test
    void testWildcardsExasolV8() {
        assumeExasol8OrHigher();
        final String nameWithWildcard = "A_A";
        this.sourceSchema.createTable(nameWithWildcard, "A", "VARCHAR(20)");
        this.sourceSchema.createTable("AXA", "X", "VARCHAR(20)");
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("describe " + this.testVirtualSchema.getFullyQualifiedName() + ".\"" + nameWithWildcard + "\"",
                table().row("A", "VARCHAR(20) UTF8", null, null, null, null).matches());
    }

    @Test
    @DisplayName("Verify DISTINCT with integer literal")
    void testDistinctWithIntegerLiteral() throws SQLException {
        final Table table = createSingleColumnTable("INT") //
                .insert(1).insert(1).insert(2).insert(3);
        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        try {
            assertThat(
                    query("SELECT DISTINCT c1, 0 AS attr from "
                            + virtualSchema.getFullyQualifiedName() + "." + table.getName()),
                    table("BIGINT", "SMALLINT") //
                            .row(1L, (short) 0).row(2L, (short) 0).row(3L, (short) 0) //
                            .matchesInAnyOrder());
        } finally {
            virtualSchema.drop();
        }
    }

    @Test
    @DisplayName("Verify GROUP BY with column number reference")
    void testGroupByWithColumnNumber() throws SQLException {
        final Table table = createSingleColumnTable("INT") //
                .insert(1).insert(1).insert(2).insert(3);
        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        try {
            assertThat(
                    query("SELECT c1, count(c1) as count from "
                            + virtualSchema.getFullyQualifiedName() + "." + table.getName() + " group by 1"),
                    table("BIGINT", "BIGINT") //
                            .row(1L, 2L).row(2L, 1L).row(3L, 1L) //
                            .matchesInAnyOrder());
        } finally {
            virtualSchema.drop();
        }
    }

    @Test
    @DisplayName("Verify that a virtual and a normal table can be joined using a HASHTYPE column")
    void joinHashtypeTables() throws java.sql.SQLException {
        final Table virtualTable = sourceSchema.createTableBuilder("VIRTUAL").column("VHASH", "HASHTYPE(16 BYTE)")
                .build();
        try (final ExasolSchema otherSchema = objectFactory.createSchema("OTHER");
                final Table otherTable = otherSchema.createTableBuilder("REAL").column("RHASH", "HASHTYPE(16 BYTE)")
                        .build();
                final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema)) {
            final String sql = "select * from " + virtualSchema.getFullyQualifiedName() + "." + virtualTable.getName()
                    + " INNER JOIN " + otherTable.getFullyQualifiedName() + " ON VHASH = RHASH";
            assertThat(query(sql), table("HASHTYPE", "HASHTYPE").matches());
        }
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
            this.parent.testVirtualSchema = this.parent.createVirtualSchema(this.parent.sourceSchema);
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
                    + this.parent.getVirtualTableName(this.parent.testVirtualSchema, table);
        }
    }
}
