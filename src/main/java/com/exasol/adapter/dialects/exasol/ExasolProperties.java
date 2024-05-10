package com.exasol.adapter.dialects.exasol;

/**
 * This class contains Exasol-specific property constants.
 */
final class ExasolProperties {
    static final String EXASOL_IMPORT_PROPERTY = "IMPORT_FROM_EXA";
    static final String EXASOL_CONNECTION_PROPERTY = "EXA_CONNECTION";
    static final String EXASOL_IS_LOCAL_PROPERTY = "IS_LOCAL";
    static final String GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA = "GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA";

    private ExasolProperties() {
        // prevent instantiation
    }
}
