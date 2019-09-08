package net.mguenther.gen;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsciiStringTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    @Test
    @DisplayName("asciiString should use the printable ASCII alphabet from range [32; 127)")
    void asciiStringShouldUseThePrintableAsciiAlphabet() {
        final int[] sampleCountPerAsciiChar = new int[127];
        final Gen<String> asciiStringGen = Gen.asciiString(95);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final String asciiString = asciiStringGen.sample();
            for (int j = 0; j < asciiString.length(); j++) {
                final int index = asciiString.charAt(j);
                sampleCountPerAsciiChar[index] = sampleCountPerAsciiChar[index] + 1;
            }
        }
        for (int i = 32; i < sampleCountPerAsciiChar.length; i++) {
            assertThat(sampleCountPerAsciiChar[i]).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("asciiString should evenly distribute generated chars")
    void asciiStringShouldEvenlyDistributeGeneratedChars() {
        final int[] sampleCountPerAsciiChar = new int[127];
        final Gen<String> asciiStringGen = Gen.asciiString(95);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final String asciiString = asciiStringGen.sample();
            for (int j = 0; j < asciiString.length(); j++) {
                final int index = asciiString.charAt(j);
                sampleCountPerAsciiChar[index] = sampleCountPerAsciiChar[index] + 1;
            }
        }
        for (int i = 32; i < sampleCountPerAsciiChar.length; i++) {
            assertThat(sampleCountPerAsciiChar[i]).isCloseTo(MAX_NUMBER_OF_PROBES, Percentage.withPercentage(5.0));
        }
    }

    @Test
    @DisplayName("two asciiString generators using the same seed should generate the same Ascii strings in the same order")
    void twoAsciiStringGeneratorsUsingTheSameSeedShouldGenerateTheSameAsciiStringsInTheSameOrder() {
        final Gen<String> asciiStringGenL = Gen.asciiString(32, new Random(1));
        final Gen<String> asciiStringGenR = Gen.asciiString(32, new Random(1));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(asciiStringGenL.sample()).isEqualTo(asciiStringGenR.sample());
        }
    }

    @Test
    @DisplayName("asciiString should throw IllegalArgumentException if the given length is negative")
    void asciiStringShouldThrowIllegalArgumentExceptionIfTheGivenLengthIsNegative() {
        assertThatThrownBy(() -> Gen.asciiString(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
