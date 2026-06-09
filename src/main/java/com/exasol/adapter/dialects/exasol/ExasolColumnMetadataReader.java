package com.exasol.adapter.dialects.exasol;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.IdentifierConverter;
import com.exasol.adapter.jdbc.BaseColumnMetadataReader;
import com.exasol.adapter.jdbc.JDBCTypeDescription;
import com.exasol.adapter.metadata.DataType;
import com.exasol.errorreporting.ExaError;

/**
 * This class implements Exasol-specific reading of column metadata.
 */
public class ExasolColumnMetadataReader extends BaseColumnMetadataReader {
    private static final Logger LOGGER = Logger.getLogger(ExasolColumnMetadataReader.class.getName());
    static final int EXASOL_INTERVAL_DAY_TO_SECOND = -104;
    static final int EXASOL_INTERVAL_YEAR_TO_MONTH = -103;
    static final int EXASOL_GEOMETRY = 123;
    static final int EXASOL_TIMESTAMP = 124;
    static final int EXASOL_HASHTYPE = 126;
    private static final int DEFAULT_SPATIAL_REFERENCE_SYSTEM_IDENTIFIER = 0;

    private static final String DIGITS_IN_PARENTHESES = "\\((\\d+)\\)";
    private static final Pattern INTERVAL_DAY_TO_SECOND_PATTERN = Pattern.compile( //
            "INTERVAL DAY" + DIGITS_IN_PARENTHESES + " TO SECOND" + DIGITS_IN_PARENTHESES);
    private static final Pattern INTERVAL_YEAR_TO_MONTH_PATTERN = Pattern.compile( //
            "INTERVAL YEAR" + DIGITS_IN_PARENTHESES + " TO MONTH");
    private static final Pattern SRID_PATTERN = Pattern.compile(DIGITS_IN_PARENTHESES);

    /**
     * Create a new instance of the {@link ExasolColumnMetadataReader}.
     *
     * @param connection          JDBC connection through which the column metadata is read from the remote database
     * @param properties          user-defined adapter properties
     * @param identifierConverter converter between source and Exasol identifiers
     * @param exaMetadata         Metadata of the Exasol database
     */
    public ExasolColumnMetadataReader(final Connection connection, final AdapterProperties properties,
            final ExaMetadata exaMetadata, final IdentifierConverter identifierConverter) {
        super(connection, properties, exaMetadata, identifierConverter);
    }

    @Override
    public DataType mapJdbcType(final JDBCTypeDescription jdbcTypeDescription) {
        final DataType resultType = getDataTypeBasedOnJdbcType(jdbcTypeDescription) //
                .or(() -> getDataTypeBasedOnTypeName(jdbcTypeDescription)) //
                .orElseGet(() -> super.mapJdbcType(jdbcTypeDescription));
        LOGGER.fine(() -> "Mapped JDBC type " + jdbcTypeDescription.getTypeName() + " ("
                + jdbcTypeDescription.getJdbcType() + ") with byte size " + jdbcTypeDescription.getByteSize()
                + ", decimal scale " + jdbcTypeDescription.getDecimalScale() + ", precision/size "
                + jdbcTypeDescription.getPrecisionOrSize() + " to " + resultType);
        return resultType;
    }

    private Optional<DataType> getDataTypeBasedOnJdbcType(final JDBCTypeDescription jdbcTypeDescription) {
        switch (jdbcTypeDescription.getJdbcType()) {
            case EXASOL_INTERVAL_DAY_TO_SECOND:
                return Optional.of(DataType.createIntervalDaySecond(jdbcTypeDescription.getPrecisionOrSize(),
                        jdbcTypeDescription.getDecimalScale()));
            case EXASOL_INTERVAL_YEAR_TO_MONTH:
                return Optional.of(DataType.createIntervalYearMonth(jdbcTypeDescription.getPrecisionOrSize()));
            case EXASOL_GEOMETRY:
                return Optional.of(DataType.createGeometry(jdbcTypeDescription.getPrecisionOrSize()));
            case EXASOL_TIMESTAMP:
                return Optional.of(convertExasolTimestamp(jdbcTypeDescription.getDecimalScale()));
            case EXASOL_HASHTYPE:
                return Optional.of(DataType.createHashtype(jdbcTypeDescription.getByteSize()));
            default:
                return Optional.empty();
        }
    }

    /**
     * Converts a given decimal scale into an Exasol {@link DataType} representing a TIMESTAMP.
     * <p>
     * If the system supports timestamps with nanosecond precision, the fractional precision
     * of the timestamp will be set to the provided decimal scale as long as it is in the range
     * 0..9. Decimal scales {@code < 0} fall back to precision 0 and values above 9 are capped at 9.
     * Otherwise, a default fractional precision of 3 (milliseconds) is used.
     *
     * @param decimalScale the number of fractional digits to use for the TIMESTAMP precision
     * @return a {@link DataType} representing a TIMESTAMP with the appropriate fractional precision
     */
    private DataType convertExasolTimestamp(final int decimalScale) {
        if (supportsTimestampsWithNanoPrecision()) {
            final int fractionalPrecision = getTimestampFractionalPrecision(decimalScale);
            return DataType.createTimestamp(true, fractionalPrecision);
        }
        return DataType.createTimestamp(true, 3);
    }

    private static int getTimestampFractionalPrecision(final int decimalScale) {
        return decimalScale < 0 ? 0 : Math.min(decimalScale, 9);
    }

    private Optional<DataType> getDataTypeBasedOnTypeName(final JDBCTypeDescription jdbcTypeDescription) {
        if ("HASHTYPE".equals(jdbcTypeDescription.getTypeName())) {
            if (jdbcTypeDescription.getByteSize() != 0) {
                return Optional.of(DataType.createHashtype(jdbcTypeDescription.getByteSize() / 2));
            } else if (jdbcTypeDescription.getPrecisionOrSize() != 0) {
                return Optional.of(DataType.createHashtype(jdbcTypeDescription.getPrecisionOrSize() / 2));
            }
        }
        return Optional.empty();
    }

    @Override
    public JDBCTypeDescription readJdbcTypeDescription(final ResultSet remoteColumn) throws SQLException {
        final JDBCTypeDescription typeDescription = super.readJdbcTypeDescription(remoteColumn);
        switch (typeDescription.getJdbcType()) {
            case EXASOL_INTERVAL_DAY_TO_SECOND:
                return extractIntervalDayToSecondPrecision(remoteColumn, typeDescription);
            case EXASOL_INTERVAL_YEAR_TO_MONTH:
                return extractIntervalYearToMonthPrecision(remoteColumn, typeDescription);
            case EXASOL_GEOMETRY:
                return getGeometryWithExtractedSrid(remoteColumn, typeDescription);
            default:
                return typeDescription;
        }
    }

    private JDBCTypeDescription getGeometryWithExtractedSrid(final ResultSet remoteColumn,
            final JDBCTypeDescription typeDescription) throws SQLException {
        final String typeDescriptionString = getTypeDescriptionStringForColumn(remoteColumn);
        final int srid = extractSrid(typeDescriptionString);
        return new JDBCTypeDescription(typeDescription.getJdbcType(), typeDescription.getDecimalScale(), srid,
                typeDescription.getByteSize(), typeDescription.getTypeName());
    }

    /**
     * Extract the spatial reference system identifier (SRID) from a type description.
     *
     * @param typeDescriptionString a type description like {@code GEOMETRY(1234)} or {@code GEOMETRY}.
     * @return the SRID from the type description or the default value
     *         {@link #DEFAULT_SPATIAL_REFERENCE_SYSTEM_IDENTIFIER}
     */
    protected int extractSrid(final String typeDescriptionString) {
        if (typeDescriptionString == null) {
            return fallbackSrid("null");
        }
        final Matcher matcher = SRID_PATTERN.matcher(typeDescriptionString);
        if (!matcher.find()) {
            return fallbackSrid(typeDescriptionString);
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (final NumberFormatException ignored) {
            return fallbackSrid(typeDescriptionString);
        }
    }

    private int fallbackSrid(final String typeDescription) {
        LOGGER.warning(() -> String.format("Could not extract SRID from type description \"%s\". Falling back to SRID %d.",
                typeDescription, DEFAULT_SPATIAL_REFERENCE_SYSTEM_IDENTIFIER));
        return DEFAULT_SPATIAL_REFERENCE_SYSTEM_IDENTIFIER;
    }

    private String getTypeDescriptionStringForColumn(final ResultSet remoteColumn) throws SQLException {
        try (final PreparedStatement preparedStatement = this.connection.prepareStatement(
                "SELECT COLUMN_TYPE FROM SYS.EXA_ALL_COLUMNS WHERE COLUMN_SCHEMA = ? AND COLUMN_TABLE = ? AND COLUMN_NAME = ?")) {
            final String schema = remoteColumn.getString("TABLE_SCHEM");
            final String table = remoteColumn.getString("TABLE_NAME");
            final String column = remoteColumn.getString("COLUMN_NAME");
            preparedStatement.setString(1, schema);
            preparedStatement.setString(2, table);
            preparedStatement.setString(3, column);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException(ExaError.messageBuilder("E-VSEXA-7")
                            .message(
                                    "Could not find a matching row in SYS.EXA_ALL_COLUMNS for column {{columnName|uq}} in schema {{schemaName|uq}} and table {{tableName|uq}}.",
                                    column, schema, table)
                            .mitigation("Ensure that the user has the necessary privileges.")
                            .toString());
                }
                return resultSet.getString("COLUMN_TYPE");
            }
        }
    }

    private JDBCTypeDescription extractIntervalDayToSecondPrecision(final ResultSet remoteColumn,
            final JDBCTypeDescription typeDescription) throws SQLException {
        final String typeDescriptionString = getTypeDescriptionStringForColumn(remoteColumn);
        final Matcher matcher = INTERVAL_DAY_TO_SECOND_PATTERN.matcher(typeDescriptionString);
        if (matcher.matches()) {
            return new JDBCTypeDescription(typeDescription.getJdbcType(), Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(1)), typeDescription.getByteSize(), typeDescription.getTypeName());
        } else {
            throw new IllegalStateException(ExaError.messageBuilder("E-VSEXA-2") //
                    .message("Failed to extract INTERVAL DAY TO SECOND precision").toString());
        }
    }

    private JDBCTypeDescription extractIntervalYearToMonthPrecision(final ResultSet remoteColumn,
            final JDBCTypeDescription typeDescription) throws SQLException {
        final String typeDescriptionString = getTypeDescriptionStringForColumn(remoteColumn);
        final Matcher matcher = INTERVAL_YEAR_TO_MONTH_PATTERN.matcher(typeDescriptionString);
        if (matcher.matches()) {
            return new JDBCTypeDescription(typeDescription.getJdbcType(), typeDescription.getDecimalScale(),
                    Integer.parseInt(matcher.group(1)), typeDescription.getByteSize(), typeDescription.getTypeName());
        } else {
            throw new IllegalStateException(ExaError.messageBuilder("E-VSEXA-3") //
                    .message("Failed to extract INTERVAL YEAR TO MONTH precision").toString());
        }
    }

}
