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

import org.junit.jupiter.api.*;
import org.testcontainers.containers.JdbcDatabaseContainer.NoDriverFoundException;

import com.exasol.adapter.properties.PropertyValidationException;
import com.exasol.dbbuilder.dialects.Table;
import com.exasol.dbbuilder.dialects.exasol.ConnectionDefinition;
import com.exasol.dbbuilder.dialects.exasol.VirtualSchema;

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

    @Test
    void testPasswordNotVisibleInImportFromExa() throws NoDriverFoundException, SQLException {
        final Table table = this.sourceSchema.createTable("T1", "C1", "VARCHAR(20)").insert("Hello.");
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        final String sql = "SELECT * FROM " + this.virtualSchema.getFullyQualifiedName() + ".\"" + table.getName()
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
    void testAlterVirtualSchemaTriggersPropertyValidation() throws SQLException {
        this.virtualSchema = createVirtualSchema(this.sourceSchema);
        final String name = this.virtualSchema.getFullyQualifiedName();
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
        final Table table = createSingleColumnTable("CHAR(20) ASCII").insert("sun").insert("rain");
        assertVirtualTableContents(table, table("VARCHAR").row(pad("sun", 20)).row(pad("rain", 20)).matches());
    }

    @Override
    @Test
    void testCharMappingUtf8() {
        verifyCharMappingUtf8("VARCHAR");
    }

    @Override
    @Test
    void testCastVarcharToChar() {
        castFrom("VARCHAR(20)").to("CHAR(40)").input("Hello.").accept("VARCHAR").verify(pad("Hello.", 40));
    }

    @Test
    void joinHashtype() throws java.sql.SQLException {
        final Table virtualTable = sourceSchema.createTableBuilder("VIRTUAL").column("VHASH", "HASHTYPE(16 BYTE)")
                .build();
        final Table realTable = objectFactory.createSchema("OTHER").createTableBuilder("REAL")
                .column("RHASH", "HASHTYPE(16 BYTE)").build();
        final VirtualSchema virtualSchema = createVirtualSchema(this.sourceSchema);
        try {
            final String sql = "select * from " + virtualSchema.getFullyQualifiedName() + "." + virtualTable.getName()
                    + " INNER JOIN " + realTable.getFullyQualifiedName() + " ON VHASH = RHASH";
            assertThat(query(sql), table("HASHTYPE", "HASHTYPE").matches());
        } finally {
            virtualSchema.drop();
        }
    }
}
