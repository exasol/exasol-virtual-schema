package com.exasol.adapter.dialects.exasol;

import static com.exasol.adapter.dialects.exasol.ExasolSqlDialect.EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH;

import java.util.logging.Logger;

import com.exasol.adapter.dialects.SqlDialect;
import com.exasol.adapter.dialects.rewriting.SqlGenerationContext;
import com.exasol.adapter.dialects.rewriting.SqlGenerationVisitor;
import com.exasol.adapter.sql.SqlLiteralTimestampUtc;

/**
 * This class generates SQL queries for the {@link ExasolSqlDialect}.
 */
public class ExasolSqlGenerationVisitor extends SqlGenerationVisitor {
    private static final Logger LOGGER = Logger.getLogger(ExasolSqlGenerationVisitor.class.getName());

    /**
     * Creates a new instance of the {@link ExasolSqlGenerationVisitor}.
     *
     * @param dialect {@link ExasolSqlDialect} dialect
     * @param context SQL generation context
     */
    ExasolSqlGenerationVisitor(final SqlDialect dialect, final SqlGenerationContext context) {
        super(dialect, context);
    }

    @Override
    public String visit(final SqlLiteralTimestampUtc literal) {
        final ExasolSqlDialect exasolSqlDialect = (ExasolSqlDialect) getDialect();
        if (exasolSqlDialect.isTimestampWithLocalTimeZoneEnabled()) {
            LOGGER.info("IGNORE_ERRORS = '" + EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH + "' property is enabled.");
            return super.visit(literal);
        } else {
            throw new UnsupportedOperationException(
                    "Attention! Using literals and constant expressions with datatype `TIMESTAMP WITH LOCAL TIME ZONE` "
                            + "in Virtual Schemas can produce an incorrect results. We recommend using 'TIMESTAMP' instead. "
                            + "If you are willing to take the risk and want to use `TIMESTAMP WITH LOCAL TIME ZONE` anyway, please, "
                            + "create a Virtual Schema with the following additional property " + "IGNORE_ERRORS = '"
                            + EXASOL_TIMESTAMP_WITH_LOCAL_TIME_ZONE_SWITCH + "'. "
                            + "We also recommend to set Exasol system `time_zone` "
                            + "to UTC while working with `TIMESTAMP WITH LOCAL TIME ZONE`.");
        }
    }
}