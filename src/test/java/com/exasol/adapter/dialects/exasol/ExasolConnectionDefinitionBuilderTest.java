package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_CONNECTION_PROPERTY;
import static com.exasol.adapter.dialects.exasol.ExasolProperties.EXASOL_IMPORT_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.exasol.ExaConnectionInformation;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.jdbc.*;

class ExasolConnectionDefinitionBuilderTest extends AbstractConnectionDefinitionBuilderTestBase {
    private static final String EXA_CONNECTION_NAME = "EXA_CONNECTION";

    @BeforeEach
    void beforeEach() {
        this.rawProperties = new HashMap<>();
        this.exaConnectionInformation = mock(ExaConnectionInformation.class);
    }

    @Override
    protected ConnectionDefinitionBuilder createConnectionBuilderUnderTest() {
        return new ExasolConnectionDefinitionBuilder();
    }

    @Test
    void testBuildConnectionDefinitionForImportFromExaWithConnectionNameGiven() {
        mockExasolNamedConnection();
        setImportFromExaProperties();
        setConnectionNameProperty();
        assertThat(calculateConnectionDefinition(), equalTo("AT '" + EXA_CONNECTION_NAME + "'"));
    }

    private void setImportFromExaProperties() {
        this.rawProperties.put(EXASOL_IMPORT_PROPERTY, "true");
        this.rawProperties.put(EXASOL_CONNECTION_PROPERTY, EXA_CONNECTION_NAME);
    }

    @Test
    void testBuildConnectionDefinitionWithoutConnectionInfomationThrowsException() {
        setImportFromExaProperties();
        final AdapterProperties properties = new AdapterProperties(this.rawProperties);
        final BaseConnectionDefinitionBuilder builder = new BaseConnectionDefinitionBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.buildConnectionDefinition(properties, null));
    }
}