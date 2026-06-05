package com.exasol.adapter.dialects.exasol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class ExasolSqlEscaperTest {
    @Test
    void escapeStringLiteralContentEscapesSingleQuotes() {
        assertThat(ExasolSqlEscaper.escapeStringLiteralContent("string ' with '' quotes \"..."),
                equalTo("string '' with '''' quotes \"..."));
    }
}
