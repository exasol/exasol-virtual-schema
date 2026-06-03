package com.exasol.adapter.dialects.exasol;

import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.properties.PropertyValidationException;
import com.exasol.adapter.properties.PropertyValidator;
import com.exasol.errorreporting.ExaError;

/**
 * Validator for property {@link AdapterProperties#SCHEMA_NAME_PROPERTY}
 */
public class SchemaNameProperty {
    /**
     * @param dialect name of the current dialect
     * @return {@link PropertyValidator} for mandatory property {@link AdapterProperties#SCHEMA_NAME_PROPERTY}.
     */
    public static PropertyValidator validator(final String dialect) {
        return properties -> {
            if (!properties.hasSchemaName()) {
                throw new PropertyValidationException(ExaError.messageBuilder("E-VSEXA-6")
                        .message("{{dialect|uq}} virtual schema dialect requires to specify a {{element1|uq}}.",
                                dialect, "schema name")
                        .mitigation("Please specify a {{element2|uq}} using property {{property}}.",
                                "schema name", AdapterProperties.SCHEMA_NAME_PROPERTY)
                        .toString());
            }
        };
    }

    private SchemaNameProperty() {
        // only static usage
    }
}
