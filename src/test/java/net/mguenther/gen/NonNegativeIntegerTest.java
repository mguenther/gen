package net.mguenther.gen;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class NonNegativeIntegerTest {

    private static final int MAX_NUMBER_OF_PROBES = 100_000;

    @Test
    @DisplayName("nonNegativeInteger should generate only integers that are larger than or equal to zero")
    void nonNegativeIntegerShouldGenerateOnlyIntegersLargerThanOrEqualToZero() {

        final Condition<Integer> isNotNegative = Conditions.isGreaterThanOrEqual(0);
        final Gen<Integer> nonNegativeIntegerGen = Gen.nonNegativeInteger();

        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(nonNegativeIntegerGen.sample()).satisfies(isNotNegative);
        }
    }

    @Test
    @DisplayName("two nonNegativeInteger generators using the same seed should generate the same integers in the same order")
    void twoNonNegativeIntegerGeneratorsUsingTheSameSeedShouldGenerateTheSameIntegersInTheSameOrder() {
        final Gen<Integer> nonNegativeIntegerL = Gen.nonNegativeInteger(new Random(1L));
        final Gen<Integer> nonNegativeIntegerR = Gen.nonNegativeInteger(new Random(1L));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(nonNegativeIntegerL.sample()).isEqualTo(nonNegativeIntegerR.sample());
        }
    }
}
