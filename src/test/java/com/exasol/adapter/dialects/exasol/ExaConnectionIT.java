package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.PATH_TO_VIRTUAL_SCHEMAS_JAR;
import static com.exasol.adapter.dialects.exasol.IntegrationTestConfiguration.VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer.NoDriverFoundException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.exasol.bucketfs.Bucket;
import com.exasol.bucketfs.BucketAccessException;
import com.exasol.containers.ExasolContainer;
import com.exasol.dbbuilder.dialects.Table;
import com.exasol.dbbuilder.dialects.exasol.*;
import com.exasol.matcher.ResultSetStructureMatcher;

@Testcontainers
class ExaConnectionIT {
    @Container
    static final ExasolContainer<? extends ExasolContainer<?>> CONTAINER = new ExasolContainer<>().withReuse(true);

    @BeforeAll
    static void beforeAll() throws InterruptedException, BucketAccessException, TimeoutException {
        final Bucket bucket = CONTAINER.getDefaultBucket();
        bucket.uploadFile(PATH_TO_VIRTUAL_SCHEMAS_JAR, VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION);
    }

    @Test
    void testPasswordNotVisibleInImportFromExa() throws NoDriverFoundException, SQLException {
        final Connection connection = CONTAINER.createConnection("");
        final ExasolObjectFactory factory = new ExasolObjectFactory(connection);
        final Table table = createSourceSchema(factory, "EXA_CONNECTION_TEST_SOURCE_SCHEMA");
        final VirtualSchema virtualSchema = createVirtualSchema(factory, "EXA_CONNECTION_TEST_VS");
        final String sql = "SELECT * FROM " + virtualSchema.getFullyQualifiedName() + "." + table.getName();
        assertThat(explainVirtual(connection, sql), //
                ResultSetStructureMatcher.table()
                        .row(anything(), not(containsString(CONTAINER.getPassword())), anything(), anything())
                        .matches());
    }

    private Table createSourceSchema(final ExasolObjectFactory factory, final String name) {
        final ExasolSchema sourceSchema = factory.createSchema(name);
        return sourceSchema.createTable("T1", "C1", "VARCHAR(20)").insert("Hello.");
    }

    private AdapterScript createAdapterScript(final ExasolSchema schema, final String adapterScriptName) {
        return schema.createAdapterScriptBuilder().name(adapterScriptName)
                .bucketFsContent("com.exasol.adapter.RequestDispatcher",
                        "/buckets/bfsdefault/default/" + VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION)
                .language(AdapterScript.Language.JAVA).build();
    }

    private VirtualSchema createVirtualSchema(final ExasolObjectFactory factory, final String virtualSchemaName) {
        final ExasolSchema schema = factory.createSchema("EXA_CONNECTION_TEST_SCHEMA");
        final AdapterScript adapterScript = createAdapterScript(schema, "EXA_CONNECTION_TEST_ADAPTER");
        final String hostAndPort = "localhost:" + CONTAINER.getDefaultInternalDatabasePort();
        final ConnectionDefinition adapterConnection = factory.createConnectionDefinition("ADAPTER_CONNECTION",
                "jdbc:exa:" + hostAndPort, CONTAINER.getUsername(), CONTAINER.getPassword());
        final ConnectionDefinition importerConnection = factory.createConnectionDefinition("IMPORTER_CONNECTION",
                hostAndPort, CONTAINER.getUsername(), CONTAINER.getPassword());
        return factory.createVirtualSchemaBuilder(virtualSchemaName) //
                .adapterScript(adapterScript) //
                .dialectName("EXASOL") //
                .connectionDefinition(adapterConnection) //
                .properties(Map.of("IMPORT_FROM_EXA", "true", "EXA_CONNECTION", importerConnection.getName())) //
                .build();
    }

    private ResultSet explainVirtual(final Connection connection, final String sql) throws SQLException {
        return connection.createStatement().executeQuery("EXPLAIN VIRTUAL " + sql);
    }
}