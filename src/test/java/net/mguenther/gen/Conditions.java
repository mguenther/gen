package net.mguenther.gen;

import org.assertj.core.api.Condition;

class Conditions {

    static Condition<Integer> isWithinBoundary(final int lower, final int upperExclusive) {
        return new Condition<>(n -> n >= lower && n < upperExclusive, "is within boundary");
    }

    static Condition<Double> isWithinBoundary(final double lower, final double upperExclusive) {
        return new Condition<>(n -> n >= lower && n < upperExclusive, "is within boundary");
    }

    static Condition<Integer> isLower(final int boundary) {
        return new Condition<>(n -> n < boundary, "lower than upper bound");
    }

    static Condition<Integer> isEven() {
        return new Condition<>(n -> n % 2 == 0, "is even");
    }

    static Condition<Integer> isOdd() {
        return new Condition<>(n -> n % 2 != 0, "is odd");
    }

    static Condition<Integer> isGreaterThanOrEqual(final int boundary) {
        return new Condition<>(n -> n >= boundary, "greater or equal than lower bound");
    }
}
