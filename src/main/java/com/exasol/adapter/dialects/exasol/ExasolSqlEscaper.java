package com.exasol.adapter.dialects.exasol;

final class ExasolSqlEscaper {
    private ExasolSqlEscaper() {
        // Not instantiable
    }

    static String escapeStringLiteralContent(final String value) {
        return value.replace("'", "''");
    }
}
