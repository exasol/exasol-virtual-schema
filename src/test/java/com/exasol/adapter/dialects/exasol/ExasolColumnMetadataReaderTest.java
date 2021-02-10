package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.sql.Types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.BaseIdentifierConverter;
import com.exasol.adapter.jdbc.JDBCTypeDescription;
import com.exasol.adapter.metadata.DataType;

class ExasolColumnMetadataReaderTest {
    private ExasolColumnMetadataReader exasolColumnMetadataReader;

    @BeforeEach
    void beforeEach() {
        this.exasolColumnMetadataReader = new ExasolColumnMetadataReader(null, AdapterProperties.emptyProperties(),
                BaseIdentifierConverter.createDefault());
    }

    @Test
    void testMapJdbcTypeGeometry() {
        final JDBCTypeDescription jdbcTypeDescription = new JDBCTypeDescription(
                ExasolColumnMetadataReader.EXASOL_GEOMETRY, 0, 0, 0, "GEOMETRY");
        assertThat(this.exasolColumnMetadataReader.mapJdbcType(jdbcTypeDescription),
                equalTo(DataType.createGeometry(3857)));
    }

    @Test
    void testMapJdbcTypeTimestamp() {
        final JDBCTypeDescription jdbcTypeDescription = new JDBCTypeDescription(
                ExasolColumnMetadataReader.EXASOL_TIMESTAMP, 0, 0, 0, "TIMESTAMP");
        assertThat(this.exasolColumnMetadataReader.mapJdbcType(jdbcTypeDescription),
                equalTo(DataType.createTimestamp(true)));
    }

    @Test
    void testMapJdbcTypeHashtype() {
        final JDBCTypeDescription jdbcTypeDescription = new JDBCTypeDescription(
                ExasolColumnMetadataReader.EXASOL_HASHTYPE, 0, 0, 16, "HASHTYPE");
        assertThat(this.exasolColumnMetadataReader.mapJdbcType(jdbcTypeDescription),
                equalTo(DataType.createHashtype(16)));
    }

    @Test
    void testMapJdbcTypeDefault() {
        final JDBCTypeDescription jdbcTypeDescription = new JDBCTypeDescription(Types.BOOLEAN, 0, 0, 0, "BOOLEAN");
        assertThat(this.exasolColumnMetadataReader.mapJdbcType(jdbcTypeDescription), equalTo(DataType.createBool()));
    }
}