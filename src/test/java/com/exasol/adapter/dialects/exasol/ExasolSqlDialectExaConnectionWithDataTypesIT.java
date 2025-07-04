package com.exasol.adapter.dialects.exasol;

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
                () -> query("alter virtual schema {0} set EXA_CONNECTION = Null", name));
        final String expected = PropertyValidationException.class.getName() + ": E-VSCJDBC-17";
        assertThat(exception.getMessage(), containsString(expected));
    }

    private ResultSet explainVirtual(final String sql) throws SQLException {
        return query("EXPLAIN VIRTUAL " + sql);
    }
}
