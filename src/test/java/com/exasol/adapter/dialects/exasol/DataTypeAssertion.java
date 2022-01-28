package com.exasol.adapter.dialects.exasol;

/**
 * This class contains data used for asserting the types of a column in a virtual schema.
 */
class DataTypeAssertion {

    private final String columnType;
    private final Object value;
    private final String expectedTypeOf;
    private final String expectedDescribeType;
    private final String expectedResultSetType;

    private DataTypeAssertion(final Builder builder) {
        this.columnType = builder.columnType;
        this.value = builder.value;
        this.expectedTypeOf = builder.expectedTypeOf;
        this.expectedDescribeType = builder.expectedDescribeType;
        this.expectedResultSetType = builder.expectedResultSetType;
    }

    static Builder builder(final AbstractExasolSqlDialectIT test) {
        return new Builder(test);
    }

    String getColumnType() {
        return this.columnType;
    }

    Object getValue() {
        return this.value;
    }

    String getExpectedTypeOf() {
        return this.expectedTypeOf;
    }

    String getExpectedDescribeType() {
        return this.expectedDescribeType;
    }

    String getExpectedResultSetType() {
        return this.expectedResultSetType;
    }

    static final class Builder {
        private String columnType;
        private Object value;
        private String expectedTypeOf;
        private String expectedDescribeType;
        private String expectedResultSetType;
        private final AbstractExasolSqlDialectIT test;

        private Builder(final AbstractExasolSqlDialectIT test) {
            this.test = test;
        }

        Builder withColumnType(final String columnType) {
            this.columnType = columnType;
            this.expectedDescribeType = columnType;
            this.expectedResultSetType = columnType;
            this.expectedTypeOf = columnType;
            return this;
        }

        Builder withValue(final Object value) {
            this.value = value;
            return this;
        }

        Builder expectTypeOf(final String expectedTypeOf) {
            this.expectedTypeOf = expectedTypeOf;
            return this;
        }

        Builder expectDescribeType(final String expectedDescribeType) {
            this.expectedDescribeType = expectedDescribeType;
            return this;
        }

        Builder expectResultSetType(final String expectedResultSetType) {
            this.expectedResultSetType = expectedResultSetType;
            return this;
        }

        void runAssert() {
            final DataTypeAssertion assertion = new DataTypeAssertion(this);
            this.test.assertVirtualSchemaTypes(assertion);
        }
    }
}
