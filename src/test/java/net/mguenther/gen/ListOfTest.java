package net.mguenther.gen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class ListOfTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("listOf should only generate empty lists if maxLength is negative")
    void listOfShouldGenerateOnlyEmptyListsIfMaxLengthIsNegative() {
        final Gen<List<Integer>> listOfGen = Gen.listOf(Gen.choose(1, 1000), -1);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(listOfGen.sample()).isEmpty();
        }
    }

    @Test
    @DisplayName("listOf should only generate empty lists of maxLength is zero")
    void listOfShouldGenerateOnlyEmptyListsIfMaxLengthIsZero() {
        final Gen<List<Integer>> listOfGen = Gen.listOf(Gen.choose(1, 1000), 0);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(listOfGen.sample()).isEmpty();
        }
    }

    @Test
    @DisplayName("listOf should generate lists that do not exceed the given maxLength")
    void listOfShouldGenerateListsOfValuesThatDoNotExceedTheGivenMaxLength() {
        final Gen<List<Integer>> listOfGen = Gen.listOf(Gen.choose(1, 1000), 10);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(listOfGen.sample().size()).isLessThanOrEqualTo(10);
        }
    }

    @Test
    @DisplayName("listOf should be able to generate lists with size equal to maxLength")
    void listOfShouldBeAbleToGenerateListsWithMaximumSize() {
        final Gen<List<Integer>> listOfGen = Gen.listOf(Gen.choose(1, 1000), 10);
        boolean foundListWithMaximumSize = false;
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final List<Integer> listOfNumbers = listOfGen.sample();
            if (listOfNumbers.size() == 10) {
                foundListWithMaximumSize = true;
                break;
            }
        }
        assertThat(foundListWithMaximumSize).isTrue();
    }

    @Test
    @DisplayName("listOf should be able to generate empty lists")
    void listOfShouldBeAbleToGenerateEmptyLists() {
        final Gen<List<Integer>> listOfGen = Gen.listOf(Gen.choose(1, 1000), 10);
        boolean foundEmptyList = false;
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final List<Integer> listOfNumbers = listOfGen.sample();
            if (listOfNumbers.isEmpty()) {
                foundEmptyList = true;
                break;
            }
        }
        assertThat(foundEmptyList).isTrue();
    }

    @Test
    @DisplayName("listOf should use the source of randomness from the underlying generator")
    void listOfShouldUseTheSourceOfRandomnessFromTheUnderlyingGenerator() {
        final Gen<List<Integer>> listOfGenL = Gen.listOf(Gen.choose(1, 1000, new Random(1)), 10);
        final Gen<List<Integer>> listOfGenR = Gen.listOf(Gen.choose(1, 1000, new Random(1)), 10);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(listOfGenL.sample()).isEqualTo(listOfGenR.sample());
        }
    }
}
