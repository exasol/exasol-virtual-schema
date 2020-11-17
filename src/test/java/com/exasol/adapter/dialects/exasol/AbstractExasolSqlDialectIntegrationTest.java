package com.exasol.adapter.dialects.exasol;

import com.exasol.bucketfs.Bucket;
import com.exasol.bucketfs.BucketAccessException;
import com.exasol.containers.ExasolContainer;
import com.exasol.dbbuilder.dialects.exasol.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static com.exasol.adapter.dialects.exasol.ExasolSqlDialect.EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH;
import static com.exasol.matcher.ResultSetMatcher.matchesResultSet;
import static java.util.Calendar.AUGUST;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class AbstractExasolSqlDialectIntegrationTest {
    private static final String VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION = "virtual-schema-dist-7.0.0-exasol-3.1.0.jar";
    private static final Path PATH_TO_VIRTUAL_SCHEMAS_JAR = Path.of("target", VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION);
    private static final String SCHEMA_EXASOL = "SCHEMA_EXASOL";
    private static final String ADAPTER_SCRIPT_EXASOL = "ADAPTER_SCRIPT_EXASOL";
    private static final String TABLE_JOIN_1 = "TABLE_JOIN_1";
    private static final String TABLE_JOIN_2 = "TABLE_JOIN_2";
    private static final String JDBC_EXASOL_CONNECTION = "JDBC_EXASOL_CONNECTION";
    private static final String TABLE_ALL_EXASOL_DATA_TYPES = "TABLE_ALL_EXASOL_TYPES";
    private static final String TABLE_SIMPLE_VALUES = "TABLE_SIMPLE_VALUES";
    private static final String VIRTUAL_SCHEMA_JDBC = "VIRTUAL_SCHEMA_JDBC";
    private static final String VIRTUAL_SCHEMA_JDBC_LOCAL = "VIRTUAL_SCHEMA_JDBC_LOCAL";
    private static final String VIRTUAL_SCHEMA_EXA = "VIRTUAL_SCHEMA_EXA";
    private static final String VIRTUAL_SCHEMA_EXA_LOCAL = "VIRTUAL_SCHEMA_EXA_LOCAL";
    private static final String SCHEMA_TEST_MIXED_CASE = "SCHEMA_TEST_Mixed_Case";
    private static final String TABLE_MIXED_CASE = "Table_Mixed_Case";
    private static final String VIRTUAL_SCHEMA_EXA_MIXED_CASE = "VIRTUAL_SCHEMA_EXA_Mixed_Case";
    private static final String EXASOL_DIALECT = "EXASOL";

    private static Statement statement;
    private static Connection connection;
    private static ExasolObjectFactory exasolObjectFactory;
    private static AdapterScript adapterScript;
    private static ConnectionDefinition connectionDefinition;
    private static ExasolContainer<? extends ExasolContainer<?>> container;

    // Set in the implementations classes
    protected static String hostAndDefaultPort;
    protected static String dockerImageReference;

    protected static void startSetUp()
            throws SQLException, BucketAccessException, InterruptedException, TimeoutException {
        container = new ExasolContainer<>(dockerImageReference);
        container.start();
        connection = container.createConnectionForUser(container.getUsername(), container.getPassword());
        final Bucket bucket = container.getDefaultBucket();
        bucket.uploadFile(PATH_TO_VIRTUAL_SCHEMAS_JAR, VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION);
        connection = container.createConnection();
        statement = connection.createStatement();
        exasolObjectFactory = new ExasolObjectFactory(connection);
        final ExasolSchema exasolSchema = exasolObjectFactory.createSchema(SCHEMA_EXASOL);
        exasolObjectFactory.createSchema(SCHEMA_TEST_MIXED_CASE);
        createTestTableAllExasolDataTypes();
        createTestTableWithSimpleValues();
        createTestTableMixedCase();
        createTestTablesForJoinTests(statement, SCHEMA_EXASOL, TABLE_JOIN_1, TABLE_JOIN_2);
        connectionDefinition = exasolObjectFactory.createConnectionDefinition(JDBC_EXASOL_CONNECTION,
                "jdbc:exa:" + hostAndDefaultPort, container.getUsername(), container.getPassword());
        adapterScript = exasolSchema.createAdapterScriptBuilder().name(ADAPTER_SCRIPT_EXASOL)
                .bucketFsContent("com.exasol.adapter.RequestDispatcher",
                        "/buckets/bfsdefault/default/" + VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION)
                .language(AdapterScript.Language.JAVA).build();
        createVirtualSchema(VIRTUAL_SCHEMA_JDBC, SCHEMA_EXASOL, Map.of());
        createVirtualSchema(VIRTUAL_SCHEMA_JDBC_LOCAL, SCHEMA_EXASOL, Map.of("IS_LOCAL", "true"));
        createVirtualSchema(VIRTUAL_SCHEMA_EXA, SCHEMA_EXASOL,
                Map.of("IMPORT_FROM_EXA", "true", "EXA_CONNECTION_STRING", hostAndDefaultPort));
        createVirtualSchema(VIRTUAL_SCHEMA_EXA_LOCAL, SCHEMA_EXASOL,
                Map.of("IMPORT_FROM_EXA", "true", "EXA_CONNECTION_STRING", hostAndDefaultPort, "IS_LOCAL", "true"));
        createVirtualSchema(VIRTUAL_SCHEMA_EXA_MIXED_CASE, SCHEMA_TEST_MIXED_CASE,
                Map.of("IMPORT_FROM_EXA", "true", "EXA_CONNECTION_STRING", hostAndDefaultPort));
    }

    @AfterAll
    static void releaseResources() throws SQLException {
        connection.close();
        statement.close();
        container.stop();
    }

    private static void createTestTablesForJoinTests(final Statement statement, final String schemaName,
                                                     final String firstTableName, final String secondTableName) throws SQLException {
        statement.execute("CREATE TABLE " + schemaName + "." + firstTableName + "(x INT, y VARCHAR(100))");
        statement.execute("INSERT INTO " + schemaName + "." + firstTableName + " VALUES (1,'aaa')");
        statement.execute("INSERT INTO " + schemaName + "." + firstTableName + " VALUES (2,'bbb')");
        statement.execute("CREATE TABLE " + schemaName + "." + secondTableName + "(x INT, y VARCHAR(100))");
        statement.execute("INSERT INTO " + schemaName + "." + secondTableName + " VALUES (2,'bbb')");
        statement.execute("INSERT INTO " + schemaName + "." + secondTableName + " VALUES (3,'ccc')");
    }

    private static void createTestTableAllExasolDataTypes() throws SQLException {
        statement.execute("CREATE OR REPLACE TABLE " + SCHEMA_EXASOL + "." + TABLE_ALL_EXASOL_DATA_TYPES //
                + "(c1 VARCHAR(100) DEFAULT 'bar', " //
                + "c2 VARCHAR(100) CHARACTER SET ASCII DEFAULT 'bar', " //
                + "c3 CHAR(10) DEFAULT 'foo'," //
                + "c4 CHAR(10) CHARACTER SET ASCII DEFAULT 'bar', " //
                + "c5 DECIMAL(5,0) DEFAULT 1, " //
                + "c6 DECIMAL(6,3) DEFAULT 1.2, " //
                + "c7 DOUBLE DEFAULT 1E2, " //
                + "c8 BOOLEAN DEFAULT TRUE, " //
                + "c9 DATE DEFAULT '2016-06-01', " //
                + "c10 TIMESTAMP DEFAULT '2016-06-01 00:00:01.000', " //
                + "c11 TIMESTAMP WITH LOCAL TIME ZONE DEFAULT '2016-06-01 00:00:02.000', " //
                + "c12 INTERVAL YEAR(3) TO MONTH DEFAULT '3-5', " //
                + "c13 INTERVAL DAY(4) TO SECOND(7) DEFAULT '2 12:50:10.123', " //
                + "c13B INTERVAL DAY TO SECOND DEFAULT '2 12:50:10.123', " //
                + "c14 GEOMETRY(3857) DEFAULT 'POINT(2 5)' " //
                + ")");
        statement.execute("INSERT INTO " + SCHEMA_EXASOL + "." + TABLE_ALL_EXASOL_DATA_TYPES + " VALUES " //
                + "('a茶', 'b', 'c茶', 'd', 123, 123.456, 2.2, FALSE, '2016-08-01', '2016-08-01 00:00:01.000', " //
                + "'2016-08-01 00:00:02.000', '4-6', '3 12:50:10.123', '3 12:50:10.123', 'POINT(2 5)')");
    }

    private static void createTestTableWithSimpleValues() throws SQLException {
        statement.execute("CREATE OR REPLACE TABLE " + SCHEMA_EXASOL + "." + TABLE_SIMPLE_VALUES //
                + "(a INT, " //
                + "b VARCHAR(100), " //
                + "c DOUBLE)");
        statement.execute("INSERT INTO " + SCHEMA_EXASOL + "." + TABLE_SIMPLE_VALUES + " VALUES " //
                + "(1, 'a', 1.1), " //
                + "(2, 'b', 2.2), " //
                + "(3, 'c', 3.3), " //
                + "(1, 'd', 4.4), " //
                + "(2, 'e', 5.5), " //
                + "(3, 'f', 6.6), " //
                + "(null, null, null)");
    }

    private static void createTestTableMixedCase() throws SQLException {
        statement.execute("CREATE OR REPLACE TABLE \"" + SCHEMA_TEST_MIXED_CASE + "\".\"" + TABLE_MIXED_CASE + "\""//
                + "(\"Column1\" INT, " //
                + "\"column2\" INT, " //
                + "COLUMN3 INT)");
        statement.execute("INSERT INTO \"" + SCHEMA_TEST_MIXED_CASE + "\".\"" + TABLE_MIXED_CASE + "\" VALUES " //
                + "(1, 2, 3)");
    }

    private static void createVirtualSchema(final String virtualSchemaName, final String schemaName,
                                            final Map<String, String> additionalParameters) throws SQLException {
        final Map<String, String> properties = new HashMap<>(Map.of("SCHEMA_NAME", schemaName));
        properties.putAll(additionalParameters);
        exasolObjectFactory.createVirtualSchemaBuilder(virtualSchemaName).adapterScript(adapterScript)
                .dialectName(EXASOL_DIALECT).connectionDefinition(connectionDefinition).properties(properties).build();
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testDataTypeMapping(final String virtualSchemaName) throws SQLException {
        final String expectedSchemaQualifiedTableName = SCHEMA_EXASOL + ".EXA_DBA_COLUMNS_EXPECTED";
        statement.execute("CREATE OR REPLACE TABLE " + expectedSchemaQualifiedTableName //
                + "(COLUMN_NAME VARCHAR(128), " //
                + "COLUMN_TYPE VARCHAR(40), " //
                + "COLUMN_MAXSIZE DECIMAL(18,0), " //
                + "COLUMN_NUM_PREC DECIMAL(18, 0), " //
                + "COLUMN_NUM_SCALE DECIMAL(18, 0), " //
                + "COLUMN_DEFAULT VARCHAR(2000))");
        statement.execute("INSERT INTO " + expectedSchemaQualifiedTableName + " VALUES " //
                + "('C1', 'VARCHAR(100) UTF8', 100, NULL, NULL, '''bar'''), " //
                + "('C2', 'VARCHAR(100) ASCII', 100, NULL, NULL, '''bar'''), " //
                + "('C3', 'CHAR(10) UTF8', 10, NULL, NULL, '''foo'''), " //
                + "('C4', 'CHAR(10) ASCII', 10, NULL, NULL, '''bar'''), " //
                + "('C5', 'DECIMAL(5,0)', 5, 5, 0, 1), " //
                + "('C6', 'DECIMAL(6,3)', 6, 6, 3, 1.2), " //
                + "('C7', 'DOUBLE', 64, NULL, NULL, 100), " //
                + "('C8', 'BOOLEAN', 1, NULL, NULL, TRUE), " //
                + "('C9', 'DATE', 10, NULL, NULL, '''2016-06-01'''), " //
                + "('C10', 'TIMESTAMP', 29, NULL, NULL, '''2016-06-01 00:00:01.000'''), " //
                + "('C11', 'TIMESTAMP WITH LOCAL TIME ZONE', 29, NULL, NULL, '''2016-06-01 00:00:02.000'''), " //
                + "('C12', 'INTERVAL YEAR(3) TO MONTH', 13, NULL, NULL, '''3-5'''), " //
                + "('C13', 'INTERVAL DAY(4) TO SECOND(7)', 29, NULL, NULL, '''2 12:50:10.123'''), " //
                + "('C13B', 'INTERVAL DAY(2) TO SECOND(3)', 29, NULL, NULL, '''2 12:50:10.123'''), "// default precision
                + "('C14', 'GEOMETRY(3857)', 8000000, NULL, NULL, '''POINT(2 5)''') " //
        );
        final ResultSet expected = statement.executeQuery("SELECT * FROM " + expectedSchemaQualifiedTableName);
        final ResultSet actual = statement
                .executeQuery("SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_MAXSIZE, COLUMN_NUM_PREC, COLUMN_NUM_SCALE, "
                        + "COLUMN_DEFAULT FROM EXA_DBA_COLUMNS WHERE COLUMN_SCHEMA = '" + virtualSchemaName
                        + "' AND COLUMN_TABLE='" + TABLE_ALL_EXASOL_DATA_TYPES + "' ORDER BY COLUMN_ORDINAL_POSITION");
        assertThat(actual, matchesResultSet(expected));
    }

    private static Stream<String> getVirtualSchemaVariantsAll() {
        return Stream.of(VIRTUAL_SCHEMA_JDBC, VIRTUAL_SCHEMA_JDBC_LOCAL, VIRTUAL_SCHEMA_EXA, VIRTUAL_SCHEMA_EXA_LOCAL);
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testVarchar(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C1, C2");
    }

    private void assertSelectColumnsResult(final String virtualSchemaName, final String columns) throws SQLException {
        final ResultSet expected = statement
                .executeQuery("SELECT " + columns + " FROM " + SCHEMA_EXASOL + "." + TABLE_ALL_EXASOL_DATA_TYPES);
        final ResultSet actual = statement
                .executeQuery("SELECT " + columns + " FROM " + virtualSchemaName + "." + TABLE_ALL_EXASOL_DATA_TYPES);
        assertThat(actual, matchesResultSet(expected));
    }

    // TODO: This test excludes VIRTUAL_SCHEMA_EXA virtual schema because of the wrong data mapping caused by driver.
    // https://github.com/exasol/virtual-schemas/issues/299
    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsWithoutExa")
    void testChar(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C3, C4");
    }

    private static Stream<String> getVirtualSchemaVariantsWithoutExa() {
        return Stream.of(VIRTUAL_SCHEMA_JDBC, VIRTUAL_SCHEMA_JDBC_LOCAL, VIRTUAL_SCHEMA_EXA_LOCAL);
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testDecimal(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C5, C6");
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testDouble(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C7");
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testBoolean(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C8");
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testDate(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C9");
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testTimestamp(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C10, C11");
    }

    // TODO: This test excludes VIRTUAL_SCHEMA_JDBC and VIRTUAL_SCHEMA_EXA virtual schema because of the bug on the
    // loader side.
    // https://github.com/exasol/exasol-virtual-schema/issues/4
    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsOnlyLocal")
    void testIntervalYearToMonth(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C12");
    }

    private static Stream<String> getVirtualSchemaVariantsOnlyLocal() {
        return Stream.of(VIRTUAL_SCHEMA_JDBC_LOCAL, VIRTUAL_SCHEMA_EXA_LOCAL);
    }

    // TODO: This test excludes VIRTUAL_SCHEMA_JDBC and VIRTUAL_SCHEMA_EXA virtual schema because of the bug on the
    // loader side.
    // https://github.com/exasol/exasol-virtual-schema/issues/4
    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsOnlyLocal")
    void testIntervalDayToSecond(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C13");
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testGeometry(final String virtualSchemaName) throws SQLException {
        assertSelectColumnsResult(virtualSchemaName, "C14");
    }

    @Test
    void testIdentifierCaseSensitivityOnTable() throws SQLException {
        final ResultSet expected = statement.executeQuery("SELECT \"Column1\", \"column2\", COLUMN3 FROM \""
                + SCHEMA_TEST_MIXED_CASE + "\".\"" + TABLE_MIXED_CASE + "\"");
        final ResultSet actual = statement.executeQuery("SELECT \"Column1\", \"column2\", COLUMN3 FROM \""
                + VIRTUAL_SCHEMA_EXA_MIXED_CASE + "\".\"" + TABLE_MIXED_CASE + "\"");
        assertThat(actual, matchesResultSet(expected));
    }

    @Test
    void assertUnquotedMixedCaseTableIsNotFound() {
        final SQLException exception = assertThrows(SQLException.class,
                () -> statement.executeQuery("SELECT * FROM \"" + SCHEMA_TEST_MIXED_CASE + "\"." + TABLE_MIXED_CASE));
        assertThat(exception.getMessage(),
                containsString("object \"SCHEMA_TEST_Mixed_Case\".\"TABLE_MIXED_CASE\" not found "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Column1", "column2"})
    void assertUnquotedMixedCaseColumnIsNotFound(final String columnName) {
        final SQLException exception = assertThrows(SQLException.class, () -> statement.executeQuery("SELECT "
                + columnName + " FROM \"" + VIRTUAL_SCHEMA_EXA_MIXED_CASE + "\".\"" + TABLE_MIXED_CASE + "\""));
        assertThat(exception.getMessage(), containsString("object " + columnName.toUpperCase() + " not found "));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testGroupConcat(final String virtualSchemaName) {
        final String query = "SELECT GROUP_CONCAT(A) FROM " + virtualSchemaName + "." + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "1,1,2,2,3,3"), //
                () -> assertExplainVirtual(query, //
                        "SELECT GROUP_CONCAT(\"" + TABLE_SIMPLE_VALUES + "\".\"A\") FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    @ParameterizedTest
    @CsvSource({"ST_UNION", "ST_INTERSECTION"})
    void testStAggregateFunctions(final String functionName) {
        final String query = "SELECT " + functionName + "('POLYGON ((0 0, 0 4, 2 4, 2 0, 0 0))') FROM "
                + VIRTUAL_SCHEMA_EXA_LOCAL + "." + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "POLYGON ((0 0, 0 4, 2 4, 2 0, 0 0))"), //
                () -> assertExplainVirtual(query, //
                        "SELECT " + functionName + "('POLYGON ((0 0, 0 4, 2 4, 2 0, 0 0))') FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    private static Stream<String> getVirtualSchemaVariantsExa() {
        return Stream.of(VIRTUAL_SCHEMA_EXA, VIRTUAL_SCHEMA_EXA_LOCAL);
    }

    private void assertExpressionExecutionStringResult(final String query, final String expected) throws SQLException {
        final ResultSet result = statement.executeQuery(query);
        result.next();
        final String actual = result.getString(1);
        assertThat(actual, containsString(expected));
    }

    private void assertExplainVirtual(final String query, final String expected) throws SQLException {
        final ResultSet explainVirtual = statement.executeQuery("EXPLAIN VIRTUAL " + query);
        explainVirtual.next();
        final String explainVirtualStringActual = explainVirtual.getString("PUSHDOWN_SQL");
        assertThat(explainVirtualStringActual, containsString(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testGroupConcatDistinct(final String virtualSchemaName) {
        final String query = "SELECT GROUP_CONCAT(DISTINCT A) FROM " + virtualSchemaName + "." + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "1,2,3"), //
                () -> assertExplainVirtual(query, //
                        "SELECT GROUP_CONCAT(DISTINCT \"" + TABLE_SIMPLE_VALUES + "\".\"A\") FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testGroupConcatOrderBy(final String virtualSchemaName) {
        final String query = "SELECT GROUP_CONCAT(A ORDER BY C) FROM " + virtualSchemaName + "." + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "1,2,3,1,2,3"), //
                () -> assertExplainVirtual(query, //
                        "SELECT GROUP_CONCAT(\"" + TABLE_SIMPLE_VALUES + "\".\"A\" ORDER BY \"" //
                                + TABLE_SIMPLE_VALUES + "\".\"C\") FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testGroupConcatOrderByDesc(final String virtualSchemaName) {
        final String query = "SELECT GROUP_CONCAT(A ORDER BY C DESC) FROM " + virtualSchemaName + "."
                + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "3,2,1,3,2,1"), //
                () -> assertExplainVirtual(query, //
                        "SELECT GROUP_CONCAT(\"" + TABLE_SIMPLE_VALUES + "\".\"A\" ORDER BY \"" //
                                + TABLE_SIMPLE_VALUES + "\".\"C\" DESC) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testGroupConcatOrderByDescNullsLast(final String virtualSchemaName) {
        final String query = "SELECT GROUP_CONCAT(A ORDER BY C DESC NULLS LAST) FROM " + virtualSchemaName + "."
                + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "3,2,1,3,2,1"), //
                () -> assertExplainVirtual(query, //
                        "SELECT GROUP_CONCAT(\"" + TABLE_SIMPLE_VALUES + "\".\"A\" ORDER BY \"" //
                                + TABLE_SIMPLE_VALUES + "\".\"C\" DESC NULLS LAST) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getParameterForTestGroupConcatSeparator")
    void testGroupConcatSeparator(final String virtualSchemaName, final String separator) {
        final String query = "SELECT GROUP_CONCAT(A SEPARATOR ';'||' ') FROM " + virtualSchemaName + "."
                + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "1; 1; 2; 2; 3; 3"), //
                () -> assertExplainVirtual(query, //
                        "SELECT GROUP_CONCAT(\"" + TABLE_SIMPLE_VALUES + "\".\"A\" SEPARATOR " + separator + ") FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    private static Stream<Arguments> getParameterForTestGroupConcatSeparator() {
        return Stream.of(Arguments.of(VIRTUAL_SCHEMA_EXA, "''; ''"), //
                Arguments.of(VIRTUAL_SCHEMA_EXA_LOCAL, "'; '"));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsOnlyLocal")
    void testGroupConcatSeparatorLocalVirtualSchemas(final String virtualSchemaName) {
        final String query = "SELECT GROUP_CONCAT(A SEPARATOR ';'||' ') FROM " + virtualSchemaName + "."
                + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "1; 1; 2; 2; 3; 3"), //
                () -> assertExplainVirtual(query, //
                        "SELECT GROUP_CONCAT(\"" + TABLE_SIMPLE_VALUES + "\".\"A\" SEPARATOR '; ') FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testExtractFromDate(final String virtualSchemaName) {
        final String query = "SELECT EXTRACT(MONTH FROM C9) FROM " + virtualSchemaName + "."
                + TABLE_ALL_EXASOL_DATA_TYPES;
        assertAll(() -> assertExpressionExecutionIntResult(query, 8), //
                () -> assertExplainVirtual(query, //
                        "SELECT EXTRACT(MONTH FROM \"" + TABLE_ALL_EXASOL_DATA_TYPES + "\".\"C9\") FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_ALL_EXASOL_DATA_TYPES + "\""));
    }

    private void assertExpressionExecutionIntResult(final String query, final int expected) throws SQLException {
        final ResultSet result = statement.executeQuery(query);
        result.next();
        final int actual = result.getInt(1);
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testExtractFromInterval(final String virtualSchemaName) {
        final String query = "SELECT EXTRACT(MONTH FROM C12) FROM " + virtualSchemaName + "."
                + TABLE_ALL_EXASOL_DATA_TYPES;
        assertAll(() -> assertExpressionExecutionIntResult(query, 6), //
                () -> assertExplainVirtual(query, //
                        "SELECT EXTRACT(MONTH FROM \"" + TABLE_ALL_EXASOL_DATA_TYPES + "\".\"C12\") FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_ALL_EXASOL_DATA_TYPES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastAsChar(final String virtualSchemaName) {
        final String query = "SELECT CAST(A AS CHAR(15)) FROM " + virtualSchemaName + "." + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "1              "), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(\"" + TABLE_SIMPLE_VALUES + "\".\"A\" AS CHAR(15) UTF8) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastBooleanAsVarchar(final String virtualSchemaName) {
        final String query = "SELECT CAST(CAST(A > 0 AS VARCHAR(15)) AS BOOLEAN) FROM " + virtualSchemaName + "."
                + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionBooleanResult(query, true), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(CAST(0 < \"" + TABLE_SIMPLE_VALUES
                                + "\".\"A\" AS VARCHAR(15) UTF8) AS BOOLEAN) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    private void assertExpressionExecutionBooleanResult(final String query, final boolean expected)
            throws SQLException {
        final ResultSet result = statement.executeQuery(query);
        result.next();
        final boolean actualResult = result.getBoolean(1);
        assertThat(actualResult, equalTo(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastVarcharAsDate(final String virtualSchemaName) {
        final String query = "SELECT CAST(CAST(C9 AS VARCHAR(30)) AS DATE) FROM " + virtualSchemaName + "."
                + TABLE_ALL_EXASOL_DATA_TYPES;
        assertAll(
                () -> assertExpressionExecutionDateResult(query,
                        new Date(new GregorianCalendar(2016, AUGUST, 1).getTime().getTime())), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(CAST(\"" + TABLE_ALL_EXASOL_DATA_TYPES
                                + "\".\"C9\" AS VARCHAR(30) UTF8) AS DATE) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_ALL_EXASOL_DATA_TYPES + "\""));
    }

    private void assertExpressionExecutionDateResult(final String query, final Date expected) throws SQLException {
        final ResultSet result = statement.executeQuery(query);
        result.next();
        final Date actualResult = result.getDate(1);
        assertThat(actualResult, equalTo(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastVarcharAsDecimal(final String virtualSchemaName) {
        final String query = "SELECT CAST(CAST(A AS VARCHAR(15)) AS DECIMAL(8, 1)) FROM " + virtualSchemaName + "."
                + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionBigDecimalResult(query, new BigDecimal("1.0")), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(CAST(\"" + TABLE_SIMPLE_VALUES
                                + "\".\"A\" AS VARCHAR(15) UTF8) AS DECIMAL(8, 1)) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    private void assertExpressionExecutionBigDecimalResult(final String query, final BigDecimal expected)
            throws SQLException {
        final ResultSet result = statement.executeQuery(query);
        result.next();
        final BigDecimal actualResult = result.getBigDecimal(1);
        assertThat(actualResult, equalTo(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastVarcharAsDouble(final String virtualSchemaName) {
        final String query = "SELECT CAST(CAST(C AS VARCHAR(15)) AS DOUBLE) FROM " + virtualSchemaName + "."
                + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionDoubleResult(query, 1.1d), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(CAST(\"" + TABLE_SIMPLE_VALUES + "\".\"C\" AS VARCHAR(15) UTF8) AS DOUBLE) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    private void assertExpressionExecutionDoubleResult(final String query, final double expected) throws SQLException {
        final ResultSet result = statement.executeQuery(query);
        result.next();
        final double actualResult = result.getDouble(1);
        assertThat(actualResult, equalTo(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastVarcharAsGeometry(final String virtualSchemaName) {
        final String query = "SELECT CAST(CAST(C14 AS VARCHAR(100)) AS GEOMETRY(5)) FROM " + virtualSchemaName + "."
                + TABLE_ALL_EXASOL_DATA_TYPES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "POINT (2 5)"), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(CAST(\"" + TABLE_ALL_EXASOL_DATA_TYPES
                                + "\".\"C14\" AS VARCHAR(100) UTF8) AS GEOMETRY(5)) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_ALL_EXASOL_DATA_TYPES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastVarcharAsIntervalDayToSecond(final String virtualSchemaName) {
        final String query = "SELECT CAST(CAST(C13 AS VARCHAR(100)) AS INTERVAL DAY (5) TO SECOND (2)) FROM "
                + virtualSchemaName + "." + TABLE_ALL_EXASOL_DATA_TYPES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "+00003 12:50:10.12"), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(CAST(\"" + TABLE_ALL_EXASOL_DATA_TYPES
                                + "\".\"C13\" AS VARCHAR(100) UTF8) AS INTERVAL DAY (5) TO SECOND (2)) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_ALL_EXASOL_DATA_TYPES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastVarcharAsIntervalYearToMonth(final String virtualSchemaName) {
        final String query = "SELECT CAST(CAST(C12 AS VARCHAR(100)) AS INTERVAL YEAR (5) TO MONTH) FROM "
                + virtualSchemaName + "." + TABLE_ALL_EXASOL_DATA_TYPES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "+00004-06"), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(CAST(\"" + TABLE_ALL_EXASOL_DATA_TYPES
                                + "\".\"C12\" AS VARCHAR(100) UTF8) AS INTERVAL YEAR (5) TO MONTH) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_ALL_EXASOL_DATA_TYPES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastVarcharAsTimestamp(final String virtualSchemaName) {
        final String query = "SELECT CAST(CAST(C10 AS VARCHAR(100)) AS TIMESTAMP) FROM " + virtualSchemaName + "."
                + TABLE_ALL_EXASOL_DATA_TYPES;
        assertAll(
                () -> assertExpressionExecutionTimestampResult(query,
                        new Timestamp(new GregorianCalendar(2016, AUGUST, 1, 0, 0, 1).getTime().getTime())), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(CAST(\"" + TABLE_ALL_EXASOL_DATA_TYPES
                                + "\".\"C10\" AS VARCHAR(100) UTF8) AS TIMESTAMP) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_ALL_EXASOL_DATA_TYPES + "\""));
    }

    private void assertExpressionExecutionTimestampResult(final String query, final Timestamp expected)
            throws SQLException {
        final ResultSet result = statement.executeQuery(query);
        result.next();
        final Timestamp actual = result.getTimestamp(1);
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastVarcharAsTimestampWithLocalTimeZone(final String virtualSchemaName) {
        final String query = "SELECT CAST(CAST(C11 AS VARCHAR(100)) AS TIMESTAMP WITH LOCAL TIME ZONE) FROM "
                + virtualSchemaName + "." + TABLE_ALL_EXASOL_DATA_TYPES;
        assertAll(
                () -> assertExpressionExecutionTimestampResult(query,
                        new Timestamp(new GregorianCalendar(2016, AUGUST, 1, 0, 0, 2).getTime().getTime())), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(CAST(\"" + TABLE_ALL_EXASOL_DATA_TYPES
                                + "\".\"C11\" AS VARCHAR(100) UTF8) AS TIMESTAMP WITH LOCAL TIME ZONE) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_ALL_EXASOL_DATA_TYPES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsExa")
    void testCastIntAsVarchar(final String virtualSchemaName) {
        final String query = "SELECT CAST(A AS VARCHAR(15)) FROM " + virtualSchemaName + "." + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "1"), //
                () -> assertExplainVirtual(query, //
                        "SELECT CAST(\"" + TABLE_SIMPLE_VALUES + "\".\"A\" AS VARCHAR(15) UTF8) FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    @ParameterizedTest
    @MethodSource("getParameterForTestCaseEqual")
    void testCaseEqual(final String virtualSchemaName, final String then) {
        final String query = "SELECT CASE A WHEN 1 THEN 'YES' WHEN 2 THEN 'PERHAPS' ELSE 'NO' END FROM "
                + virtualSchemaName + "." + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "YES"), //
                () -> assertExplainVirtual(query, //
                        "SELECT CASE \"" + TABLE_SIMPLE_VALUES + "\".\"A\" WHEN 1 " + then + " FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    private static Stream<Arguments> getParameterForTestCaseEqual() {
        return Stream.of(Arguments.of(VIRTUAL_SCHEMA_EXA, "THEN ''YES'' WHEN 2 THEN ''PERHAPS'' ELSE ''NO'' END"), //
                Arguments.of(VIRTUAL_SCHEMA_EXA_LOCAL, "THEN 'YES' WHEN 2 THEN 'PERHAPS' ELSE 'NO' END"));
    }

    @ParameterizedTest
    @MethodSource("getParameterForTestCaseMoreThan")
    void testCaseMoreThan(final String virtualSchemaName, final String then) {
        final String query = "SELECT CASE WHEN A > 1 THEN 'YES' ELSE 'NO' END FROM " //
                + virtualSchemaName + "." + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "NO"), //
                () -> assertExplainVirtual(query, //
                        "SELECT CASE WHEN 1 < \"" + TABLE_SIMPLE_VALUES + "\".\"A\" " + then + " FROM \"" //
                                + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\""));
    }

    private static Stream<Arguments> getParameterForTestCaseMoreThan() {
        return Stream.of(Arguments.of(VIRTUAL_SCHEMA_EXA, "THEN ''YES'' ELSE ''NO'' END"), //
                Arguments.of(VIRTUAL_SCHEMA_EXA_LOCAL, "THEN 'YES' ELSE 'NO' END"));
    }

    @Test
    void testCreateVirtualSchemaWithNonexistentConnectionThrowsException() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CREATE VIRTUAL SCHEMA VIRTUAL_SCHEMA_WRONG_CONNECTION");
        builder.append(" USING " + SCHEMA_EXASOL + ".ADAPTER_SCRIPT_EXASOL WITH ");
        builder.append("SQL_DIALECT     = 'EXASOL' ");
        builder.append("CONNECTION_NAME = 'NONEXISTENT_CONNECTION' ");
        builder.append("SCHEMA_NAME     = '" + SCHEMA_EXASOL + "' ");
        final SQLException exception = assertThrows(SQLException.class, () -> statement.execute(builder.toString()));
        assertThat(exception.getMessage(),
                containsString("Could not access the connection information of connection \"NONEXISTENT_CONNECTION\""));
    }

    @ParameterizedTest
    @MethodSource("getParameterForTestVirtualSchemaExplainImport")
    void testVirtualSchemaExplainImport(final String virtualSchemaName, final String expectedImportStatement) {
        final String query = "SELECT 1 FROM " + virtualSchemaName + "." + TABLE_SIMPLE_VALUES;
        assertAll(() -> assertExpressionExecutionStringResult(query, "1"), //
                () -> assertExplainVirtual(query, expectedImportStatement));
    }

    private static Stream<Arguments> getParameterForTestVirtualSchemaExplainImport() {
        final String select = "SELECT 1 FROM \"" + SCHEMA_EXASOL + "\".\"" + TABLE_SIMPLE_VALUES + "\"";
        return Stream.of(
                Arguments.of(VIRTUAL_SCHEMA_EXA,
                        "IMPORT FROM EXA AT '" + hostAndDefaultPort + "' USER 'SYS' IDENTIFIED BY 'exasol' STATEMENT " //
                                + "'" + select + "'"),
                Arguments.of(VIRTUAL_SCHEMA_EXA_LOCAL, select),
                Arguments.of(VIRTUAL_SCHEMA_JDBC,
                        "IMPORT INTO (c1 DECIMAL(1, 0)) FROM JDBC AT " + JDBC_EXASOL_CONNECTION + " STATEMENT " //
                                + "'" + select + "'"),
                Arguments.of(VIRTUAL_SCHEMA_JDBC_LOCAL, select));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testInnerJoin(final String virtualSchemaName) throws SQLException {
        final ResultSet expected = getSelectAllFromJoinExpectedTable(statement, SCHEMA_EXASOL,
                "(x INT, y VARCHAR(100), a INT, b VARCHAR(100))", "VALUES(2,'bbb', 2,'bbb')");
        final ResultSet actual = statement.executeQuery("SELECT * FROM " + virtualSchemaName + "." + TABLE_JOIN_1
                + " a INNER JOIN  " + virtualSchemaName + "." + TABLE_JOIN_2 + " b ON a.x=b.x");
        assertThat(actual, matchesResultSet(expected));
    }

    private ResultSet getSelectAllFromJoinExpectedTable(final Statement statement, final String schemaName,
                                                        final String expectedColumns, final String expectedValues) throws SQLException {
        statement.execute("CREATE OR REPLACE TABLE " + schemaName + ".TABLE_JOIN_EXPECTED " + expectedColumns);
        statement.execute("INSERT INTO " + schemaName + ".TABLE_JOIN_EXPECTED " + expectedValues);
        return statement.executeQuery("SELECT * FROM " + schemaName + ".TABLE_JOIN_EXPECTED");
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testInnerJoinWithProjection(final String virtualSchemaName) throws SQLException {
        final ResultSet expected = getSelectAllFromJoinExpectedTable(statement, SCHEMA_EXASOL, "(y VARCHAR(100))",
                " VALUES('bbbbbb')");
        final ResultSet actual = statement.executeQuery("SELECT b.y || " + virtualSchemaName + "." + TABLE_JOIN_1
                + ".y FROM " + virtualSchemaName + "." + TABLE_JOIN_1 + " INNER JOIN  " + virtualSchemaName + "."
                + TABLE_JOIN_2 + " b ON " + virtualSchemaName + "." + TABLE_JOIN_1 + ".x=b.x");
        assertThat(actual, matchesResultSet(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testLeftJoin(final String virtualSchemaName) throws SQLException {
        final ResultSet expected = getSelectAllFromJoinExpectedTable(statement, SCHEMA_EXASOL,
                "(x INT, y VARCHAR(100), a INT, b VARCHAR(100))", "VALUES(1, 'aaa', null, null), " //
                        + "(2, 'bbb', 2, 'bbb')");
        final ResultSet actual = statement.executeQuery("SELECT * FROM " + virtualSchemaName + "." + TABLE_JOIN_1
                + " a LEFT OUTER JOIN  " + virtualSchemaName + "." + TABLE_JOIN_2 + " b ON a.x=b.x ORDER BY a.x");
        assertThat(actual, matchesResultSet(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testRightJoin(final String virtualSchemaName) throws SQLException {
        final ResultSet expected = getSelectAllFromJoinExpectedTable(statement, SCHEMA_EXASOL,
                "(x INT, y VARCHAR(100), a INT, b VARCHAR(100))", "VALUES(2, 'bbb', 2, 'bbb'), " //
                        + "(null, null, 3, 'ccc')");
        final ResultSet actual = statement.executeQuery("SELECT * FROM " + virtualSchemaName + "." + TABLE_JOIN_1
                + " a RIGHT OUTER JOIN  " + virtualSchemaName + "." + TABLE_JOIN_2 + " b ON a.x=b.x ORDER BY a.x");
        assertThat(actual, matchesResultSet(expected));
    }

    @ParameterizedTest
    @MethodSource("getVirtualSchemaVariantsAll")
    void testFullOuterJoin(final String virtualSchemaName) throws SQLException {
        final ResultSet expected = getSelectAllFromJoinExpectedTable(statement, SCHEMA_EXASOL,
                "(x INT, y VARCHAR(100), a INT, b VARCHAR(100))", "VALUES(1, 'aaa', null, null), " //
                        + "(2, 'bbb', 2, 'bbb'), " //
                        + "(null, null, 3, 'ccc')");
        final ResultSet actual = statement.executeQuery("SELECT * FROM " + virtualSchemaName + "." + TABLE_JOIN_1
                + " a FULL OUTER JOIN  " + virtualSchemaName + "." + TABLE_JOIN_2 + " b ON a.x=b.x ORDER BY a.x");
        assertThat(actual, matchesResultSet(expected));
    }

    @Test
    void testCreateVirtualSchemaWithIgnoreErrorsProperty() throws SQLException {
        createVirtualSchema("VIRTUAL_SCHEMA_IGNORES_ERRORS", SCHEMA_EXASOL,
                Map.of("IS_LOCAL", "true", "IGNORE_ERRORS", EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH));
        assertThat(
                statement.executeQuery(
                        "SELECT NOW() - INTERVAL '1' MINUTE FROM VIRTUAL_SCHEMA_IGNORES_ERRORS." + TABLE_SIMPLE_VALUES),
                instanceOf(ResultSet.class));
    }

    @Test
    void testVirtualSchemaWithoutIgnoreErrorsPropertyThrowsException() {
        final SQLException exception = assertThrows(SQLException.class, () -> statement.execute(
                "SELECT NOW() - INTERVAL '1' MINUTE FROM " + VIRTUAL_SCHEMA_EXA_LOCAL + "." + TABLE_SIMPLE_VALUES));
        assertThat(exception.getMessage(),
                containsString("Attention! Using literals and constant expressions with datatype "
                        + "`TIMESTAMP WITH LOCAL TIME ZONE` in Virtual Schemas can produce an incorrect results"));
    }
}