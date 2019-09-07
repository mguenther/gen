package net.mguenther.gen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizedDoubleTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("normalizedDouble should generate double values within interval [0.0; 1.0)")
    void normalizedDoubleShouldGenerateDoubleValuesFromZeroToOneExclusive() {
        final Gen<Double> normalizedDoubleGen = Gen.normalizedDouble();
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(normalizedDoubleGen.sample()).satisfies(Conditions.isWithinBoundary(0.0, 1.0));
        }
    }

    @Test
    @DisplayName("two normalizedDouble generators using the same seed should generate the same double values in the same order")
    void twoNormalizedDoubleGeneratorsUsingTheSameSeedShouldGenerateTheSameDoubleValuesInTheSameOrder() {
        final Gen<Double> normalizedDoubleGenL = Gen.normalizedDouble(new Random(1));
        final Gen<Double> normalizedDoubleGenR = Gen.normalizedDouble(new Random(1));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(normalizedDoubleGenL.sample()).isEqualTo(normalizedDoubleGenR.sample());
        }
    }
}
