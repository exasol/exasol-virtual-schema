package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.BaseIdentifierConverter;
import com.exasol.adapter.jdbc.JDBCTypeDescription;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.metadata.DataType.ExaCharset;

class ExasolColumnMetadataReaderTest {
    private ExasolColumnMetadataReader exasolColumnMetadataReader;

    private ExaMetadata exaMetadataMock;

    @BeforeEach
    void beforeEach() {
        prepareMocks("8.34.0");
    }

    private void prepareMocks(String exaDbVersion) {
        this.exaMetadataMock = Mockito.mock(ExaMetadata.class);
        when(exaMetadataMock.getDatabaseVersion()).thenReturn(exaDbVersion);
        this.exasolColumnMetadataReader = new ExasolColumnMetadataReader(null, AdapterProperties.emptyProperties(),
                exaMetadataMock, BaseIdentifierConverter.createDefault());
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
        assertMapJdbcTypeTimestamp("8.29.9");
        assertMapJdbcTypeTimestamp("8.34.0");
        assertMapJdbcTypeTimestamp("7.1.30");
    }

    private void assertMapJdbcTypeTimestamp(String exaDbVersion) {
        prepareMocks(exaDbVersion);
        assertTypeMapped(timestamp(3), DataType.createTimestamp(true, 3));
    }

    @Test
    void testMapJdbcTypeTimestampWithUnknownJdbcTypeName() {
        assertMapJdbcTypeTimestampWithUnknownJdbcTypeName("8.29.9");
        assertMapJdbcTypeTimestampWithUnknownJdbcTypeName("8.34.0");
        assertMapJdbcTypeTimestampWithUnknownJdbcTypeName("7.1.30");
    }

    private void assertMapJdbcTypeTimestampWithUnknownJdbcTypeName(String exaDbVersion) {
        prepareMocks(exaDbVersion);
        assertTypeMapped(timestamp(9).typeName("unknown"), DataType.createTimestamp(true, 9));
    }

    @Test
    void testMapJdbcTypeTimestampLocalTimezone() {
        assertMapJdbcTypeTimestampLocalTimezone("8.34.0");
        assertMapJdbcTypeTimestampLocalTimezone("8.29.9");
        assertMapJdbcTypeTimestampLocalTimezone("7.1.30");
    }

    private void assertMapJdbcTypeTimestampLocalTimezone(String exaDbVersion) {
        prepareMocks(exaDbVersion);
        assertTypeMapped(timestampWithTimeZone(3), DataType.createTimestamp(true, 3));
    }

    @Test
    void testMapJdbcTypeTimestampLocalTimezoneWithUnknownJdbcTypeName() {
        assertMapJdbcTypeTimestampLocalTimezoneWithUnknownJdbcTypeName("8.34.0");
        assertMapJdbcTypeTimestampLocalTimezoneWithUnknownJdbcTypeName("8.29.9");
        assertMapJdbcTypeTimestampLocalTimezoneWithUnknownJdbcTypeName("7.1.30");
    }

    private void assertMapJdbcTypeTimestampLocalTimezoneWithUnknownJdbcTypeName(String exaDbVersion) {
        prepareMocks(exaDbVersion);
        assertTypeMapped(timestampWithTimeZone(8).typeName("unknown"), DataType.createTimestamp(true, 8));
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
        assertTypeMapped(varchar(5).byteSize(5), DataType.createVarChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeVarcharMaxLength() {
        assertTypeMapped(varchar(2_000_001), DataType.createVarChar(2_000_000, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeVarcharWithUnknownJdbcTypeName() {
        assertTypeMapped(varchar(5).typeName("unknown"), DataType.createVarChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeCharUtf8() {
        assertTypeMapped(charType(5).byteSize(10), DataType.createChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeCharAscii() {
        assertTypeMapped(charType(5).byteSize(5), DataType.createChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeLongCharMappedToVarchar() {
        assertTypeMapped(charType(2001), DataType.createVarChar(2001, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeLongCharMappedToVarcharMaxVarcharLength() {
        assertTypeMapped(charType(2_000_001), DataType.createVarChar(2_000_000, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeCharWithUnknownJdbcTypeName() {
        assertTypeMapped(charType(5).typeName("unknown"), DataType.createChar(5, ExaCharset.UTF8));
    }

    @Test
    void testMapJdbcTypeDefault() {
        final JDBCTypeDescription jdbcTypeDescription = new JDBCTypeDescription(Types.BOOLEAN, 0, 3, 0, "BOOLEAN");
        assertThat(this.exasolColumnMetadataReader.mapJdbcType(jdbcTypeDescription), equalTo(DataType.createBool()));
    }

    @ParameterizedTest
    @CsvSource({ "GEOMETRY, 0", //
            "GEOMETRY(), 0", //
            "GEOMETRY(2222), 2222" })
    void testExtractSrid(final String input, final int expected) {
        assertThat(this.exasolColumnMetadataReader.extractSrid(input), equalTo(expected));
    }

    @ParameterizedTest
    @MethodSource("timestampTypeDescriptions")
    void testExtractTimestampPrecisionFromTypeString(final String typeDescription, final int expectedPrecision)
            throws SQLException {
        final ResultSet resultSetMock = Mockito.mock(ResultSet.class);
        when(resultSetMock.getString("COLUMN_SCHEMA")).thenReturn("MY_SCHEMA");
        when(resultSetMock.getString("COLUMN_TABLE")).thenReturn("MY_TABLE");
        when(resultSetMock.getString("COLUMN_NAME")).thenReturn("MY_COLUMN");

        final ExasolColumnMetadataReader readerSpy = Mockito.spy(this.exasolColumnMetadataReader);
        Mockito.doReturn(typeDescription).when(readerSpy).getTypeDescriptionStringForColumn(resultSetMock);

        final JDBCTypeDescription baseDescription = new JDBCTypeDescription(
                ExasolColumnMetadataReader.EXASOL_TIMESTAMP, 0, 0, 0, "TIMESTAMP WITH LOCAL TIME ZONE");

        // Call the precision extractor directly
        final JDBCTypeDescription result = readerSpy.extractTimestampPrecision(resultSetMock, baseDescription);

        assertThat(result.getPrecisionOrSize(), equalTo(expectedPrecision));
    }

    private static Stream<Arguments> timestampTypeDescriptions() {
        return Stream.of(
                Arguments.of("TIMESTAMP", 3), // default
                Arguments.of("TIMESTAMP(3)", 3),
                Arguments.of("TIMESTAMP(5)", 5),
                Arguments.of("TIMESTAMP(9)", 9),
                Arguments.of("TIMESTAMP(23)", 9),
                Arguments.of("TIMESTAMP WITH LOCAL TIME ZONE", 3), // default
                Arguments.of("TIMESTAMP(3) WITH LOCAL TIME ZONE", 3),
                Arguments.of("TIMESTAMP(5) WITH LOCAL TIME ZONE", 5),
                Arguments.of("TIMESTAMP(9) WITH LOCAL TIME ZONE", 9),
                Arguments.of("TIMESTAMP(23) WITH LOCAL TIME ZONE", 9)
        );
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
        return jdbcType("INTERVAL DAY TO SECOND").jdbcType(ExasolColumnMetadataReader.EXASOL_INTERVAL_DAY_TO_SECOND)
                .precisionOrSize(precision).decimalScale(fraction);
    }

    private static JdbcTypeBuilder intervalYearToMonth(final int precision) {
        return jdbcType("INTERVAL YEAR TO MONTH").jdbcType(ExasolColumnMetadataReader.EXASOL_INTERVAL_YEAR_TO_MONTH)
                .precisionOrSize(precision);
    }

    private static JdbcTypeBuilder hashtype(final int byteSize) {
        return jdbcType("HASHTYPE").jdbcType(ExasolColumnMetadataReader.EXASOL_HASHTYPE).byteSize(byteSize);
    }

    private static JdbcTypeBuilder timestampWithTimeZone(int precision) {
        return jdbcType("TIMESTAMP WITH LOCAL TIME ZONE").jdbcType(ExasolColumnMetadataReader.EXASOL_TIMESTAMP).precisionOrSize(precision);
    }

    private static JdbcTypeBuilder timestamp(int precision) {
        return jdbcType("TIMESTAMP").jdbcType(ExasolColumnMetadataReader.EXASOL_TIMESTAMP).precisionOrSize(precision);
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
