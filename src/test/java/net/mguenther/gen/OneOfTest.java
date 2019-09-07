package net.mguenther.gen;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OneOfTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("oneOf should generate values from the whole set")
    void oneOfShouldGenerateValuesFromTheWholeSet() {
        final boolean[] generated = new boolean[10];
        final List<Integer> setOfValues = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        final Gen<Integer> oneOfGen = Gen.oneOf(setOfValues);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            generated[oneOfGen.sample()] = true;
        }
        for (boolean hasBeenGenerated : generated) {
            assertThat(hasBeenGenerated).isTrue();
        }
    }

    @Test
    @DisplayName("oneOf should generate values evenly distributed")
    void oneOfShouldGenerateValuesEvenlyDistributed() {
        final int[] frequencyOfValues = new int[10];
        final List<Integer> setOfValues = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        final Gen<Integer> oneOfGen = Gen.oneOf(setOfValues);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final int index = oneOfGen.sample();
            frequencyOfValues[index] = frequencyOfValues[index] + 1;
        }
        for (int frequency : frequencyOfValues) {
            assertThat(frequency).isCloseTo(MAX_NUMBER_OF_PROBES / 10, Percentage.withPercentage(15.0));
        }
    }

    @Test
    @DisplayName("two oneOf generators using the same seed should generate the same values in the same order")
    void twoOneOfGeneratorsUsingTheSameSeedShouldGenerateTheSameValuesInTheSameOrder() {
        final List<Integer> setOfValues = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        final Gen<Integer> oneOfGenL = Gen.oneOf(setOfValues, new Random(1));
        final Gen<Integer> oneOfGenR = Gen.oneOf(setOfValues, new Random(1));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(oneOfGenL.sample()).isEqualTo(oneOfGenR.sample());
        }
    }

    @Test
    @DisplayName("oneOf should throw an IllegalArgumentException if there are no values to produce from")
    void oneOfShouldThrowIllegalArgumentExceptionIfVarArgsIsEmpty() {
        assertThatThrownBy(Gen::oneOf).isInstanceOf(IllegalArgumentException.class);
    }
}
