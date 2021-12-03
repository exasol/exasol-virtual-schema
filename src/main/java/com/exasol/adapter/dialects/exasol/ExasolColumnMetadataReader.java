package com.exasol.adapter.dialects.exasol;

import java.sql.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    static final int EXASOL_INTERVAL_DAY_TO_SECONDS = -104;
    static final int EXASOL_INTERVAL_YEAR_TO_MONTHS = -103;
    static final int EXASOL_GEOMETRY = 123;
    static final int EXASOL_TIMESTAMP = 124;
    static final int EXASOL_HASHTYPE = 126;

    private static final int DEFAULT_SPACIAL_REFERENCE_SYSTEM_IDENTIFIER = 3857;
    private static final int DEFAULT_HASHTYPE_SIZE = 16;
    private static final int DEFAULT_INTERVAL_DAY_TO_SECOND_PRECISION = 2;
    private static final int DEFAULT_INTERVAL_DAY_TO_SECOND_FRACTION = 3;
    private static final int DEFAULT_INTERVAL_YEAR_TO_MONTH_PRECISION = 2;

    private static final String INTERVAL_DAY_TO_SECOND_PATTERN = "INTERVAL DAY\\((\\d+)\\) TO SECOND\\((\\d+)\\)";
    private static final String SRID_PATTERN = "\\((\\d+)\\)";

    /**
     * Create a new instance of the {@link ExasolColumnMetadataReader}.
     *
     * @param connection          JDBC connection through which the column metadata is read from the remote database
     * @param properties          user-defined adapter properties
     * @param identifierConverter converter between source and Exasol identifiers
     */
    public ExasolColumnMetadataReader(final Connection connection, final AdapterProperties properties,
            final IdentifierConverter identifierConverter) {
        super(connection, properties, identifierConverter);
    }

    @Override
    public DataType mapJdbcType(final JDBCTypeDescription type) {
        final DataType resultType = map(type);
        LOGGER.fine(() -> "Mapped JDBC type '" + type.getTypeName() + "' / " + type.getJdbcType() + " with byte size "
                + type.getByteSize() + ", decimal scale " + type.getDecimalScale() + ", precision/size "
                + type.getPrecisionOrSize() + " to " + resultType);
        return resultType;
    }

    private DataType map(final JDBCTypeDescription jdbcTypeDescription) {
        final String typeName = jdbcTypeDescription.getTypeName();
        final int jdbcType = jdbcTypeDescription.getJdbcType();
        if ("HASHTYPE".equals(typeName) || (jdbcType == EXASOL_HASHTYPE)) {
            if (jdbcTypeDescription.getByteSize() > 0) {
                return DataType.createHashtype(jdbcTypeDescription.getByteSize() / 2);
            } else {
                return DataType.createHashtype(DEFAULT_HASHTYPE_SIZE);
            }
        }
        if ("GEOMETRY".equals(typeName) || (jdbcType == EXASOL_GEOMETRY)) {
            return DataType.createGeometry(jdbcTypeDescription.getPrecisionOrSize());
        }

        if ("INTERVAL YEAR TO MONTH".equals(typeName) && (jdbcType == EXASOL_INTERVAL_YEAR_TO_MONTHS)) {
            if (jdbcTypeDescription.getPrecisionOrSize() == 0) {
                return DataType.createIntervalYearMonth(DEFAULT_INTERVAL_YEAR_TO_MONTH_PRECISION);
            } else {
                return DataType.createIntervalYearMonth(jdbcTypeDescription.getPrecisionOrSize());
            }
        }

        if ("INTERVAL YEAR TO MONTH".equals(typeName) && (jdbcType == Types.VARCHAR)) {
            // TODO: Precision/size is equal to string length -> Calculate precision from string length?
            return DataType.createIntervalYearMonth(DEFAULT_INTERVAL_YEAR_TO_MONTH_PRECISION);
        }

        if ("INTERVAL DAY TO SECOND".equals(typeName) && (jdbcType == EXASOL_INTERVAL_DAY_TO_SECONDS)) {
            if (jdbcTypeDescription.getDecimalScale() == 0) {
                return DataType.createIntervalDaySecond(DEFAULT_INTERVAL_DAY_TO_SECOND_PRECISION,
                        DEFAULT_INTERVAL_DAY_TO_SECOND_FRACTION);
            } else {
                return DataType.createIntervalDaySecond(jdbcTypeDescription.getPrecisionOrSize(),
                        jdbcTypeDescription.getDecimalScale());
            }
        }

        if ("INTERVAL DAY TO SECOND".equals(typeName) && (jdbcType == Types.VARCHAR)) {
            if (jdbcTypeDescription.getDecimalScale() == 0) {
                return DataType.createIntervalDaySecond(DEFAULT_INTERVAL_DAY_TO_SECOND_PRECISION,
                        DEFAULT_INTERVAL_DAY_TO_SECOND_FRACTION);
            } else {
                return DataType.createIntervalDaySecond(jdbcTypeDescription.getPrecisionOrSize(),
                        jdbcTypeDescription.getDecimalScale());
            }
        }

        if ("TIMESTAMP WITH LOCAL TIME ZONE".equals(typeName)) {
            return DataType.createTimestamp(true);
        }

        if ("TIMESTAMP".equals(typeName) || (jdbcType == EXASOL_TIMESTAMP)) {
            return DataType.createTimestamp(false);
        }

        return super.mapJdbcType(jdbcTypeDescription);
    }

    @Override
    public JDBCTypeDescription readJdbcTypeDescription(final ResultSet remoteColumn) throws SQLException {
        final JDBCTypeDescription typeDescription = super.readJdbcTypeDescription(remoteColumn);
        switch (typeDescription.getJdbcType()) {
        case EXASOL_INTERVAL_DAY_TO_SECONDS:
            return extractIntervalDayToSecondPrecision(remoteColumn, typeDescription);
        case EXASOL_INTERVAL_YEAR_TO_MONTHS:
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
     * Extract the spacial reference system identifier (SRID) from a type description.
     *
     * @param typeDescriptionString a type description like {@code GEOMETRY(1234)} or {@code GEOMETRY}.
     * @return the SRID from the type description or the default value
     *         {@link #DEFAULT_SPACIAL_REFERENCE_SYSTEM_IDENTIFIER}
     */
    protected int extractSrid(final String typeDescriptionString) {
        final Pattern pattern = Pattern.compile(SRID_PATTERN);
        final Matcher matcher = pattern.matcher(typeDescriptionString);
        if (matcher.find()) {
            final String srid = matcher.group(1);
            return Integer.parseInt(srid);
        } else {
            return DEFAULT_SPACIAL_REFERENCE_SYSTEM_IDENTIFIER;
        }
    }

    private String getTypeDescriptionStringForColumn(final ResultSet remoteColumn) throws SQLException {
        try (final PreparedStatement preparedStatement = this.connection.prepareStatement(
                "SELECT COLUMN_TYPE FROM SYS.EXA_ALL_COLUMNS WHERE COLUMN_SCHEMA = ? AND COLUMN_TABLE = ? AND COLUMN_NAME = ?;")) {
            final String schema = remoteColumn.getString("TABLE_SCHEM");
            final String table = remoteColumn.getString("TABLE_NAME");
            final String column = remoteColumn.getString("COLUMN_NAME");
            preparedStatement.setString(1, schema);
            preparedStatement.setString(2, table);
            preparedStatement.setString(3, column);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getString("COLUMN_TYPE");
            }
        }
    }

    private JDBCTypeDescription extractIntervalDayToSecondPrecision(final ResultSet remoteColumn,
            final JDBCTypeDescription typeDescription) throws SQLException {
        final String typeDescriptionString = getTypeDescriptionStringForColumn(remoteColumn);
        final Pattern pattern = Pattern.compile(INTERVAL_DAY_TO_SECOND_PATTERN);
        final Matcher matcher = pattern.matcher(typeDescriptionString);
        if (matcher.matches()) {
            return new JDBCTypeDescription(typeDescription.getJdbcType(), Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(1)), typeDescription.getByteSize(), typeDescription.getTypeName());
        } else {
            throw new IllegalStateException(ExaError.messageBuilder("E-VS-EXA-2") //
                    .message("Failed to extract INTERVAL DAY TO SECOND precision").toString());
        }
    }

    private JDBCTypeDescription extractIntervalYearToMonthPrecision(final ResultSet remoteColumn,
            final JDBCTypeDescription typeDescription) throws SQLException {
        final String typeDescriptionString = getTypeDescriptionStringForColumn(remoteColumn);
        final Pattern pattern = Pattern.compile("INTERVAL YEAR\\((\\d+)\\) TO MONTH");
        final Matcher matcher = pattern.matcher(typeDescriptionString);
        if (matcher.matches()) {
            return new JDBCTypeDescription(typeDescription.getJdbcType(), typeDescription.getDecimalScale(),
                    Integer.parseInt(matcher.group(1)), typeDescription.getByteSize(), typeDescription.getTypeName());
        } else {
            throw new IllegalStateException(ExaError.messageBuilder("E-VS-EXA-3") //
                    .message("Failed to extract INTERVAL YEAR TO MONTH precision").toString());
        }
    }
}