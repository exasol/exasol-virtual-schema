package com.exasol.adapter.dialects.exasol;

import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.properties.PropertyValidationException;
import com.exasol.adapter.properties.PropertyValidator;
import com.exasol.errorreporting.ExaError;

/**
 * {@link PropertyValidator} for validation of a mandatory property.
 */
public class MandatoryProperty implements PropertyValidator {
    /**
     *
     * @param dialect  name of the current virtual schema dialect to be included in error message
     * @param element  label for the missing property value
     * @param property name of the property
     * @return new instance of {@link PropertyValidator} for validation of a mandatory property.
     */
    public static PropertyValidator validator(final String dialect, final String element, final String property) {
        return new MandatoryProperty(dialect, element, property);
    }

    private final String dialect;
    private final String element;
    private final String property;

    private MandatoryProperty(final String dialect, final String element, final String property) {
        this.dialect = dialect;
        this.element = element;
        this.property = property;
    }

    @Override
    public void validate(final AdapterProperties properties) throws PropertyValidationException {
        if (!properties.hasSchemaName()) {
            throw new PropertyValidationException(ExaError.messageBuilder("E-VSEXA-6")
                    .message("{{dialect|uq}} virtual schema dialect requires to specify a {{element1|uq}}.",
                            this.dialect, this.element) //
                    .mitigation("Please specify a {{element2|uq}} using property {{property}}.", //
                            this.element, this.property) //
                    .toString());
        }
    }
}