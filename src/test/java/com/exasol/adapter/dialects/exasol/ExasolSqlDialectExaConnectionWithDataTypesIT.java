package com.exasol.adapter.dialects.exasol;

import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.*;
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
 */
class ExasolSqlDialectExaConnectionWithDataTypesIT extends AbstractRemoteExasolVirtualSchemaConnectionIT {
    private static final String EXA_CONNECTION_NAME = "THE_EXA_CONNECTION";
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
        return Set.of();
    }

    @Override
    protected Map<String, String> getConnectionSpecificVirtualSchemaProperties() {
        return Map.of("IMPORT_FROM_EXA", "true", //
                "EXA_CONNECTION", this.exaConnection.getName(), //
                "GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA", "true");
    }

    // https://github.com/exasol/exasol-virtual-schema/issues/125
    @Test
    void testStringLiteralInSelectList() {
        final Table table = createSingleColumnTable("VARCHAR(20)").insert("value");
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        final String virtualTable = getVirtualTableName(this.testVirtualSchema, table);
        assertVsQuery("SELECT 'VS_EXA_META' AS VS_NAME, C1 FROM " + virtualTable,
                table("CHAR", "VARCHAR").row("VS_EXA_META", "value").matches());
    }

    // https://github.com/exasol/exasol-virtual-schema/issues/124
    @Test
    void testNullLiteralInUnionAll() {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        final String virtualTable = getVirtualTableName(this.testVirtualSchema, table);
        assertVsQuery("SELECT NULL AS X FROM " + virtualTable + " UNION ALL SELECT 1 FROM DUAL",
                table("SMALLINT").row((Short) null).row((short) 1).matches());
    }

    @Test
    void testTypedNullLiteral() {
        final Table table = createSingleColumnTable("BOOLEAN").insert(true);
        this.testVirtualSchema = createVirtualSchema(this.sourceSchema);
        final String virtualTableName = getVirtualTableName(this.testVirtualSchema, table);

        assertAll(
                () -> assertVsQuery("SELECT CAST(NULL AS DECIMAL(18,0)) FROM " + virtualTableName,
                        table("BIGINT").row((Object) null).matches()),
                () -> assertVsQuery("SELECT CAST(NULL AS DOUBLE) FROM " + virtualTableName,
                        table("DOUBLE PRECISION").row((Object) null).matches()));
    }

    @Test
    void testPasswordNotVisibleInImportFromExa() throws NoDriverFoundException, SQLException {
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
                () -> execute("alter virtual schema {0} set EXA_CONNECTION = Null", name));
        final String expected = PropertyValidationException.class.getName()
                + ": E-VSCJDBC-17: You defined the property 'IMPORT_FROM_EXA'. Please also define 'EXA_CONNECTION'.";
        assertThat(exception.getMessage(), containsString(expected));
    }

    private ResultSet explainVirtual(final String sql) throws SQLException {
        return query("EXPLAIN VIRTUAL " + sql);
    }
}
