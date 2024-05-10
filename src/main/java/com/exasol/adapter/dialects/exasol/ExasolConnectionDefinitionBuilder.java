package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.AdapterProperties.CONNECTION_NAME_PROPERTY;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_CONNECTION_PROPERTY;

import com.exasol.ExaConnectionInformation;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.jdbc.BaseConnectionDefinitionBuilder;
import com.exasol.errorreporting.ExaError;

/**
 * This class implements an Exasol-specific connection definition builder.
 * <p>
 * In case of an <code>IMPORT FROM EXA</code> we actually need two different connection definitions. The regular one for
 * reading the remote metadata in the Virtual Schema backend and a specialized one for the import statement, provided in
 * a separate property.
 */
public class ExasolConnectionDefinitionBuilder extends BaseConnectionDefinitionBuilder {
    @Override
    public String buildConnectionDefinition(final AdapterProperties properties,
            final ExaConnectionInformation exaConnectionInformation) {
        if (properties.isEnabled(ExasolProperties.EXASOL_IMPORT_PROPERTY)) {
            return buildImportFromExaConnectionDefinition(properties);
        } else {
            return super.buildConnectionDefinition(properties, exaConnectionInformation);
        }
    }

    private String buildImportFromExaConnectionDefinition(final AdapterProperties properties) {
        if (properties.containsKey(EXASOL_CONNECTION_PROPERTY) && properties.hasConnectionName()) {
            return "AT \"" + getExasolConnectionName(properties) + "\"";
        } else {
            throw new IllegalArgumentException(ExaError.messageBuilder("E-VSEXA-1") //
                    .message("Incomplete remote connection information") //
                    .mitigation("Please specify a named EXA connection {{exasol_connection_property}}"
                            + " for the ExaLoader and a named JDBC connection with {{connection_name_property}}"
                            + " for the Virtual Schema adapter.") //
                    .parameter("exasol_connection_property", EXASOL_CONNECTION_PROPERTY) //
                    .parameter("connection_name_property", CONNECTION_NAME_PROPERTY) //
                    .toString());
        }
    }

    private String getExasolConnectionName(final AdapterProperties properties) {
        return properties.get(EXASOL_CONNECTION_PROPERTY);
    }
}
