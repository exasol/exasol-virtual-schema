package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.jdbc.BaseTableMetadataReader;

class ExasolMetadataReaderTest {
    private ExasolMetadataReader exasolMetadataReader;

    private ExaMetadata exaMetadataMock;

    @BeforeEach
    void beforeEach() {
        this.exaMetadataMock = Mockito.mock(ExaMetadata.class);
        when(exaMetadataMock.getDatabaseVersion()).thenReturn("8.34.0");
        this.exasolMetadataReader = new ExasolMetadataReader(null, AdapterProperties.emptyProperties(), exaMetadataMock);
    }

    @Test
    void testGetTableMetadataReader() {
        assertThat(this.exasolMetadataReader.getTableMetadataReader(), instanceOf(BaseTableMetadataReader.class));
    }

    @Test
    void testGetColumnMetadataReader() {
        assertThat(this.exasolMetadataReader.getColumnMetadataReader(), instanceOf(ExasolColumnMetadataReader.class));
    }
}
