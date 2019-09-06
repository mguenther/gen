package net.mguenther.gen;

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
        final Gen<Integer> gen = Gen.odd(Integer.MIN_VALUE, Integer.MAX_VALUE);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(Conditions.isOdd());
        }
    }

    @Test
    @DisplayName("odd should generate numbers greater or equal than given start argument")
    void oddShouldGenerateNumbersGreaterOrEqualThanGivenStartArgument() {
        final Gen<Integer> gen = Gen.odd(0, 2);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(Conditions.isGreaterThanOrEqual(0));
        }
    }

    @Test
    @DisplayName("odd should generate numbers lower than given stop argument")
    void oddShouldGenerateNumbersLowerThanGivenStopArgument() {
        final Gen<Integer> gen = Gen.odd(0, 2);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(Conditions.isLower(2));
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
