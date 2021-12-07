package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.sql.Types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.BaseIdentifierConverter;
import com.exasol.adapter.jdbc.JDBCTypeDescription;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.metadata.DataType.ExaCharset;

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
    void testMapJdbcTypeGeometryDifferentTypeName() {
        assertTypeMapped(geometry(2222).typeName("other"), DataType.createGeometry(2222));
    }

    @Test
    void testMapJdbcTypeGeometryDifferentJdbcType() {
        assertTypeMapped(geometry(2222).jdbcType(Types.VARCHAR), DataType.createGeometry(2222));
    }

    @Test
    void testMapJdbcTypeTimestamp() {
        assertTypeMapped(timestamp(), DataType.createTimestamp(false));
    }

    @Test
    void testMapJdbcTypeTimestampDifferentJdbcType() {
        assertTypeMapped(timestamp().jdbcType(Types.VARCHAR), DataType.createTimestamp(false));
    }

    @Test
    void testMapJdbcTypeTimestampDifferentTypeName() {
        assertTypeMapped(timestamp().typeName("other"), DataType.createTimestamp(false));
    }

    @Test
    void testMapJdbcTypeTimestampWithTimezone() {
        assertTypeMapped(timestampWithTimeZone(), DataType.createTimestamp(true));
    }

    @Test
    void testMapJdbcTypeTimestampWithTimezoneDifferentJdbcType() {
        assertTypeMapped(timestampWithTimeZone().jdbcType(Types.VARCHAR), DataType.createTimestamp(true));
    }

    @Test
    void testMapJdbcTypeTimestampWithTimezoneDifferentTypeNameFallBackToTimestampWithoutTimezone() {
        assertTypeMapped(timestampWithTimeZone().typeName("other"), DataType.createTimestamp(false));
    }

    @Test
    void testMapJdbcTypeHashtype() {
        assertTypeMapped(hashtype(16), DataType.createHashtype(8));
    }

    @Test
    void testMapJdbcTypeHashtypeWithZeroSizeReturnsDefaultSize() {
        assertTypeMapped(hashtype(0), DataType.createHashtype(16));
    }

    @Test
    void testMapJdbcTypeHashtypeDifferentJdbcType() {
        assertTypeMapped(hashtype(16).jdbcType(Types.VARCHAR), DataType.createHashtype(8));
    }

    @Test
    void testMapJdbcTypeHashtypeDifferentTypeName() {
        assertTypeMapped(hashtype(16).typeName("other"), DataType.createHashtype(8));
    }

    @Test
    void testMapJdbcTypeHashtypeDifferentSize() {
        assertTypeMapped(hashtype(8), DataType.createHashtype(4));
    }

    @Test
    void testMapJdbcTypeHashtypeOddSize() {
        assertTypeMapped(hashtype(7), DataType.createHashtype(3));
    }

    @Test
    void testMapJdbcTypeIntervalYearToMonth() {
        assertTypeMapped(intervalYearToMonth(2), DataType.createIntervalYearMonth(2));
    }

    @Test
    void testMapJdbcTypeIntervalYearToMonthDifferentPrecision() {
        assertTypeMapped(intervalYearToMonth(5), DataType.createIntervalYearMonth(5));
    }

    @Test
    void testMapJdbcTypeIntervalYearToMonthZeroPrecisionReturnsDefaultPrecision() {
        assertTypeMapped(intervalYearToMonth(0), DataType.createIntervalYearMonth(2));
    }

    @Test
    void testMapJdbcTypeIntervalYearToMonthJdbcTypeVarcharUsesDefaultPrecision() {
        assertTypeMapped(intervalYearToMonth(5).jdbcType(Types.VARCHAR), DataType.createVarChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeIntervalYearToMonthJdbcTypeUnknown() {
        assertTypeMapped(intervalYearToMonth(5).jdbcType(Integer.MAX_VALUE), DataType.createUnsupported());
    }

    @Test
    void testMapJdbcTypeIntervalYearToMonthDifferentTypeNameUnsupported() {
        assertTypeMapped(intervalYearToMonth(2).typeName("other"), DataType.createIntervalYearMonth(2));
    }

    @Test
    void testMapJdbcTypeIntervalYearToMonthDifferentTypeNameUnsupportedNonDefaultPrecision() {
        assertTypeMapped(intervalYearToMonth(3).typeName("other"), DataType.createIntervalYearMonth(3));
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecond() {
        assertTypeMapped(intervalDayToSecond(2, 3), DataType.createIntervalDaySecond(2, 3));
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecondZeroFractionReturnsDefault() {
        assertTypeMapped(intervalDayToSecond(5, 0), DataType.createIntervalDaySecond(2, 3));
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecondDifferentValues() {
        assertTypeMapped(intervalDayToSecond(5, 1), DataType.createIntervalDaySecond(5, 1));
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecondJdbcTypeVarchar() {
        assertTypeMapped(intervalDayToSecond(2, 3).jdbcType(Types.VARCHAR), DataType.createVarChar(2, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecondJdbcTypeVarcharZeroFractionReturnsDefault() {
        assertTypeMapped(intervalDayToSecond(5, 0).jdbcType(Types.VARCHAR), DataType.createVarChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecondJdbcTypeUnknownReturnsUnsupportedType() {
        assertTypeMapped(intervalDayToSecond(2, 3).jdbcType(Integer.MAX_VALUE), DataType.createUnsupported());
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecondOtherTypeName() {
        assertTypeMapped(intervalDayToSecond(2, 3).typeName("other"), DataType.createIntervalDaySecond(2, 3));
    }

    @Test
    void testMapJdbcTypeIntervalDayToSecondOtherTypeNameNonDefaultValues() {
        assertTypeMapped(intervalDayToSecond(4, 6).typeName("other"), DataType.createIntervalDaySecond(4, 6));
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
        }

        public JdbcTypeBuilder jdbcType(final int jdbcType) {
            this.jdbcType = jdbcType;
            return this;
        }

        public JdbcTypeBuilder decimalScale(final int decimalScale) {
            this.decimalScale = decimalScale;
            return this;
        }

        public JdbcTypeBuilder precisionOrSize(final int precisionOrSize) {
            this.precisionOrSize = precisionOrSize;
            return this;
        }

        public JdbcTypeBuilder byteSize(final int byteSize) {
            this.byteSize = byteSize;
            return this;
        }

        public JdbcTypeBuilder typeName(final String typeName) {
            this.typeName = typeName;
            return this;
        }

        public JDBCTypeDescription build() {
            return new JDBCTypeDescription(this.jdbcType, this.decimalScale, this.precisionOrSize, this.byteSize,
                    this.typeName);
        }
    }
}