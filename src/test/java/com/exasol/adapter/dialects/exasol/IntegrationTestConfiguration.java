package com.exasol.adapter.dialects.exasol;

import java.nio.file.Path;

public final class IntegrationTestConfiguration {
    private static final String DEFAULT_DOCKER_DB_REFERENCE = "7.0.2";
    public static final String VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION = "virtual-schema-dist-6.0.0-exasol-3.1.1.jar";
    public static final Path PATH_TO_VIRTUAL_SCHEMAS_JAR = Path.of("target", VIRTUAL_SCHEMAS_JAR_NAME_AND_VERSION);

    private IntegrationTestConfiguration() {
        // prevent instantiation
    }

    /**
     * Get the {@code docker-db} image reference.
     * <p>
     * This reference can be overridden by setting the Java property {@code com.exasol.dockerdb.image}. If the property
     * is not set, then the default reference provided with the integration tests is used instead.
     * </p>
     *
     * @return reference to the {@code docker-db} image.
     */
    public static String getDockerImageReference() {
        final String reference = System.getProperty("com.exasol.dockerdb.image");
        return reference == null ? DEFAULT_DOCKER_DB_REFERENCE : reference;
    }
}