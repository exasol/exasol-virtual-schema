package com.exasol.adapter.dialects.exasol;

import java.util.*;

/**
 * This class exercises a set of tests defined in the base class on a local Exasol, using {@code IMPORT} via a JDBC
 * connection.
 * <p>
 * In this case the Adapter uses the same JDBC connection to attach to the database that the ExaLoader needs for running
 * the {@code IMPORT}.
 * </p>
 */
class ExasolSqlDialectJdbcConnectionIT extends AbstractRemoteExasolVirtualSchemaConnectionIT {
    @Override
    protected Map<String, String> getConnectionSpecificVirtualSchemaProperties() {
        return Collections.emptyMap();
    }

    @Override
    protected Set<String> expectVarcharFor() {
        return isVersionOrHigher(7, 1, 16) //
                ? Collections.emptySet() //
                : Set.of("GEOMETRY", "INTERVAL", "INTERVAL YEAR TO MONTH", "INTERVAL DAY TO SECOND");
    }
}
