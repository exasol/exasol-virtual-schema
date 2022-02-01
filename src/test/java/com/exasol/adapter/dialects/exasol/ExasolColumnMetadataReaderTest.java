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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExasolColumnMetadataReaderTest {
    private ExasolColumnMetadataReader exasolColumnMetadataReader;

    @BeforeEach
    void beforeEach() {
        this.exasolColumnMetadataReader = new ExasolColumnMetadataReader(null, AdapterProperties.emptyProperties(),
                BaseIdentifierConverter.createDefault());
    }

    @Test
    void testMapJdbcTypeGeometry() {
        assertTypeMapped(geometry(2222), DataType.createGeometry(2222));
    }

    @Test
    void testMapJdbcTypeGeometryWithUnknownJdbcTypeName() {
        assertTypeMapped(geometry(2222).typeName("unknown"), DataType.createGeometry(2222));
    }

    @Test
    void testMapJdbcTypeTimestamp() {
        assertTypeMapped(timestamp(), DataType.createTimestamp(true));
    }

    @Test
    void testMapJdbcTypeTimestampWithUnknownJdbcTypeName() {
        assertTypeMapped(timestamp().typeName("unknown"), DataType.createTimestamp(true));
    }

    @Test
    void testMapJdbcTypeTimestampLocalTimezone() {
        assertTypeMapped(timestampWithTimeZone(), DataType.createTimestamp(true));
    }

    @Test
    void testMapJdbcTypeTimestampLocalTimezoneWithUnknownJdbcTypeName() {
        assertTypeMapped(timestampWithTimeZone().typeName("unknown"), DataType.createTimestamp(true));
    }

    @Test
    void testMapJdbcTypeHashtype() {
        assertTypeMapped(hashtype(16), DataType.createHashtype(16));
    }

    @Test
    void testMapJdbcTypeHashtypeWithUnknownJdbcTypeName() {
        assertTypeMapped(hashtype(16).typeName("unknown"), DataType.createHashtype(16));
    }

    @Test
    void testMapJdbcTypeHashtypeWithVarcharJdbcTypeWithByteSize() {
        assertTypeMapped(hashtype(32).byteSize(32).precisionOrSize(42).jdbcType(Types.VARCHAR),
                DataType.createHashtype(16));
    }

    @Test
    void testMapJdbcTypeHashtypeWithVarcharJdbcTypeWithPrecision() {
        assertTypeMapped(hashtype(0).byteSize(0).precisionOrSize(42).jdbcType(Types.VARCHAR),
                DataType.createHashtype(21));
    }

    @Test
    void testMapJdbcTypeIntervalYearToMonth() {
        assertTypeMapped(intervalYearToMonth(4), DataType.createIntervalYearMonth(4));
    }

    @Test
    void testMapJdbcTypeIntervalYearToMonthWithUnknownTypeName() {
        assertTypeMapped(intervalYearToMonth(4).typeName("unknown"), DataType.createIntervalYearMonth(4));
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecond() {
        assertTypeMapped(intervalDayToSecond(4, 6), DataType.createIntervalDaySecond(4, 6));
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecondUnknownTypeName() {
        assertTypeMapped(intervalDayToSecond(4, 6).typeName("unknown"), DataType.createIntervalDaySecond(4, 6));
    }

    @Test
    void testMapJdbcTypeVarchar() {
        assertTypeMapped(varchar(5), DataType.createVarChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeVarcharAscii() {
        assertTypeMapped(varchar(5).byteSize(5), DataType.createVarChar(5, ExaCharset.ASCII));
    }

    @Test
    void testMapJdbcTypeVarcharMaxLength() {
        assertTypeMapped(varchar(2_000_001), DataType.createVarChar(2_000_000, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeVarcharWithUnknownJdbcTypenName() {
        assertTypeMapped(varchar(5).typeName("unknown"), DataType.createVarChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeCharUtf8() {
        assertTypeMapped(charType(5).byteSize(10), DataType.createChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeCharAscii() {
        assertTypeMapped(charType(5).byteSize(5), DataType.createChar(5, ExaCharset.ASCII));
    }

    @Test
    void testMapJdbcTypeLongCharMappedToVarchar() {
        assertTypeMapped(charType(2001), DataType.createVarChar(2001, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeLongCharMappedToVarcharMaxVarcharLenth() {
        assertTypeMapped(charType(2_000_001), DataType.createVarChar(2_000_000, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeCharWithUnknownJdbcTypenName() {
        assertTypeMapped(charType(5).typeName("unknown"), DataType.createChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeDefault() {
        final JDBCTypeDescription jdbcTypeDescription = new JDBCTypeDescription(Types.BOOLEAN, 0, 0, 0, "BOOLEAN");
        assertThat(this.exasolColumnMetadataReader.mapJdbcType(jdbcTypeDescription), equalTo(DataType.createBool()));
    }

    @ParameterizedTest
    @CsvSource({ "GEOMETRY, 3857", //
            "GEOMETRY(), 3857", //
            "GEOMETRY(2222), 2222" })
    void testExtractSrid(final String input, final int expected) {
        assertThat(this.exasolColumnMetadataReader.extractSrid(input), equalTo(expected));
    }

    private void assertTypeMapped(final JdbcTypeBuilder typeBuilder, final DataType expectedDataType) {
        assertThat(this.exasolColumnMetadataReader.mapJdbcType(typeBuilder.build()), equalTo(expectedDataType));
    }

    private static JdbcTypeBuilder varchar(final int length) {
        return jdbcType("VARCHAR").jdbcType(Types.VARCHAR).precisionOrSize(length);
    }

    private static JdbcTypeBuilder charType(final int length) {
        return jdbcType("CHAR").jdbcType(Types.CHAR).precisionOrSize(length);
    }

    private static JdbcTypeBuilder intervalDayToSecond(final int precision, final int fraction) {
        return jdbcType("INTERVAL DAY TO SECOND").jdbcType(ExasolColumnMetadataReader.EXASOL_INTERVAL_DAY_TO_SECONDS)
                .precisionOrSize(precision).decimalScale(fraction);
    }

    private static JdbcTypeBuilder intervalYearToMonth(final int precision) {
        return jdbcType("INTERVAL YEAR TO MONTH").jdbcType(ExasolColumnMetadataReader.EXASOL_INTERVAL_YEAR_TO_MONTHS)
                .precisionOrSize(precision);
    }

    private static JdbcTypeBuilder hashtype(final int byteSize) {
        return jdbcType("HASHTYPE").jdbcType(ExasolColumnMetadataReader.EXASOL_HASHTYPE).byteSize(byteSize);
    }

    private static JdbcTypeBuilder timestampWithTimeZone() {
        return jdbcType("TIMESTAMP WITH LOCAL TIME ZONE").jdbcType(ExasolColumnMetadataReader.EXASOL_TIMESTAMP);
    }

    private static JdbcTypeBuilder timestamp() {
        return jdbcType("TIMESTAMP").jdbcType(ExasolColumnMetadataReader.EXASOL_TIMESTAMP);
    }

    private static JdbcTypeBuilder geometry(final int srid) {
        return jdbcType("GEOMETRY").jdbcType(ExasolColumnMetadataReader.EXASOL_GEOMETRY).precisionOrSize(srid);
    }

    private static JdbcTypeBuilder jdbcType(final String typeName) {
        return new JdbcTypeBuilder().jdbcType(0).typeName(typeName).byteSize(0).decimalScale(0);
    }

    private static class JdbcTypeBuilder {
        private int jdbcType;
        private int decimalScale;
        private int precisionOrSize;
        private int byteSize;
        private String typeName;

        private JdbcTypeBuilder() {
            // Not instantiable
        }

        JdbcTypeBuilder jdbcType(final int jdbcType) {
            this.jdbcType = jdbcType;
            return this;
        }

        JdbcTypeBuilder decimalScale(final int decimalScale) {
            this.decimalScale = decimalScale;
            return this;
        }

        JdbcTypeBuilder precisionOrSize(final int precisionOrSize) {
            this.precisionOrSize = precisionOrSize;
            return this;
        }

        JdbcTypeBuilder byteSize(final int byteSize) {
            this.byteSize = byteSize;
            return this;
        }

        JdbcTypeBuilder typeName(final String typeName) {
            this.typeName = typeName;
            return this;
        }

        JDBCTypeDescription build() {
            return new JDBCTypeDescription(this.jdbcType, this.decimalScale, this.precisionOrSize, this.byteSize,
                    this.typeName);
        }
    }
}