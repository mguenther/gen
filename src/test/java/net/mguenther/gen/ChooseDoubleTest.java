package net.mguenther.gen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class ChooseDoubleTest {

    private static final double LOWER_BOUND = -10.0;

    private static final double UPPER_BOUND = 10.0;

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("choose should only generate double values from the given interval")
    void chooseShouldOnlyGenerateDoubleValuesFromGivenInterval() {
        final Gen<Double> gen = Gen.choose(LOWER_BOUND, UPPER_BOUND);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(Conditions.isWithinBoundary(LOWER_BOUND, UPPER_BOUND));
        }
    }

    @Test
    @DisplayName("choose should exclude the value at the upper bound")
    void chooseShouldExcludeUpperBound() {
        final Gen<Double> gen = Gen.choose(LOWER_BOUND, UPPER_BOUND);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).isNotEqualTo(10.0);
        }
    }

    @Test
    @DisplayName("two choose generators using the same seed should generate the same integers in the same order")
    void twoChooseGeneratorsUsingTheSameSeedShouldGenerateTheSameIntegersInTheSameOrder() {
        final Gen<Double> chooseL = Gen.choose(LOWER_BOUND, UPPER_BOUND, new Random(1L));
        final Gen<Double> chooseR = Gen.choose(LOWER_BOUND, UPPER_BOUND, new Random(1L));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(chooseL.sample()).isEqualTo(chooseR.sample());
        }
    }
}
