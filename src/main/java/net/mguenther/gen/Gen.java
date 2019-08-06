package net.mguenther.gen;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Markus Günther (markus.guenther@gmail.com)
 */
public class Gen<T> {

    private final Function<Random, T> valueGenerator;

    private final Random sourceOfRandomness;

    /**
     * Consumes a {@link Supplier<T>} that provides attributes of type {@code T}. Although this
     * constructor expects a source of randomness (cf. {@link Random}), this source of randomness
     * is not applied to the given {@link Supplier}.
     *
     * @param valueProvider
     * @param sourceOfRandomness
     */
    public Gen(final Supplier<T> valueProvider,
               final Random sourceOfRandomness) {
        this.valueGenerator = r -> valueProvider.get();
        this.sourceOfRandomness = sourceOfRandomness;
    }

    public Gen(final Function<Random, T> valueGenerator) {
        this(valueGenerator, new Random());
    }

    public Gen(final Function<Random, T> valueGenerator,
               final Random sourceOfRandomness) {
        this.valueGenerator = valueGenerator;
        this.sourceOfRandomness = sourceOfRandomness;
    }

    public T sample() {
        return valueGenerator.apply(sourceOfRandomness);
    }

    public <U> Gen<U> thenApply(final Function<? super T, ? extends U> mappingFn) {
        return new Gen<>(r -> mappingFn.apply(sample()), sourceOfRandomness);
    }

    public <U> Gen<U> thenCombine(final BiFunction<Random, ? super T, ? extends U> mappingFn) {
        return new Gen<>(r -> mappingFn.apply(sourceOfRandomness, sample()), sourceOfRandomness);
    }

    public <U> Gen<U> thenCompose(final Function<? super T, ? extends Gen<U>> mappingFn) {
        return new Gen<>(r -> mappingFn.apply(sample()).sample(), sourceOfRandomness);
    }

    public <U> Gen<U> thenCompose(final BiFunction<Random, ? super T, ? extends Gen<U>> mappingFn) {
        return new Gen<>(r -> mappingFn.apply(r, sample()).sample(), sourceOfRandomness);
    }

    public static <T> Gen<T> constant(final T value) {
        return constant(value, new Random());
    }

    public static <T> Gen<T> constant(final T value,
                                      final Random random) {
        return new Gen<>(() -> value, random);
    }

    public static <T> Gen<T> oneOf(final T... values) {
        return oneOf(Arrays.asList(values));
    }

    public static <T> Gen<T> oneOf(final List<T> values) {
        return oneOf(values, new Random());
    }

    public static <T> Gen<T> oneOf(final List<T> values,
                                   final Random random) {
        final Function<Random, T> f = r -> {
            final int i = r.nextInt(values.size());
            return values.get(i);
        };
        return new Gen<>(f, random);
    }

    public static <T> Gen<List<T>> listOf(final Gen<T> gen,
                                          final int maxLength) {
        final Function<Random, List<T>> f = r -> {
            final int length = r.nextInt(Math.max(0, maxLength));
            return Stream.iterate(gen, t -> t)
                    .limit(length)
                    .map(Gen::sample)
                    .collect(Collectors.toList());
        };
        return new Gen<>(f, gen.sourceOfRandomness);
    }

    public static <T> Gen<List<T>> nonEmptyListOf(final Gen<T> gen,
                                                  final int maxLength) {
        final Function<Random, List<T>> f = r -> {
            final int length = r.nextInt(Math.max(0, Math.min(Integer.MAX_VALUE - 1, maxLength) + 1));
            return Stream.iterate(gen, t -> t)
                    .limit(length)
                    .map(Gen::sample)
                    .collect(Collectors.toList());
        };
        return new Gen<>(f, gen.sourceOfRandomness);
    }

    public static <T> Gen<List<T>> listOfN(final Gen<T> gen,
                                           final int length) {
        final Function<Random, List<T>> f = r -> {
            return Stream.iterate(gen, t -> t)
                    .limit(Math.max(0, length))
                    .map(Gen::sample)
                    .collect(Collectors.toList());
        };
        return new Gen<>(f, gen.sourceOfRandomness);
    }

    public static Gen<Integer> nonNegativeInteger() {
        return nonNegativeInteger(new Random());
    }

    public static Gen<Integer> nonNegativeInteger(final Random sourceOfRandomness) {
        final Function<Random, Integer> f = r -> {
            final int i = r.nextInt();
            return i < 0 ? -(i + 1) : i;
        };
        return new Gen<>(f, sourceOfRandomness);
    }

    public static Gen<Integer> choose(final int start,
                                      final int stopExclusive) {
        return choose(start, stopExclusive, new Random());
    }

    public static Gen<Integer> choose(final int start,
                                      final int stopExclusive,
                                      final Random sourceOfRandomness) {
        return nonNegativeInteger(sourceOfRandomness)
                .thenApply(n -> start + (n % (stopExclusive - start)));
    }

    public static Gen<Integer> even(final int start,
                                    final int stopExclusive) {
        return even(start, stopExclusive, new Random());
    }

    public static Gen<Integer> even(final int start,
                                    final int stopExclusive,
                                    final Random sourceOfRandomness) {
        final int stop = stopExclusive % 2 == 0 ? stopExclusive - 1 : stopExclusive;
        return choose(start, stop, sourceOfRandomness)
                .thenApply(n -> n % 2 != 0 ? n + 1 : n);
    }

    public static Gen<Integer> odd(final int start,
                                   final int stopExclusive) {
        return odd(start, stopExclusive, new Random());
    }

    public static Gen<Integer> odd(final int start,
                                   final int stopExclusive,
                                   final Random sourceOfRandomness) {
        final int stop = stopExclusive % 2 != 0 ? stopExclusive - 1 : stopExclusive;
        return choose(start, stop, sourceOfRandomness)
                .thenApply(n -> n % 2 == 0 ? n + 1 : n);
    }

    public static Gen<Double> normalizedDouble() {
        return normalizedDouble(new Random());
    }

    public static Gen<Double> normalizedDouble(final Random sourceOfRandomness) {
        return nonNegativeInteger(sourceOfRandomness)
                .thenApply(n -> n / ((double) Integer.MAX_VALUE + 1));
    }

    public static Gen<Double> choose(final double start,
                                     final double stopExclusive) {
        return choose(start, stopExclusive, new Random());
    }

    public static Gen<Double> choose(final double start,
                                     final double stopExclusive,
                                     final Random sourceOfRandomness) {
        return normalizedDouble(sourceOfRandomness)
                .thenApply(d -> start + d * (stopExclusive - start));
    }

    public static <T> Gen<T> weighted(final double threshold,
                                      final Gen<T> genT1,
                                      final Gen<T> genT2) {
        return weighted(threshold, genT1, genT2, new Random());
    }

    public static <T> Gen<T> weighted(final double threshold,
                                      final Gen<T> genT1,
                                      final Gen<T> genT2,
                                      final Random sourceOfRandomness) {
        return normalizedDouble(sourceOfRandomness)
                .thenCompose(probability -> probability < threshold ? genT1 : genT2);
    }

    private static final String DIGITS = "0123456789";
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase() + DIGITS;

    public static Gen<String> asciiString(final int length) {
        return asciiString(length, new Random());
    }

    public static Gen<String> asciiString(final int length,
                                          final Random sourceOfRandomness) {
        return listOfN(choose(0, 127, sourceOfRandomness), length)
                .thenApply(list -> list
                        .stream()
                        .map(Character::toChars)
                        .map(String::valueOf)
                        .reduce("", String::concat, String::concat));
    }

    public static Gen<String> alphaNumString(final int length) {
        return alphaNumString(length, new Random());
    }

    public static Gen<String> alphaNumString(final int length,
                                             final Random sourceOfRandomness) {
        return fromAlphabetString(length, ALPHABET, sourceOfRandomness);
    }

    public static Gen<String> numString(final int length) {
        return numString(length, new Random());
    }

    public static Gen<String> numString(final int length,
                                        final Random sourceOfRandomness) {
        return fromAlphabetString(length, DIGITS, sourceOfRandomness);
    }

    public static Gen<String> fromAlphabetString(final int length,
                                                  final String alphabet) {
        return fromAlphabetString(length, alphabet, new Random());
    }

    public static Gen<String> fromAlphabetString(final int length,
                                                  final String alphabet,
                                                  final Random sourceOfRandomness) {
        return listOfN(choose(0, alphabet.length(), sourceOfRandomness), length)
                .thenApply(list -> list
                        .stream()
                        .map(alphabet::charAt)
                        .map(String::valueOf)
                        .reduce("", String::concat, String::concat));
    }
}
