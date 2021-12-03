package com.exasol.adapter.dialects.exasol;

import java.util.Map;

/**
 * This class exercises a set of tests defined in the base class on a local Exasol, using {@code SELECT} instead of
 * {@code IMPORT}
 */
class ExasolSqlDialectLocalConnectionIT extends AbstractExasolSqlDialectIT {

    @Override
    protected Map<String, String> getConnectionSpecificVirtualSchemaProperties() {
        return Map.of("IS_LOCAL", "true");
    }
}
