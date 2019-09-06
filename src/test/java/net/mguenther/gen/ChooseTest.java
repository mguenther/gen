package net.mguenther.gen;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class ChooseTest {

    private static final int LOWER_BOUND = -10;

    private static final int UPPER_BOUND = 10;

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("choose should only generate integers from the given interval")
    void chooseShouldOnlyGenerateIntegersFromGivenInterval() {
        final Gen<Integer> gen = Gen.choose(LOWER_BOUND, UPPER_BOUND);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).satisfies(Conditions.isWithinBoundary(LOWER_BOUND, UPPER_BOUND));
        }
    }

    @Test
    @DisplayName("choose should be able to generate all integers within the given interval")
    void chooseShouldBeAbleToGenerateAllIntegersWithinGivenInterval() {

        int[] generatedNumbers = new int[UPPER_BOUND-LOWER_BOUND];

        final Gen<Integer> gen = Gen.choose(LOWER_BOUND, UPPER_BOUND);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            int index = gen.sample() + 10;
            generatedNumbers[index] = generatedNumbers[index] + 1;
        }

        for (int generatedNumber : generatedNumbers) {
            assertThat(generatedNumber).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    @DisplayName("choose should exclude the value at the upper bound")
    void chooseShouldExcludeUpperBound() {
        final Gen<Integer> gen = Gen.choose(LOWER_BOUND, UPPER_BOUND);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(gen.sample()).isNotEqualTo(10);
        }
    }

    @Test
    @DisplayName("choose should generate integers evenly within the given interval (max. 15% deviation)")
    void chooseShouldDistributeGeneratedIntegersEvenlyWithinGivenInterval() {

        final int[] generatedNumbers = new int[UPPER_BOUND-LOWER_BOUND];
        final Gen<Integer> gen = Gen.choose(LOWER_BOUND, UPPER_BOUND);

        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            int index = gen.sample() + 10;
            generatedNumbers[index] = generatedNumbers[index] + 1;
        }

        for (int generatedNumber : generatedNumbers) {
            assertThat(generatedNumber).isCloseTo(MAX_NUMBER_OF_PROBES / generatedNumbers.length, Percentage.withPercentage(15.0));
        }
    }

    @Test
    @DisplayName("two choose generators using the same seed should generate the same integers in the same order")
    void twoChooseGeneratorsUsingTheSameSeedShouldGenerateTheSameIntegersInTheSameOrder() {
        final Gen<Integer> chooseL = Gen.choose(LOWER_BOUND, UPPER_BOUND, new Random(1L));
        final Gen<Integer> chooseR = Gen.choose(LOWER_BOUND, UPPER_BOUND, new Random(1L));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(chooseL.sample()).isEqualTo(chooseR.sample());
        }
    }
}
