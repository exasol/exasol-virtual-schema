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
    public static final Logger LOGGER = Logger.getLogger(ExasolColumnMetadataReader.class.getName());

    static final int EXASOL_INTERVAL_DAY_TO_SECONDS = -104;
    static final int EXASOL_INTERVAL_YEAR_TO_MONTHS = -103;
    static final int EXASOL_GEOMETRY = 123;
    static final int EXASOL_TIMESTAMP = 124;
    static final int EXASOL_HASHTYPE = 126;
    private static final int DEFAULT_SPACIAL_REFERENCE_SYSTEM_IDENTIFIER = 3857;
    public static final String INTERVAL_DAY_TO_SECOND_PATTERN = "INTERVAL DAY\\((\\d+)\\) TO SECOND\\((\\d+)\\)";

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
    public DataType mapJdbcType(final JDBCTypeDescription jdbcTypeDescription) {
        switch (jdbcTypeDescription.getJdbcType()) {
        case EXASOL_INTERVAL_DAY_TO_SECONDS:
            return DataType.createIntervalDaySecond(jdbcTypeDescription.getPrecisionOrSize(),
                    jdbcTypeDescription.getDecimalScale());
        case EXASOL_INTERVAL_YEAR_TO_MONTHS:
            return DataType.createIntervalYearMonth(jdbcTypeDescription.getPrecisionOrSize());
        case EXASOL_GEOMETRY:
            return DataType.createGeometry(jdbcTypeDescription.getPrecisionOrSize());
        case EXASOL_TIMESTAMP:
            return DataType.createTimestamp(true);
        case EXASOL_HASHTYPE:
            return DataType.createHashtype(jdbcTypeDescription.getByteSize());
        default:
            return super.mapJdbcType(jdbcTypeDescription);
        }
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

    private int extractSrid(final String typeDescriptionString) {
        final int start = typeDescriptionString.indexOf("(");
        if (start > -1) {
            final int end = typeDescriptionString.indexOf(")");
            return Integer.parseInt(typeDescriptionString.substring(start + 1, end));
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