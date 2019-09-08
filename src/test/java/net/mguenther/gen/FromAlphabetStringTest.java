package net.mguenther.gen;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FromAlphabetStringTest {

    private static final int MAX_NUMBER_OF_PROBES = 10_000;

    private static final String TEST_ALPHABET = "0123456789";

    @Test
    @DisplayName("fromAlphabetString should use every character of the given alphabet")
    void fromAlphabetStringShouldUseEveryCharacterOfTheGivenAlphabet() {
        final int[] sampleCountPerChars = new int[TEST_ALPHABET.length()];
        final Gen<String> fromAlphabetStringGen = Gen.fromAlphabetString(10, TEST_ALPHABET);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final String generatedString = fromAlphabetStringGen.sample();
            for (int j = 0; j < generatedString.length(); j++) {
                final int index = Integer.parseInt(String.valueOf(generatedString.charAt(j)));
                sampleCountPerChars[index] = sampleCountPerChars[index] + 1;
            }
        }
        for (int sampleCountPerChar : sampleCountPerChars) {
            assertThat(sampleCountPerChar).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("fromAlphabetString should evenly distribute characters of the given alphabet")
    void fromAlphabetStringShouldEvenlyDistributeCharactersOfTheGivenAlphabet() {
        final int[] sampleCountPerChars = new int[TEST_ALPHABET.length()];
        final Gen<String> fromAlphabetStringGen = Gen.fromAlphabetString(10, TEST_ALPHABET);
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            final String generatedString = fromAlphabetStringGen.sample();
            for (int j = 0; j < generatedString.length(); j++) {
                final int index = Integer.parseInt(String.valueOf(generatedString.charAt(j)));
                sampleCountPerChars[index] = sampleCountPerChars[index] + 1;
            }
        }
        for (int sampleCountPerChar : sampleCountPerChars) {
            assertThat(sampleCountPerChar).isCloseTo(MAX_NUMBER_OF_PROBES, Percentage.withPercentage(5.0));
        }
    }

    @Test
    @DisplayName("two fromAlphabetString generators using the same seed should generate the same string in the same order")
    void twoFromAlphabetStringGeneratorsUsingTheSameSeedShouldGenerateTheSameStringInTheSameOrder() {
        final Gen<String> fromAlphabetStringGenL = Gen.fromAlphabetString(32, TEST_ALPHABET, new Random(1));
        final Gen<String> fromAlphabetStringGenR = Gen.fromAlphabetString(32, TEST_ALPHABET, new Random(1));
        for (int i = 0; i < MAX_NUMBER_OF_PROBES; i++) {
            assertThat(fromAlphabetStringGenL.sample()).isEqualTo(fromAlphabetStringGenR.sample());
        }
    }

    @Test
    @DisplayName("fromAlphabetString should throw IllegalArgumentException if the given alphabet is null")
    void fromAlphabetStringShouldThrowIllegalArgumentExceptionIfTheGivenAlphabetIsNull() {
        assertThatThrownBy(() -> Gen.fromAlphabetString(10, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("fromAlphabetString should throw IllegalArgumentException if the given alphabet is empty")
    void fromAlphabetStringShouldThrowIllegalArgumentExceptionIfTheGivenAlphabetIsEmpty() {
        assertThatThrownBy(() -> Gen.fromAlphabetString(32, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("fromAlphabetString should throw IllegalArgumentException if the given length is negative")
    void fromAlphabetStringShouldThrowIllegalArgumentExceptionIfTheGivenLengthIsNegative() {
        assertThatThrownBy(() -> Gen.fromAlphabetString(-1, "abc"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
