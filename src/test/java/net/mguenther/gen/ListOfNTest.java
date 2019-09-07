package net.mguenther.gen;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class ListOfNTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    void listOfNShouldAlwaysProduceListsWithTheGivenSize() {
        final Gen<List<Integer>> listOfNGen = Gen.listOfN(Gen.choose(1, 1000), 10);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(listOfNGen.sample()).hasSize(10);
        }
    }

    @Test
    void listOfNShouldGenerateEmptyListsIfNIsEqualToZero() {
        final Gen<List<Integer>> listOfNGen = Gen.listOfN(Gen.choose(1, 1000), 0);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(listOfNGen.sample()).isEmpty();
        }
    }

    @Test
    void listOfNShouldGenerateEmptyListsIfNIsNegative() {
        final Gen<List<Integer>> listOfNGen = Gen.listOfN(Gen.choose(1, 1000), -1);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(listOfNGen.sample()).isEmpty();
        }
    }

    @Test
    void listOfNShouldUseTheSourceOfRandomnessFromTheUnderlyingGenerator() {
        final Gen<List<Integer>> listOfNGenL = Gen.listOfN(Gen.choose(1, 1000, new Random(1)), 5);
        final Gen<List<Integer>> listOfNGenR = Gen.listOfN(Gen.choose(1, 1000, new Random(1)), 5);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(listOfNGenL.sample()).isEqualTo(listOfNGenR.sample());
        }
    }
}
