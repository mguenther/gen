package net.mguenther.gen;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OddTest {

    private static final int MAX_NUMBER_OF_PROBES = 1_000;

    @Test
    @DisplayName("odd should generate only odd numbers")
    void oddShouldGenerateOnlyOddNumbers() {
        final Condition<Integer> isOdd = new Condition<>(n -> n % 2 != 0, "is odd");
        final Gen<Integer> gen = Gen.odd(Integer.MIN_VALUE, Integer.MAX_VALUE);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(isOdd);
        }
    }

    @Test
    @DisplayName("odd should generate numbers greater or equal than given start argument")
    void oddShouldGenerateNumbersGreaterOrEqualThanGivenStartArgument() {
        final Condition<Integer> isGreaterThanOrEqualLowerBound = new Condition<>(n -> n >= 0, "is greater or equal than lower bound");
        final Gen<Integer> gen = Gen.odd(0, 2);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(isGreaterThanOrEqualLowerBound);
        }
    }

    @Test
    @DisplayName("odd should generate numbers lower than given stop argument")
    void oddShouldGenerateNumbersLowerThanGivenStopArgument() {
        final Condition<Integer> isLower = new Condition<>(n -> n < 2, "is lower");
        final Gen<Integer> gen = Gen.odd(0, 2);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(isLower);
        }
    }

    @Test
    @DisplayName("odd should throw IllegalArgumentException if lower and upper bound are the same")
    void oddShouldThrowIllegalArgumentExceptionIfLowerAndUpperBoundAreTheSame() {
        assertThrows(IllegalArgumentException.class, () -> Gen.odd(1, 1));
        assertThrows(IllegalArgumentException.class, () -> Gen.odd(1, 1, new Random()));
    }

    @Test
    @DisplayName("odd should throw IllegalArgumentException if lower bound is greater than the upper bound")
    void oddShouldThrowIllegalArgumentExceptionIfLowerBoundIsGreaterThanTheUpperBound() {
        assertThrows(IllegalArgumentException.class, () -> Gen.odd(2, 1));
        assertThrows(IllegalArgumentException.class, () -> Gen.odd(2, 1, new Random()));
    }

    @Test
    @DisplayName("two odd generators using the same seed should generate the same numbers in the same order")
    void twoOddGeneratorsUsingTheSameSeedShouldGenerateTheSameNumbersInTheSameOrder() {
        final Gen<Integer> genL = Gen.odd(Integer.MIN_VALUE, Integer.MAX_VALUE, new Random(1L));
        final Gen<Integer> genR = Gen.odd(Integer.MIN_VALUE, Integer.MAX_VALUE, new Random(1L));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(genL.sample()).isEqualTo(genR.sample());
        }
    }
}
