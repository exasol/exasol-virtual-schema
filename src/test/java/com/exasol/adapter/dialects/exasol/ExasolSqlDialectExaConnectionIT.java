package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_CONNECTION_PROPERTY;
import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer.NoDriverFoundException;

import com.exasol.adapter.properties.PropertyValidationException;
import com.exasol.dbbuilder.dialects.Table;
import com.exasol.dbbuilder.dialects.exasol.ConnectionDefinition;

/**
 * This class exercises a set of tests defined in the base class on a local Exasol, using {@code IMPORT} via a EXA
 * connection.
 * <p>
 * In this case the Adapter uses a different (JDBC) connection to attach to the database than the ExaLoader which runs
 * this {@code IMPORT}.
 * </p>
 * <p>
 * These tests take the following specialties of a local connection into account:
 * </p>
 * <ul>
 * <li>{@code INTERVAL} types are reported with JDBC type name {@code VARCHAR} in ResultSets</li>
 * <li>{@code HASHTYPE} types are reported with JDBC type name {@code VARCHAR} in ResultSets</li>
 * <li>{@code GEOMETRY} types are reported with JDBC type name {@code VARCHAR} in ResultSets</li>
 * <ul>
 */

class ExasolSqlDialectExaConnectionIT extends AbstractRemoteExasolVirtualSchemaConnectionIT {
    private static final String EXA_CONNECTION_NAME = "EXA_CONNECTION";
    private ConnectionDefinition exaConnection;

    @Override
    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        this.exaConnection = objectFactory.createConnectionDefinition(EXA_CONNECTION_NAME, getTargetAddress(),
                this.user.getName(), this.user.getPassword());
    }

    private String getTargetAddress() {
        return "127.0.0.1" + "/" + EXASOL.getTlsCertificateFingerprint().orElseThrow() + ":"
                + EXASOL.getDefaultInternalDatabasePort();
    }

    @Override
    @AfterEach
    void afterEach() {
        dropAll(this.exaConnection);
        this.exaConnection = null;
        super.afterEach();
    }

    @Override
    protected Set<String> expectVarcharFor() {
        return Set.of("GEOMETRY", "INTERVAL", "INTERVAL YEAR TO MONTH", "INTERVAL DAY TO SECOND", "HASHTYPE");
    }

    @Override
    protected Map<String, String> getConnectionSpecificVirtualSchemaProperties() {
        return Map.of("IMPORT_FROM_EXA", "true", EXASOL_CONNECTION_PROPERTY, this.exaConnection.getName());
    }

    // These tests were overridden for the following reasons:
    // Strict datatype checking which is by default enabled for Exasol V8 makes a number of these tests fail.
    //
    // The old tests that work for DB versions prior to strict datatype checking
    // thus can only be run for earlier Exasol DB versions and have to be skipped for V8.
    //
    // The tests using the newer GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA switch that fixes most of the issues with strict
    // datatype checking on the pushed down queries
    // can be found in ExasolSqlDialectExaConnectionWithDataTypesIT.

    @Test
    void testPasswordNotVisibleInImportFromExa() throws NoDriverFoundException, SQLException {
        assumeExasol7OrLower();
        final Table table = this.sourceSchema.createTable("T1", "C1", "VARCHAR(20)").insert("Hello.");
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        final String sql = "SELECT * FROM " + this.testVirtualSchema.getFullyQualifiedName() + ".\"" + table.getName()
                + "\"";
        assertThat(explainVirtual(sql), //
                table().row( //
                        anything(), //
                        not(anyOf( //
                                containsString(this.user.getName()), //
                                containsString(this.user.getPassword()), //
                                containsString(EXASOL.getUsername()), //
                                containsString(EXASOL.getPassword()) //
                        )), //
                        anything(), //
                        anything() //
                ).matches());
    }

    @Test
    void testAlterVirtualSchemaTriggersPropertyValidation() {
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        final String name = this.testVirtualSchema.getFullyQualifiedName();
        final SQLException exception = assertThrows(SQLException.class,
                () -> query("alter virtual schema {0} set EXA_CONNECTION = Null", name));
        final String expected = PropertyValidationException.class.getName() + ": E-VSCJDBC-17";
        assertThat(exception.getMessage(), containsString(expected));
    }

    private ResultSet explainVirtual(final String sql) throws SQLException {
        return query("EXPLAIN VIRTUAL " + sql);
    }

    @Override
    @Test
    void testCharMappingAscii() {
        assumeExasol7OrLower();
        final Table table = createSingleColumnTable("CHAR(20) ASCII").insert("sun").insert("rain");
        assertVirtualTableContents(table, table("VARCHAR").row(pad("sun", 20)).row(pad("rain", 20)).matches());
    }

    @Override
    @Test
    void testCharMappingUtf8() {
        assumeExasol7OrLower();
        verifyCharMappingUtf8("VARCHAR");
    }

    @Override
    @Test
    void testCastVarcharToChar() {
        assumeExasol7OrLower();
        castFrom("VARCHAR(20)").to("CHAR(40)").input("Hello.").accept("VARCHAR").verify(pad("Hello.", 40));
    }

    @Override
    @Test
    void joinHashtypeTables() {
        final SQLException exception = assertThrows(SQLException.class, super::joinHashtypeTables);
        assertThat(exception.getMessage(), anyOf(
                // Error message for Exasol 7.1:
                containsString("Feature not supported: Incomparable Types: VARCHAR(32) UTF8 and HASHTYPE(16 BYTE)!"),
                // Error message for Exasol 8:
                containsString(
                        "Adapter generated invalid pushdown query for virtual table VIRTUAL: Data type mismatch in column number 1 (1-indexed). Expected HASHTYPE(16 BYTE), but got VARCHAR(32) UTF8.")));
    }

    @Test
    @Override
    void testNonDefaultGeometry() {
        assumeExasol7OrLower();
        typeAssertionFor("GEOMETRY(4321)").withValue("POINT (2 5)") //
                .expectTypeOf("GEOMETRY(4321)") //
                .expectDescribeType("GEOMETRY(4321)") //
                .expectResultSetType(expectDataType("GEOMETRY")) //
                .runAssert();

    }

    @Override
    @Test
    void testHashTypeWithBitSize() {
        assumeExasol7OrLower();
        typeAssertionFor("HASHTYPE(16 BIT)").withValue("550e") //
                .expectDescribeType("HASHTYPE(2 BYTE)") //
                .expectTypeOf("HASHTYPE(2 BYTE)") //
                .expectResultSetType(expectDataType("HASHTYPE")) //
                .runAssert();
    }

    @Override
    @Test
    void testDefaultIntervalYearToMonth() {
        assumeExasol7OrLower();
        typeAssertionFor("INTERVAL YEAR TO MONTH").withValue("5-3") //
                .expectTypeOf("INTERVAL YEAR(2) TO MONTH") //
                .expectDescribeType("INTERVAL YEAR(2) TO MONTH") //
                .expectResultSetType(expectDataType("INTERVAL YEAR TO MONTH")) //
                .expectValue("+05-03") //
                .runAssert();
    }

    @Override
    @Test
    void testDefaultGeometry() {
        assumeExasol7OrLower();
        typeAssertionFor("GEOMETRY").withValue("POINT (2 5)") //
                .expectTypeOf("GEOMETRY") //
                .expectDescribeType("GEOMETRY") //
                .expectResultSetType(expectDataType("GEOMETRY")) //
                .runAssert();
    }

    @Override
    @Test
    void testDefaultIntervalDayToSecond() {
        assumeExasol7OrLower();
        typeAssertionFor("INTERVAL DAY TO SECOND").withValue("2 12:50:10.123") //
                // day: 2 digits, seconds: 3 digits after decimal point
                .expectTypeOf("INTERVAL DAY(2) TO SECOND(3)") //
                .expectDescribeType("INTERVAL DAY(2) TO SECOND(3)") //
                .expectResultSetType(expectDataType("INTERVAL DAY TO SECOND")) //
                .expectValue("+02 12:50:10.123") //
                .runAssert();
    }

    @Override
    @Test
    void testGeometryMapping() {
        assumeExasol7OrLower();
        // Note that the JDBC driver reports the result as VARCHAR for Exasol database with major version < 8
        final Table table = createSingleColumnTable("GEOMETRY").insert("POINT (2 3)");
        assertVirtualTableContents(table, table(expectDataType("GEOMETRY")).row("POINT (2 3)").matches());
    }

    @Override
    @Test
    void testNonDefaultIntervalYearToMonth() {
        assumeExasol7OrLower();
        typeAssertionFor("INTERVAL YEAR(4) TO MONTH") // 4 digits for year
                .withValue("5-3") // sample interval of 5 years and 3 months
                .expectTypeOf("INTERVAL YEAR(4) TO MONTH") //
                .expectDescribeType("INTERVAL YEAR(4) TO MONTH") //
                .expectResultSetType(expectDataType("INTERVAL YEAR TO MONTH")) //
                .expectValue("+0005-03") // 4 digits for year
                .runAssert();
    }

    @Override
    @Test
    void testNonDefaultIntervalDayToSecond() {
        assumeExasol7OrLower();
        // day: 4 digits, seconds: 6 digits after decimal point
        typeAssertionFor("INTERVAL DAY(4) TO SECOND(6)").withValue("2 12:50:10.123") //
                .expectTypeOf("INTERVAL DAY(4) TO SECOND(6)") //
                .expectDescribeType("INTERVAL DAY(4) TO SECOND(6)") //
                .expectResultSetType(expectDataType("INTERVAL DAY TO SECOND")) //
                .expectValue("+0002 12:50:10.123000") //
                .runAssert();
    }

    @Override
    @Test
    void testNonDefaultHashType() {
        assumeExasol7OrLower();
        typeAssertionFor("HASHTYPE(4 BYTE)").withValue("550e8400") //
                .expectDescribeType("HASHTYPE(4 BYTE)") //
                .expectTypeOf("HASHTYPE(4 BYTE)") //
                .expectResultSetType(expectDataType("HASHTYPE")) //
                .runAssert();
    }

    @Override
    @Test
    void testCastVarcharAsGeometry() {
        assumeExasol7OrLower();
        castFrom("VARCHAR(20)").to("GEOMETRY(5)").input("POINT(2 5)").accept(expectDataType("GEOMETRY"))
                .verify("POINT (2 5)");
    }

    @Override
    @Test
    void testDefaultHashType() {
        assumeExasol7OrLower();
        typeAssertionFor("HASHTYPE").withValue("550e8400-e29b-11d4-a716-446655440000")
                .expectDescribeType("HASHTYPE(16 BYTE)") //
                .expectTypeOf("HASHTYPE(16 BYTE)") //
                .expectResultSetType(expectDataType("HASHTYPE")) //
                .expectValue("550e8400e29b11d4a716446655440000") //
                .runAssert();
    }

    @Override
    @Test
    void testIntervalYearToMonthMapping() {
        assumeExasol7OrLower();
        final Table table = createSingleColumnTable("INTERVAL YEAR (9) TO MONTH")//
                .insert("-999999999-11") //
                .insert("-1-1") //
                .insert("0-0") //
                .insert("1-1") //
                .insert("999999999-11");
        // why is precision 9 missing in expected data type?
        assertVirtualTableContents(table, table(expectDataType("INTERVAL YEAR TO MONTH")) //
                .row("-999999999-11") //
                .row("-000000001-01") //
                .row("+000000000-00") //
                .row("+000000001-01") //
                .row("+999999999-11") //
                .matches());
    }

    @Override
    @Test
    void testIntervalDayToSecondMapping() {
        assumeExasol7OrLower();
        final Table table = createSingleColumnTable("INTERVAL DAY (9) TO SECOND") //
                .insert("-999999999 23:59:59.999") //
                .insert("-1 12:34:56.789") //
                .insert("0 00:00:00.000") //
                .insert("1 12:34:56.789") //
                .insert("999999999 23:59:59.999");
        // why is precision 9 missing in expected data type?
        assertVirtualTableContents(table, table(expectDataType("INTERVAL DAY TO SECOND")) //
                .row("-999999999 23:59:59.999") //
                .row("-000000001 12:34:56.789") //
                .row("+000000000 00:00:00.000") //
                .row("+000000001 12:34:56.789") //
                .row("+999999999 23:59:59.999") //
                .matches());
    }

    @Override
    @Test
    void testCastVarcharAsIntervalDayToSecond() {
        assumeExasol7OrLower();
        // why is precision 5 and 2 missing in expected data type?
        castFrom("VARCHAR(30)").to("INTERVAL DAY (5) TO SECOND (2)").input("+00003 12:50:10.12")
                .accept(expectDataType("INTERVAL DAY TO SECOND")).verify("+00003 12:50:10.12");
    }

    @Override
    @Test
    void testCastVarcharAsIntervalYearToMonth() {
        assumeExasol7OrLower();
        // why is precision 5 missing in expectedDataType?
        castFrom("VARCHAR(30)").to("INTERVAL YEAR (5) TO MONTH").input("+00004-06")
                .accept(expectDataType("INTERVAL YEAR TO MONTH")).verify("+00004-06");
    }

    @Override
    @Test
    void testCaseEqual() {
        assumeExasol7OrLower();
        final Table table = createSingleColumnTable("INTEGER").insert(1).insert(2).insert(3);
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT CASE C1 WHEN 1 THEN 'YES' WHEN 2 THEN 'PERHAPS' ELSE 'NO' END FROM " //
                + getVirtualTableName(this.testVirtualSchema, table), //
                table().row("YES").row("PERHAPS").row("NO").matches());
    }

    @Override
    @Test
    void testCaseGreaterThan() {
        assumeExasol7OrLower();
        final Table table = createSingleColumnTable("INTEGER").insert(1).insert(2).insert(3);
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        assertVsQuery("SELECT CASE WHEN C1 > 1 THEN 'YES' ELSE 'NO' END FROM " //
                + getVirtualTableName(this.testVirtualSchema, table), //
                table().row("NO").row("YES").row("YES").matches());
    }
}
