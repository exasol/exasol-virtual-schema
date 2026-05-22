package com.exasol.adapter.dialects.exasol;

import com.exasol.adapter.dialects.*;
import com.exasol.logging.VersionCollector;

/**
 * Factory for the Exasol SQL dialect.
 */
public class ExasolSqlDialectFactory implements SqlDialectFactory {
    @Override
    public String getSqlDialectName() {
        return ExasolSqlDialect.NAME;
    }

    @Override
    public SqlDialect createSqlDialect(final JDBCAdapterContext context) {
        return new ExasolSqlDialect(context);
    }

    @Override
    public String getSqlDialectVersion() {
        final VersionCollector versionCollector = new VersionCollector(
                "META-INF/maven/com.exasol/exasol-virtual-schema/pom.properties");
        return versionCollector.getVersionNumber();
    }

    @Override
    public String getAdapterProjectShortTag() {
        return "VSEXA";
    }
}
