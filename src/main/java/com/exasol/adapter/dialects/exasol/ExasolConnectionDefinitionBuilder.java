package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.AdapterProperties.*;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_CONNECTION_STRING_PROPERTY;

import java.util.logging.Logger;

import com.exasol.ExaConnectionInformation;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.jdbc.BaseConnectionDefinitionBuilder;

/**
 * This class implements an Exasol-specific connection definition builder.
 * <p>
 * In case of an <code>IMPORT FROM EXA</code> we actually need two different connection definitions. The regular one for
 * reading the remote metadata in the Virtual Schema backend and a specialized one for the import statement, provided in
 * a separate property.
 */
public class ExasolConnectionDefinitionBuilder extends BaseConnectionDefinitionBuilder {
    private static final Logger LOGGER = Logger.getLogger(ExasolConnectionDefinitionBuilder.class.getName());

    @Override
    public String buildConnectionDefinition(final AdapterProperties properties,
            final ExaConnectionInformation exaConnectionInformation) {
        if (properties.containsKey(ExasolProperties.EXASOL_IMPORT_PROPERTY)) {
            return buildImportFromExaConnectionDefinition(properties, exaConnectionInformation);
        } else {
            return super.buildConnectionDefinition(properties, exaConnectionInformation);
        }
    }

    private String buildImportFromExaConnectionDefinition(final AdapterProperties properties,
            final ExaConnectionInformation exaConnectionInformation) {
        if (properties.containsKey(EXASOL_CONNECTION_STRING_PROPERTY) && properties.hasConnectionName()) {
            return mixNamedConnectionWithExasolConnectionString(properties, exaConnectionInformation);
        } else {
            throw new IllegalArgumentException(
                    "Incomplete remote connection information. Please specify an Exasol connection string with property \""
                            + EXASOL_CONNECTION_STRING_PROPERTY + "\" and a named connection with \""
                            + CONNECTION_NAME_PROPERTY + "\".");
        }
    }

    private String mixNamedConnectionWithExasolConnectionString(final AdapterProperties properties,
            final ExaConnectionInformation exaConnectionInformation) {
        final String exasolConnectionString = getExasolConnectionString(properties);
        LOGGER.finer(() -> "Mixing Exasol connection string \"" + exasolConnectionString + "\" into named connection.");
        return getConnectionDefinition(exasolConnectionString, exaConnectionInformation.getUser(),
                exaConnectionInformation.getPassword());
    }

    private String getExasolConnectionString(final AdapterProperties properties) {
        return properties.get(EXASOL_CONNECTION_STRING_PROPERTY);
    }
}