package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.BaseIdentifierConverter;
import com.exasol.adapter.jdbc.JDBCTypeDescription;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.metadata.DataType.ExaCharset;

@ExtendWith(MockitoExtension.class)
class ExasolColumnMetadataReaderTest {
    private static final String TYPE_DESCRIPTION_QUERY = "SELECT COLUMN_TYPE FROM SYS.EXA_ALL_COLUMNS"
            + " WHERE COLUMN_SCHEMA = ? AND COLUMN_TABLE = ? AND COLUMN_NAME = ?";

    private ExasolColumnMetadataReader exasolColumnMetadataReader;

    private ExaMetadata exaMetadataMock;

    @BeforeEach
    void beforeEach() {
        prepareMocks("8.34.0");
    }

    private void prepareMocks(final String exaDbVersion) {
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

    @ParameterizedTest
    @CsvSource({
            "8.34.0, -1, 3",
            "8.34.0, 0, 3",
            "8.34.0, 1, 1",
            "8.34.0, 5, 5",
            "8.34.0, 9, 9",
            "8.34.0, 23, 9",
            "8.29.9, -1, 3",
            "8.29.9, 0, 3",
            "8.29.9, 9, 3"
    })
    void testMapJdbcTypeTimestamp(final String exaDbVersion, final int decimalScale, final int expectedPrecision) {
        prepareMocks(exaDbVersion);
        assertAll(
                () -> assertTypeMapped(timestamp(decimalScale), DataType.createTimestamp(true, expectedPrecision)),
                () -> assertTypeMapped(timestamp(decimalScale).typeName("unknown"), DataType.createTimestamp(true, expectedPrecision)),
                () -> assertTypeMapped(timestampWithTimeZone(decimalScale), DataType.createTimestamp(true, expectedPrecision)),
                () -> assertTypeMapped(timestampWithTimeZone(decimalScale).typeName("unknown"), DataType.createTimestamp(true, expectedPrecision)));
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
        final JDBCTypeDescription jdbcTypeDescription = new JDBCTypeDescription(Types.BOOLEAN, 0, 0, 0, "BOOLEAN");
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
    @NullSource
    @ValueSource(strings = { "GEOMETRY", "GEOMETRY()", "GEOMETRY(abc)", "GEOMETRY(999999999999999999999999)", "GEOMETRY(-1)", "GEOMETRY(1.2)",
            "GEOMETRY(1,2)" })
    void testExtractSridFallsBackToDefaultForInvalidInput(final String input) {
        assertThat(this.exasolColumnMetadataReader.extractSrid(input), equalTo(0));
    }

    @Test
    void readJdbcTypeDescriptionFallsBackToDefaultSridForNullColumnType() throws SQLException {
        final ExasolColumnMetadataReader reader = reader(mockConnection(mockTypeDescriptionStatement(null)));

        final JDBCTypeDescription result = reader.readJdbcTypeDescription(
                remoteColumn(ExasolColumnMetadataReader.EXASOL_GEOMETRY));

        assertAll(
                () -> assertThat(result.getPrecisionOrSize(), equalTo(0)),
                () -> assertThat(result.getJdbcType(), equalTo(ExasolColumnMetadataReader.EXASOL_GEOMETRY)));
    }

    @ParameterizedTest
    @CsvSource({
            "'TIMESTAMP', 3",
            "'TIMESTAMP(3)', 3",
            "'TIMESTAMP(5)', 5",
            "'TIMESTAMP(9)', 9",
            "'TIMESTAMP(23)', 9",
            "'TIMESTAMP WITH LOCAL TIME ZONE', 3",
            "'TIMESTAMP(3) WITH LOCAL TIME ZONE', 3",
            "'TIMESTAMP(5) WITH LOCAL TIME ZONE', 5",
            "'TIMESTAMP(9) WITH LOCAL TIME ZONE', 9"
    })
    void testExtractTimestampPrecisionFromTypeString(final String typeDescription, final int expectedPrecision) {
        final ExasolColumnMetadataReader readerSpy = Mockito.spy(this.exasolColumnMetadataReader);
        final JDBCTypeDescription baseDescription = new JDBCTypeDescription(
                getJdbcType(typeDescription), expectedPrecision, 0, 0, typeDescription);
        final DataType result = readerSpy.mapJdbcType(baseDescription);
        assertThat(result.getPrecision(), equalTo(expectedPrecision));
    }

    @Test
    void readJdbcTypeDescriptionExtractsIntervalYearToMonthPrecision() throws SQLException {
        final PreparedStatement statementMock = mockTypeDescriptionStatement("INTERVAL YEAR(7) TO MONTH");
        final Connection connectionMock = mockConnection(statementMock);
        final ExasolColumnMetadataReader reader = reader(connectionMock);
        final JDBCTypeDescription result = reader.readJdbcTypeDescription(
                remoteColumn(ExasolColumnMetadataReader.EXASOL_INTERVAL_YEAR_TO_MONTH));
        assertAll(
                () -> assertThat(result.getPrecisionOrSize(), equalTo(7)),
                () -> verify(connectionMock).prepareStatement(TYPE_DESCRIPTION_QUERY),
                () -> verify(statementMock).setString(1, "SOURCE_SCHEMA"),
                () -> verify(statementMock).setString(2, "SOURCE_TABLE"),
                () -> verify(statementMock).setString(3, "SOURCE_COLUMN"));
    }

    @Test
    void readJdbcTypeDescriptionExtractsIntervalDayToSecondPrecision() throws SQLException {
        final ExasolColumnMetadataReader reader = reader(mockConnection(mockTypeDescriptionStatement(
                "INTERVAL DAY(4) TO SECOND(6)")));
        final JDBCTypeDescription result = reader.readJdbcTypeDescription(
                remoteColumn(ExasolColumnMetadataReader.EXASOL_INTERVAL_DAY_TO_SECOND));
        assertAll(
                () -> assertThat(result.getPrecisionOrSize(), equalTo(4)),
                () -> assertThat(result.getDecimalScale(), equalTo(6)));
    }

    @Test
    void readJdbcTypeDescriptionThrowsCompleteErrorMessageForInvalidIntervalYearToMonth() throws SQLException {
        final ExasolColumnMetadataReader reader = reader(mockConnection(mockTypeDescriptionStatement("INTERVAL YEAR")));
        final ResultSet remoteColumn = remoteColumn(ExasolColumnMetadataReader.EXASOL_INTERVAL_YEAR_TO_MONTH);
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reader.readJdbcTypeDescription(remoteColumn));
        assertThat(exception.getMessage(),
                equalTo("E-VSEXA-3: Failed to extract INTERVAL YEAR TO MONTH precision"));
    }

    @Test
    void readJdbcTypeDescriptionThrowsCompleteErrorMessageForInvalidIntervalDayToSecond() throws SQLException {
        final ExasolColumnMetadataReader reader = reader(mockConnection(mockTypeDescriptionStatement(
                "INTERVAL DAY TO SECOND")));
        final ResultSet remoteColumn = remoteColumn(ExasolColumnMetadataReader.EXASOL_INTERVAL_DAY_TO_SECOND);
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reader.readJdbcTypeDescription(remoteColumn));
        assertThat(exception.getMessage(),
                equalTo("E-VSEXA-2: Failed to extract INTERVAL DAY TO SECOND precision"));
    }

    @Test
    void readJdbcTypeDescriptionThrowsCompleteErrorMessageWhenDictionaryRowIsMissing(@Mock final Connection connectionMock, @Mock final ResultSet resultSetMock,
            @Mock final PreparedStatement statementMock) throws SQLException {
        when(resultSetMock.next()).thenReturn(false);
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(connectionMock.prepareStatement(TYPE_DESCRIPTION_QUERY)).thenReturn(statementMock);
        final ExasolColumnMetadataReader reader = reader(connectionMock);
        final ResultSet remoteColumn = remoteColumn(ExasolColumnMetadataReader.EXASOL_GEOMETRY);

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reader.readJdbcTypeDescription(remoteColumn));

        assertThat(exception.getMessage(),
                equalTo("E-VSEXA-7: Could not find a matching row in SYS.EXA_ALL_COLUMNS for column SOURCE_COLUMN in schema SOURCE_SCHEMA and table SOURCE_TABLE. This may be caused by missing privileges or concurrent schema changes."));
    }

    private int getJdbcType(final String typeDescription) {
        return typeDescription.contains("WITH LOCAL TIME ZONE") ? ExasolColumnMetadataReader.EXASOL_TIMESTAMP : Types.TIMESTAMP;
    }

    private ExasolColumnMetadataReader reader(final Connection connection) {
        return new ExasolColumnMetadataReader(connection, AdapterProperties.emptyProperties(), this.exaMetadataMock,
                BaseIdentifierConverter.createDefault());
    }

    private Connection mockConnection(final PreparedStatement statementMock) throws SQLException {
        final Connection connectionMock = Mockito.mock(Connection.class);
        when(connectionMock.prepareStatement(TYPE_DESCRIPTION_QUERY)).thenReturn(statementMock);
        return connectionMock;
    }

    private PreparedStatement mockTypeDescriptionStatement(final String columnType) throws SQLException {
        final ResultSet resultSetMock = Mockito.mock(ResultSet.class);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getString("COLUMN_TYPE")).thenReturn(columnType);
        final PreparedStatement statementMock = Mockito.mock(PreparedStatement.class);
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        return statementMock;
    }

    private ResultSet remoteColumn(final int jdbcType) throws SQLException {
        final ResultSet remoteColumnMock = Mockito.mock(ResultSet.class);
        when(remoteColumnMock.getInt("DATA_TYPE")).thenReturn(jdbcType);
        when(remoteColumnMock.getInt("DECIMAL_DIGITS")).thenReturn(0);
        when(remoteColumnMock.getInt("COLUMN_SIZE")).thenReturn(0);
        when(remoteColumnMock.getInt("CHAR_OCTET_LENGTH")).thenReturn(0);
        when(remoteColumnMock.getString("TYPE_NAME")).thenReturn("INTERVAL");
        when(remoteColumnMock.getString("TABLE_SCHEM")).thenReturn("SOURCE_SCHEMA");
        when(remoteColumnMock.getString("TABLE_NAME")).thenReturn("SOURCE_TABLE");
        when(remoteColumnMock.getString("COLUMN_NAME")).thenReturn("SOURCE_COLUMN");
        return remoteColumnMock;
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

    private static JdbcTypeBuilder timestampWithTimeZone(final int precision) {
        return jdbcType("TIMESTAMP WITH LOCAL TIME ZONE", precision).jdbcType(ExasolColumnMetadataReader.EXASOL_TIMESTAMP).precisionOrSize(precision);
    }

    private static JdbcTypeBuilder timestamp(final int precision) {
        return jdbcType("TIMESTAMP", precision).jdbcType(ExasolColumnMetadataReader.EXASOL_TIMESTAMP).precisionOrSize(precision);
    }

    private static JdbcTypeBuilder geometry(final int srid) {
        return jdbcType("GEOMETRY").jdbcType(ExasolColumnMetadataReader.EXASOL_GEOMETRY).precisionOrSize(srid);
    }

    private static JdbcTypeBuilder jdbcType(final String typeName) {
        return jdbcType(typeName, 0);
    }

    private static JdbcTypeBuilder jdbcType(final String typeName, final int decimalScale) {
        return new JdbcTypeBuilder().jdbcType(0).typeName(typeName).byteSize(0).decimalScale(decimalScale);
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
