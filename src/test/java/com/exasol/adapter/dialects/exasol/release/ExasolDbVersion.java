package com.exasol.adapter.dialects.exasol.release;

import java.util.Arrays;

public class ExasolDbVersion implements Comparable<ExasolDbVersion> {

    public static ExasolDbVersion of(final int... numbers) {
        return new ExasolDbVersion(numbers);
    }

    public static ExasolDbVersion parse(final String string) {
        final int[] numbers = Arrays.stream(string.split("\\.")).mapToInt(Integer::parseInt).toArray();
        return new ExasolDbVersion(numbers);
    }

    private static final int LESS = -1;
    private static final int EQUAL = 0;
    private static final int GREATER = 1;

    private final int[] numbers;

    ExasolDbVersion(final int... numbers) {
        this.numbers = numbers;
    }

    @Override
    public int compareTo(final ExasolDbVersion other) {
        for (int i = 0; i < this.numbers.length; i++) {
            final int result = compare(i, other);
            if (differs(result)) {
                return result;
            }
        }
        if (this.numbers.length < other.numbers.length) {
            return LESS;
        }
        return EQUAL;
    }

    public boolean isGreaterOrEqualThan(final ExasolDbVersion other) {
        return compareTo(other) > LESS;
    }

    private int compare(final int i, final ExasolDbVersion other) {
        if (i >= other.numbers.length) {
            return GREATER;
        }
        return Integer.compare(this.numbers[i], other.numbers[i]);
    }

    private boolean differs(final int result) {
        return result != EQUAL;
    }
}
