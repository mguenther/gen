package net.mguenther.gen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class LiftTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("lift should wrap existing method into an instance of Gen")
    void liftShouldWrapExistingMethodIntoGen() {

        final Gen<Integer> randomIntGen = Gen.lift(this::myExistingRandomIntMethod);

        assertThat(randomIntGen.sample()).isNotNull();
        assertThat(randomIntGen.sample()).satisfies(Conditions.isWithinBoundary(0, 1_000_000));
    }

    @Test
    @DisplayName("calling sample on lifted method should always invoke the underlying method")
    void sampleOnLiftedMethodShouldAlwaysInvokeTheUnderlyingMethod() {

        final Gen<Integer> randomIntGen = Gen.lift(this::myExistingRandomIntMethod);

        final Integer l = randomIntGen.sample();
        final Integer r = randomIntGen.sample();

        assertThat(l).isNotEqualTo(r);
    }

    @Test
    @DisplayName("a lifted method should be indepenent of the Gen's source of randomness")
    void liftedMethodShouldBeIndependentOfSourceOfRandomness() {

        final Gen<Integer> randomIntGenL = Gen.lift(this::myExistingRandomIntMethod, new Random(1));
        final Gen<Integer> randomIntGenR = Gen.lift(this::myExistingRandomIntMethod, new Random(1));

        assertThat(randomIntGenL.sample()).isNotEqualTo(randomIntGenR.sample());
    }

    @Test
    @DisplayName("lift should preserve the given source of randomness when using combinators")
    void liftShouldPreserveTheGivenSourceOfRandomnessWhenUsingCombinators() {

        final Gen<Integer> randomIntGenL = Gen.lift(this::myExistingRandomIntMethod, new Random(1))
                .flatMap((r1, randomIndependentNumber) -> Gen.choose(1, 1_000_000, r1)
                .map(randomDependentNumber -> randomDependentNumber));
        final Gen<Integer> randomIntGenR = Gen.lift(this::myExistingRandomIntMethod, new Random(1))
                .flatMap((r1, randomIndependentNumber) -> Gen.choose(1, 1_000_000, r1)
                .map(randomDependentNumber -> randomDependentNumber));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(randomIntGenL.sample()).isEqualTo(randomIntGenR.sample());
        }
    }

    private Integer myExistingRandomIntMethod() {
        return (int) (Math.random() * 1_000_000);
    }
}
