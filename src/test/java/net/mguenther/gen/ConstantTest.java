package net.mguenther.gen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class ConstantTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("constant should always generate the same value")
    void constantShouldAlwaysGenerateTheSameValue() {
        final Gen<Integer> constantGen = Gen.constant(1);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(constantGen.sample()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("constant should retain source of randomness if combined with other generators")
    void constantShouldRetainSourceOfRandomnessIfCombinedWithOtherGenerators() {
        final Gen<List<Integer>> constantGenL = Gen.nonEmptyListOf(Gen.constant(1, new Random(1)), 10);
        final Gen<List<Integer>> constantGenR = Gen.nonEmptyListOf(Gen.constant(1, new Random(1)), 10);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(constantGenL.sample()).isEqualTo(constantGenR.sample());
        }
    }
}
