package net.mguenther.gen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class GenTest {

    @Test
    @DisplayName("sample should return a value obtained from the value generator")
    void sampleShouldReturnValueObtainedFromValueGenerator() {
        assertThat(Gen.constant("a").sample()).isEqualTo("a");
    }

    @Test
    @DisplayName("map should return new generator with mapping function applied to value generator")
    void mapShouldReturnNewGeneratorWithMappingFunctionAppliedToValueGenerator() {

        final Gen<Integer> gen = Gen.constant("abc")
                .map((Function<? super String, ? extends String>) String::toUpperCase)
                .map(String::length);

        assertThat(gen.sample()).isEqualTo(3);
    }

    @Test
    @DisplayName("map should return new generator based on mapping function that leverages randomness")
    void mapShouldReturnNewGeneratorBasedOnMappingFunctionThatLeveragesSourceOfRandomness() {

        final Gen<String> gen = Gen.constant("abc", new Random(1L))
                .map((r, v) -> r.nextInt())
                .map(String::valueOf);

        assertThat(gen.sample()).isEqualTo("-1155869325");
    }

    @Test
    void flatMapShouldPassOnTheSameSourceOfRandomnessFromTheFirstGenerator() {

        final Gen<Tuple> tupleGen = Gen.nonNegativeInteger(new Random(1L))
                .flatMap((r1, x) -> Gen.nonNegativeInteger(r1)
                .flatMap((r2, y) -> Gen.nonNegativeInteger(r2)
                .map(z -> new Tuple(x, y, z))));

        final Tuple tuple = tupleGen.sample();

        // the assertions underneath would fail for y and z if flatMap would not properly
        // pass on the source of randomness from the first Generator
        assertThat(tuple.x).isEqualTo(1155869324);
        assertThat(tuple.y).isEqualTo(431529176);
        assertThat(tuple.z).isEqualTo(1761283695);
    }

    static class Tuple {
        private final int x;
        private final int y;
        private final int z;

        Tuple(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "Tuple{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }
}
