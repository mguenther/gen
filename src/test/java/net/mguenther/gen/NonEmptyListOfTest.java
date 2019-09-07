package net.mguenther.gen;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NonEmptyListOfTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("nonEmptyListOf should never produce an empty list")
    void nonEmptyListOfShouldNeverProduceAnEmptyList() {
        final Gen<List<Integer>> nonEmptyListOfGen = Gen.nonEmptyListOf(Gen.choose(1, 1000), 10);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(nonEmptyListOfGen.sample()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("nonEmptyListOf should throw an IllegalArgumentException if maxLength is equal to zero")
    void nonEmptyListOfShouldThrowAnIllegalArgumentExceptionIfMaxLengthIsEqualToZero() {
        assertThatThrownBy(() -> Gen.nonEmptyListOf(Gen.choose(1, 1000),  0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("nonEmptyListOf should throw an IllegalArgumentException if maxLength is less than zero")
    void nonEmptyListOfShouldThrowAnIllegalArgumentExceptionIfMaxLengthIsLessThanZero() {
        assertThatThrownBy(() -> Gen.nonEmptyListOf(Gen.choose(1, 1000),  -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("nonEmptyListOf should generate lists that do not exceed the given maxLength")
    void nonEmptyListOfShouldGenerateListsOfValuesThatDoNotExceedTheGivenMaxLength() {
        final Gen<List<Integer>> nonEmptyListOfGen = Gen.nonEmptyListOf(Gen.choose(1, 1000), 10);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(nonEmptyListOfGen.sample().size()).isLessThanOrEqualTo(10);
        }
    }

    @Test
    @DisplayName("nonEmptyListOf should be able to generate lists with the maximum length")
    void nonEmptyListOfShouldBeAbleToGenerateListsWithMaximumSize() {
        final Gen<List<Integer>> nonEmptyListOfGen = Gen.nonEmptyListOf(Gen.choose(1, 1000), 10);
        boolean foundListWithMaximumSize = false;
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final List<Integer> listOfNumbers = nonEmptyListOfGen.sample();
            if (listOfNumbers.size() == 10) {
                foundListWithMaximumSize = true;
                break;
            }
        }
        assertThat(foundListWithMaximumSize).isTrue();
    }

    @Test
    @DisplayName("nonEmptyListOf should be able to generate lists with minimum size of 1")
    void nonEmptyListOfShouldBeAbleToGenerateListsWithMinimumSize() {
        final Gen<List<Integer>> nonEmptyListOfGen = Gen.nonEmptyListOf(Gen.choose(1, 1000), 10);
        boolean foundListWithMinimumSize = false;
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final List<Integer> listOfNumbers = nonEmptyListOfGen.sample();
            if (listOfNumbers.size() == 1) {
                foundListWithMinimumSize = true;
                break;
            }
        }
        assertThat(foundListWithMinimumSize).isTrue();
    }

    @Test
    @DisplayName("nonEmptyListOf should use the source of randomness from the underlying generator")
    void nonEmptyListOfShouldUseTheSourceOfRandomnessFromTheUnderlyingGenerator() {
        final Gen<List<Integer>> nonEmptyListOfGenL = Gen.nonEmptyListOf(Gen.choose(1, 1000, new Random(1)), 10);
        final Gen<List<Integer>> nonEmptyListOfGenR = Gen.nonEmptyListOf(Gen.choose(1, 1000, new Random(1)), 10);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(nonEmptyListOfGenL.sample()).isEqualTo(nonEmptyListOfGenR.sample());
        }
    }

    @Test
    @DisplayName("nonEmptyListOf should generate lists whose length is evenly distributed in the range of [1, maxLength]")
    void nonEmptyListOfShouldGenerateListsWhoseLengthIsEvenlyDistributed() {
        final Gen<List<Integer>> nonEmptyListOfGen = Gen.nonEmptyListOf(Gen.choose(1, 1000), 10);
        final int[] frequencies = new int[10];
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final int index = nonEmptyListOfGen.sample().size() - 1;
            frequencies[index] = frequencies[index] + 1;
        }
        for (int frequency : frequencies) {
            assertThat(frequency).isCloseTo(MAX_NUMBER_OF_PROBES / 10, Percentage.withPercentage(15.0));
        }
    }
}
