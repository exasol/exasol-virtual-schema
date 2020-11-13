package com.exasol.adapter.dialects.exasol;

import com.exasol.bucketfs.BucketAccessException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

@Tag("integration")
@Testcontainers
public class ExasolSqlDialectAgainstExasolSixIT extends AbstractExasolSqlDialectIntegrationTest {
    @BeforeAll
    static void beforeAll() throws InterruptedException, SQLException, TimeoutException, BucketAccessException {
        hostAndDefaultPort = "localhost:8888";
        dockerImageReference = "exasol/docker-db:6.2.11-d1";
        startSetUp();
    }
}