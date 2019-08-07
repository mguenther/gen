package net.mguenther.gen;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvenTest {

    private static final int MAX_NUMBER_OF_PROBES = 1_000;

    private static final int LOWER_BOUND_FOR_BOUNDARY_TEST = 0;

    private static final int UPPER_BOUND_FOR_BOUNDARY_TEST = 2;

    @Test
    @DisplayName("even should generate only even numbers")
    void evenShouldGenerateOnlyEvenNumbers() {
        final Condition<Integer> isEven = new Condition<>(n -> n % 2 == 0, "is even");
        final Gen<Integer> gen = Gen.even(Integer.MIN_VALUE, Integer.MAX_VALUE);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(isEven);
        }
    }

    @Test
    @DisplayName("even should generate numbers greater or equal than given start argument")
    void evenShouldGenerateNumbersGreaterOrEqualThanGivenStartArgument() {
        final Condition<Integer> isGreaterThanOrEqualLowerBound = new Condition<>(n -> n >= LOWER_BOUND_FOR_BOUNDARY_TEST, "greater or equal than lower bound");
        final Gen<Integer> gen = Gen.even(LOWER_BOUND_FOR_BOUNDARY_TEST, UPPER_BOUND_FOR_BOUNDARY_TEST);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(isGreaterThanOrEqualLowerBound);
        }
    }

    @Test
    @DisplayName("even should generate numbers lower than stop argument")
    void evenShouldGenerateNumbersLowerThanStopArgument() {
        final Condition<Integer> isLower = new Condition<>(n -> n < UPPER_BOUND_FOR_BOUNDARY_TEST, "lower than upper bound");
        final Gen<Integer> gen = Gen.even(LOWER_BOUND_FOR_BOUNDARY_TEST, UPPER_BOUND_FOR_BOUNDARY_TEST);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(isLower);
        }
    }

    @Test
    @DisplayName("even should throw IllegalArgumentException if lower and upper bound are the same")
    void evenShouldThrowIllegalArgumentExceptionIfLowerAndUpperBoundAreTheSame() {
        assertThrows(IllegalArgumentException.class, () -> Gen.even(1, 1));
        assertThrows(IllegalArgumentException.class, () -> Gen.even(1, 1, new Random()));
    }

    @Test
    @DisplayName("even should throw IllegalArgumentException if the lower bound is greater than the upper bound")
    void evenShouldThrowIllegalArgumentExceptionIfLowerBoundIsGreaterThanUpperBound() {
        assertThrows(IllegalArgumentException.class, () -> Gen.even(2, 1));
        assertThrows(IllegalArgumentException.class, () -> Gen.even(2, 1, new Random()));
    }

    @Test
    @DisplayName("two even generators using the same seed should generate the same numbers in the same order")
    void twoEvenGeneratorsUsingTheSameSeedShouldGenerateTheSameNumbersInTheSameOrder() {
        final Gen<Integer> genL = Gen.even(Integer.MIN_VALUE, Integer.MAX_VALUE, new Random(1L));
        final Gen<Integer> genR = Gen.even(Integer.MIN_VALUE, Integer.MAX_VALUE, new Random(1L));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(genL.sample()).isEqualTo(genR.sample());
        }
    }
}
