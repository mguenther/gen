package net.mguenther.gen;

import org.assertj.core.api.Condition;

public class Conditions {

    public static Condition<Integer> isWithinBoundary(final int lower, final int upperExclusive) {
        return new Condition<>(n -> n >= lower && n < upperExclusive, "is within boundary");
    }
}
