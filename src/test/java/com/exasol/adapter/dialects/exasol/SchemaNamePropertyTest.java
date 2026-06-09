package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.properties.PropertyValidationException;
import com.exasol.adapter.properties.PropertyValidator;

class SchemaNamePropertyTest {
    @Test
    void validatesPresentSchemaName() {
        final PropertyValidator validator = SchemaNameProperty.validator("EXASOL");
        final AdapterProperties properties = new AdapterProperties(Map.of(AdapterProperties.SCHEMA_NAME_PROPERTY,
                "MY_SCHEMA"));

        assertDoesNotThrow(() -> validator.validate(properties));
    }

    @Test
    void rejectsMissingSchemaName() {
        final PropertyValidator validator = SchemaNameProperty.validator("EXASOL");
        final AdapterProperties properties = new AdapterProperties(Map.of());

        final PropertyValidationException exception = assertThrows(PropertyValidationException.class,
                () -> validator.validate(properties));
        assertThat(exception.getMessage(),
                equalTo("E-VSEXA-6: EXASOL virtual schema dialect requires to specify a schema name."
                        + " Please specify a schema name using property 'SCHEMA_NAME'."));
    }
}
