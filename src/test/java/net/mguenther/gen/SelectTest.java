package net.mguenther.gen;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelectTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("select should use all generators from the given set of generators")
    void selectShouldUseAllGeneratorsFromTheWholeSetOfGivenGenerators() {
        final List<Gen<Integer>> listOfGen = Arrays.asList(Gen.even(0, 10), Gen.odd(0, 10));
        final Gen<Integer> randomGenFromListGen = Gen.select(listOfGen);
        assertThat(Stream.generate(randomGenFromListGen::sample)
                .limit(MAX_NUMBER_OF_PROBES)
                .anyMatch(n -> n % 2 == 0)).isTrue();
        assertThat(Stream.generate(randomGenFromListGen::sample)
                .limit(MAX_NUMBER_OF_PROBES)
                .anyMatch(n -> n % 2 == 1)).isTrue();
    }

    @Test
    @DisplayName("select should use generators evenly distributed")
    void selectShouldUseGeneratorsEvenlyDistributed() {
        final List<Gen<Integer>> listOfGen = Arrays.asList(Gen.even(0, 10), Gen.odd(0, 10));
        final Gen<Integer> randomGenFromListGen = Gen.select(listOfGen);
        final List<Integer> generatedNumbers = Stream.generate(randomGenFromListGen::sample)
                .limit(MAX_NUMBER_OF_PROBES)
                .collect(Collectors.toList());
        final int numberOfEvenNumbers = (int) generatedNumbers.stream().filter(n -> n % 2 == 0).count();
        final int numberOfOddNumbers = (int) generatedNumbers.stream().filter(n -> n % 2 == 1).count();
        assertThat(numberOfEvenNumbers).isCloseTo(MAX_NUMBER_OF_PROBES / 2, Percentage.withPercentage(15.0));
        assertThat(numberOfOddNumbers).isCloseTo(MAX_NUMBER_OF_PROBES / 2, Percentage.withPercentage(15.0));
    }

    @Test
    @DisplayName("select with explicit source of randomness should use generators evenly distributed")
    void selectWithExplicitSourceOfRandomnessShouldUseGeneratorsEvenlyDistributed() {
        final Gen<Integer> randomGenFromListGen = Gen.select(sourceOfRandomness -> Arrays.asList(
                Gen.even(0, 10, sourceOfRandomness),
                Gen.odd(0, 10, sourceOfRandomness)));
        final List<Integer> generatedNumbers = Stream.generate(randomGenFromListGen::sample)
                .limit(MAX_NUMBER_OF_PROBES)
                .collect(Collectors.toList());
        final int numberOfEvenNumbers = (int) generatedNumbers.stream().filter(n -> n % 2 == 0).count();
        final int numberOfOddNumbers = (int) generatedNumbers.stream().filter(n -> n % 2 == 1).count();
        assertThat(numberOfEvenNumbers).isCloseTo(MAX_NUMBER_OF_PROBES / 2, Percentage.withPercentage(15.0));
        assertThat(numberOfOddNumbers).isCloseTo(MAX_NUMBER_OF_PROBES / 2, Percentage.withPercentage(15.0));
    }

    @Test
    @DisplayName("select should throw an IllegalArgumentException if there are no generators to select from")
    void selectShouldThrowIllegalArgumentExceptionIfVarArgsIsEmpty() {
        assertThatThrownBy(() -> Gen.select(Collections.emptyList())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("two select generators with explicit source of randomness passed to the same generators should produce the same values in the same order")
    void twoSelectGeneratorsWithExplicitSourceOfRandomnessPassedToTheSameGeneratorsAndProduceTheSameValuesInTheSameOrder() {
        final Gen<Integer> randomGenFromListGenL = Gen.select(sourceOfRandomness -> Arrays.asList(
                Gen.even(0, 10, sourceOfRandomness),
                Gen.odd(0, 10, sourceOfRandomness)), new Random(1));
        final Gen<Integer> randomGenFromListGenR = Gen.select(sourceOfRandomness -> Arrays.asList(
                Gen.even(0, 10, sourceOfRandomness),
                Gen.odd(0, 10, sourceOfRandomness)), new Random(1));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(randomGenFromListGenL.sample()).isEqualTo(randomGenFromListGenR.sample());
        }
    }

    @Test
    @DisplayName("two select generators with explicit source of randomness should select generators in the same order")
    void twoSelectGeneratorsWithExplicitSourceOfRandomnessShouldSelectGeneratorsInTheSameOrder() {
        final Gen<Integer> randomGenFromListGenL = Gen.select(Arrays.asList(
                Gen.even(0, 10),
                Gen.odd(0, 10)),
                new Random(1));
        final Gen<Integer> randomGenFromListGenR = Gen.select(Arrays.asList(
                Gen.even(0, 10),
                Gen.odd(0, 10)),
                new Random(1));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final int l = randomGenFromListGenL.sample();
            final int r = randomGenFromListGenR.sample();
            assertThat(l % 2 == 0 && r % 2 == 0 || l % 2 == 1 && r % 2 == 1).isTrue();
        }
    }
}
