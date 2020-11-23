package com.exasol.adapter.dialects.exasol;

import java.util.Collections;
import java.util.Map;

/**
 * This class exercises a set of tests defined in the base class for on a local Exasol, using {@code IMPORT} via a JDBC
 * connection.
 * <p>
 * In this case the Adapter uses the same JDBC connection to attach to the database that the ExaLoader needs for running
 * the {@code IMPORT}.
 * </p>
 * <p>
 * This tests takes the following specialties of a local connection into account:
 * </p>
 * <ul>
 * <li>{@code INTERVAL} types are converted to {@code VARCHAR}</li>
 * <ul>
 */
class ExasolSqlDialectJdbcConnectionIT extends AbstractRemoteExasolVirtualSchemaConnectionIT {
    @Override
    protected Map<String, String> getConnectionSepcificVirtualSchemaProperties() {
        return Collections.emptyMap();
    }
}