package net.mguenther.gen;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightedTest {

    private static final int MAX_NUMBER_OF_PROBES = 1_000_000;

    @Test
    @DisplayName("weighted should use the threshold to distribute samples between the given generators (max. deviation of 1%)")
    void weightedShouldUseTheThresholdToDistributeSamplesBetweenTheGivenGenerators() {
        final int[] sampleCountPerGenerator = new int[2];
        final Gen<Integer> weightedGen = Gen.weighted(0.3, Gen.constant(0), Gen.constant(1));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final int index = weightedGen.sample();
            sampleCountPerGenerator[index] = sampleCountPerGenerator[index] + 1;
        }
        assertThat(sampleCountPerGenerator[0]).isCloseTo((int) (MAX_NUMBER_OF_PROBES * 0.3), Percentage.withPercentage(1.0));
        assertThat(sampleCountPerGenerator[1]).isCloseTo((int) (MAX_NUMBER_OF_PROBES * (1 - 0.3)), Percentage.withPercentage(1.0));
    }

    @Test
    @DisplayName("weighted should throw IllegalArgumentException if threshold is smaller than or equal to lower bound of 0.0")
    void weightedShouldThrowIllegalArgumentExceptionIfThresholdIsSmallerThanLowerBound() {
        assertThatThrownBy(() -> Gen.weighted(0.0, Gen.constant(0), Gen.constant(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("weighted should throw IllegalArgumentException if threshold is larger than or equal to upper bound of 1.0")
    void weightedShouldThrowIllegalArgumentExceptionIfThresholdIsLargerThanUpperBound() {
        assertThatThrownBy(() -> Gen.weighted(1.0, Gen.constant(0), Gen.constant(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("two weighted generators using the same seed should generate the same sample distribution between their underlying generators")
    void twoWeightedGeneratorsUsingTheSameSeedShouldGenerateTheSameSampleDistributionBetweenTheirUnderlyingGenerators() {
        final Gen<Integer> weightedGenL = Gen.weighted(0.5, Gen.constant(0), Gen.constant(1), new Random(1));
        final Gen<Integer> weightedGenR = Gen.weighted(0.5, Gen.constant(0), Gen.constant(1), new Random(1));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(weightedGenL.sample()).isEqualTo(weightedGenR.sample());
        }
    }
}
