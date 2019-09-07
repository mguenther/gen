package net.mguenther.gen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: Remark on methods that accept java.util.Random as source of randomness - the user can control the seed of the randomizer
 *
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

    /**
     * Constructs a generator that always returns the same value {@code} of type {@code T}. Retains
     * the given source of randomness when combined with other generators.
     *
     * @param value
     *      the constant value that this generator should produce
     * @param <T>
     *      paramterized type of the constant value
     * @return
     *      a {@code Gen}erator that always produces the same value
     */
    public static <T> Gen<T> constant(final T value) {
        return constant(value, new Random());
    }

    /**
     * Constructs a generator that always returns the same value {@code} of type {@code T}. Retains
     * the given source of randomness when combined with other generators.
     *
     * @param value
     *      the constant value that this generator should produce
     * @param sourceOfRandomness
     *      uses the given instance of {@link java.util.Random} as source of randomness
     * @param <T>
     *      paramterized type of the constant value
     * @return
     *      a {@code Gen}erator that always produces the same value
     */
    public static <T> Gen<T> constant(final T value,
                                      final Random sourceOfRandomness) {
        return new Gen<>(() -> value, sourceOfRandomness);
    }

    /**
     * Constructs a generator that generates values of type {@code T} from the given varargs.
     *
     * @param values
     *      list of values of type {@code T} from which the returned {@code Gen} produces values
     * @return
     *      a {@code Gen}erator that generates values from the given list of values of type {@code T}
     */
    @SafeVarargs
    public static <T> Gen<T> oneOf(final T... values) {
        return oneOf(Arrays.asList(values));
    }

    /**
     * Constructs a generator that generates values of type {@code T} from the given {@link java.util.List}.
     *
     * @param values
     *      list of values of type {@code T} from which the returned {@code Gen} produces values
     * @return
     *      a {@code Gen}erator that generates values from the given list of values of type {@code T}
     */
    public static <T> Gen<T> oneOf(final List<T> values) {
        return oneOf(values, new Random());
    }

    /**
     * Constructs a generator that generates values of type {@code T} from the given {@link java.util.List}.
     *
     * @param values
     *      list of values of type {@code T} from which the returned {@code Gen} produces values
     * @param sourceOfRandomness
     *      uses the given instance of {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that generates values from the given list of values of type {@code T}
     */
    public static <T> Gen<T> oneOf(final List<T> values,
                                   final Random sourceOfRandomness) {
        if (values.isEmpty()) throw new IllegalArgumentException("The given list of values cannot be empty.");
        final Function<Random, T> f = r -> {
            final int i = r.nextInt(values.size());
            return values.get(i);
        };
        return new Gen<>(f, sourceOfRandomness);
    }

    /**
     * Constructs a generator that generates a {@link java.util.List} of length [0; {@code maxLength}]
     * while using the given generator {@code gen} to produce the elements of the list. This generator
     * uses the source of randomness from the given generator {@code gen}. The {@link java.util.List}
     * may be empty.
     *
     * Given the same seed, two instances of the enclosed generator will produce lists with the same
     * length and the same elements inside of the list.
     *
     * @param gen
     *      {@code Gen}erator that is used to produce the elements of the list
     * @param maxLength
     *      this is the maximum length (inclusive) that generated lists should have
     * @param <T>
     *      parameterized type of the elements in the {@link java.util.List}
     * @return
     *      a {@code Gen}erator that produces lists up to the size of {@code maxLength} where the
     *      elements of that list are produced using the given generator
     */
    public static <T> Gen<List<T>> listOf(final Gen<T> gen,
                                          final int maxLength) {
        final Function<Random, List<T>> f = r -> {
            final int sanitizedMaxLength = Math.max(0, includeUpperBound(maxLength));
            return sanitizedMaxLength == 0 ? Collections.emptyList() : Stream.iterate(gen, t -> t)
                    .limit(r.nextInt(sanitizedMaxLength))
                    .map(Gen::sample)
                    .collect(Collectors.toList());
        };
        return new Gen<>(f, gen.sourceOfRandomness);
    }

    private static int includeUpperBound(final int upperBoundExclusive) {
        return upperBoundExclusive + 1;
    }

    /**
     * Constructs a generator that generates a {@link java.util.List} of length {@code length}, while
     * using the given generator {@code gen} to produce the elements of the list. This generator uses
     * the source of randomness from the given generator {@code gen}. If {@code length} is less than or
     * equal to zero, the generator will produce empty lists.
     *
     * Given the same seed, two instances of the enclosed generator will produce lists with the same
     * length and the same elements inside of the list.
     *
     * @param gen
     *      {@code Gen}erator that is used to produce the elements of the list
     * @param length
     *      this is the length (inclusive) that generated should have
     * @param <T>
     *      parameterized type of the elements in the {@link java.util.List}
     * @return
     *      a {@code Gen}erator that produces lists of the same size {@code length} where the
     *      elements of that list are produced using the given generator
     */
    public static <T> Gen<List<T>> listOfN(final Gen<T> gen,
                                           final int length) {
        final Function<Random, List<T>> f = r -> Stream.iterate(gen, t -> t)
                .limit(Math.max(0, length))
                .map(Gen::sample)
                .collect(Collectors.toList());
        return new Gen<>(f, gen.sourceOfRandomness);
    }

    /**
     * Constructs a generator that generates a {@link java.util.List} of length [1; {@code maxLength}]
     * while using the given generator {@code gen} to produce the elements of the list. This generator
     * uses the source of randomness from the given generator {@code gen}.
     *
     * Given the same seed, two instances of the enclosed generator will produce lists with the same
     * length and the same elements inside of the list.
     *
     * @param gen
     *      {@code Gen}erator that is used to produce the elements of the list
     * @param maxLength
     *      this is the maximum length (inclusive) that generated lists should have
     * @param <T>
     *      parameterized type of the elements in the {@link java.util.List}
     * @return
     *      a {@code Gen}erator that produces lists up to the size of {@code maxLength} where the
     *      elements of that list are produced using the given generator; generated lists are
     *      never empty
     */
    public static <T> Gen<List<T>> nonEmptyListOf(final Gen<T> gen,
                                                  final int maxLength) {
        if (maxLength <= 0) throw new IllegalArgumentException("the given maxLength of a nonEmptyListOf generator must be larger than 0");
        final Function<Random, List<T>> f = r -> {
            // using the randomly generated int as argument for includeUpperBound also ensure that the
            // generator does not produce empty lists
            final int length = includeUpperBound(r.nextInt(Math.min(Integer.MAX_VALUE - 1, maxLength)));
            return Stream.iterate(gen, t -> t)
                    .limit(length)
                    .map(Gen::sample)
                    .collect(Collectors.toList());
        };
        return new Gen<>(f, gen.sourceOfRandomness);
    }

    /**
     * Constructs a generator that generates {@link Integer}s {@code x} that are larger than or equal
     * to zero. The largest integer that this generator may generate is {@code Integer.MAX_VALUE}.
     *
     * @return
     *      a {@code Gen}erator that generates {@link Integer}s that are larger than or equal to zero
     */
    public static Gen<Integer> nonNegativeInteger() {
        return nonNegativeInteger(new Random());
    }

    /**
     * Constructs a generator that generates {@link Integer}s {@code x} that are larger than or equal
     * to zero. The largest integer that this generator may generate is {@code Integer.MAX_VALUE}.
     *
     * @param sourceOfRandomness
     *      uses the given {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that generates {@link Integer}s that are larger than or equal to zero
     */
    public static Gen<Integer> nonNegativeInteger(final Random sourceOfRandomness) {
        final Function<Random, Integer> f = r -> {
            final int i = r.nextInt();
            return i < 0 ? -(i + 1) : i;
        };
        return new Gen<>(f, sourceOfRandomness);
    }

    /**
     * Constructs a generator that generates {@link Integer}s. The generated {@link Integer}s are within the
     * interval [start; stopExclusive). The largest admissible interval is [{@code Integer.MIN_VALUE},
     * {@code Integer.MAX_VALUE}).
     *
     * @param start
     *      lower bound of the interval from which {@link Integer}s are generated
     * @param stopExclusive
     *      upper bound of the interval from which {@link Integer}s are generated; the {@link Integer}
     *      representing the upper bound is never generated by this generator
     * @return
     *      a {@code Gen}erator that generates {@link Integer}s that are within the given interval
     */
    public static Gen<Integer> choose(final int start,
                                      final int stopExclusive) {
        return choose(start, stopExclusive, new Random());
    }

    /**
     * Constructs a generator that generates {@link Integer}s. The generated {@link Integer}s are within the
     * interval [start; stopExclusive). The largest admissible interval is [{@code Integer.MIN_VALUE},
     * {@code Integer.MAX_VALUE}).
     *
     * @param start
     *      lower bound of the interval from which {@link Integer}s are generated
     * @param stopExclusive
     *      upper bound of the interval from which {@link Integer}s are generated; the {@link Integer}
     *      representing the upper bound is never generated by this generator
     * @param sourceOfRandomness
     *      uses the given {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that generates {@link Integer}s that are within the given interval
     */
    public static Gen<Integer> choose(final int start,
                                      final int stopExclusive,
                                      final Random sourceOfRandomness) {
        return nonNegativeInteger(sourceOfRandomness)
                .thenApply(n -> start + (n % (stopExclusive - start)));
    }

    /**
     * Constructs a generator that generates {@link Integer}s {@code x} that are congruent modulo 2. The
     * generated {@link Integer}s are within the interval [start, stopExclusive). The largest admissible
     * interval is [{@code Integer.MIN_VALUE}, {@code Integer.MAX_VALUE}).
     *
     * Uses a source of randomness with a randomized seed.
     *
     * @param start
     *      lower bound of the interval from which even {@link Integer}s are generated
     * @param stopExclusive
     *      upper bound of the interval from which even {@link Integer}s are generated; the {@link Integer}
     *      representing the upper bound is never generated by this generator
     * @return
     *      a {@code Gen}erator that generates {@link Integer}s {@code x} that are congruent modulo 2
     */
    public static Gen<Integer> even(final int start,
                                    final int stopExclusive) {
        return even(start, stopExclusive, new Random());
    }

    /**
     * Constructs a generator that generates {@link Integer}s {@code x} that are congruent modulo 2. The
     * generated {@link Integer}s are within the interval [start, stopExclusive). The largest admissible
     * interval is [{@code Integer.MIN_VALUE}, {@code Integer.MAX_VALUE}).
     *
     * Uses the given {@link java.util.Random} as source of randomness.
     *
     * @param start
     *      lower bound of the interval from which even {@link Integer}s are generated
     * @param stopExclusive
     *      upper bound of the interval from which even {@link Integer}s are generated; the {@link Integer}
     *      representing the upper bound is never generated by this generator
     * @param sourceOfRandomness
     *      uses the given {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that generates {@link Integer}s {@code x} that are congruent modulo 2
     */
    public static Gen<Integer> even(final int start,
                                    final int stopExclusive,
                                    final Random sourceOfRandomness) {
        if (start >= stopExclusive) {
            final String message = "The given lower bound '%s' must be smaller than the given upper bound '%s'.";
            throw new IllegalArgumentException(String.format(message, start, stopExclusive));
        }
        final int stop = stopExclusive % 2 == 0 ? stopExclusive - 1 : stopExclusive;
        return choose(start, stop, sourceOfRandomness)
                .thenApply(n -> n % 2 != 0 ? n + 1 : n);
    }

    /**
     * Constructs a generator that generates {@link Integer}s {@code x} that are not congruent modulo 2. The
     * generated {@link Integer}s are within the interval [start; stopExclusive). The largest admissible
     * interval is [{@code Integer.MIN_VALUE}, {@code Integer.MAX_VALUE}).
     *
     * Uses a source of randomness with a randomized seed.
     *
     * @param start
     *      lower bound of the interval from which odd {@link Integer}s are generated
     * @param stopExclusive
     *      upper bound of the interval from which odd {@link Integer}s are generated; the {@link Integer}
     *      representing the upper bound is never generated by this generator
     * @return
     *      a {@code Gen}erator that generates {@link Integer}s {@code x} that are not congruent modulo 2
     */
    public static Gen<Integer> odd(final int start,
                                   final int stopExclusive) {
        return odd(start, stopExclusive, new Random());
    }

    /**
     * Constructs a generator that generates {@link Integer}s {@code x} that are not congruent modulo 2. The
     * generated {@link Integer}s are within the interval [start; stopExclusive). The largest admissible
     * interval is [{@code Integer.MIN_VALUE}, {@code Integer.MAX_VALUE}).
     *
     * Uses the given instance of {@link java.util.Random} as source of randomness.
     *
     * @param start
     *      lower bound of the interval from which odd {@link Integer}s are generated
     * @param stopExclusive
     *      upper bound of the interval from which odd {@link Integer}s are generated; the {@link Integer}
     *      representing the upper bound is never generated by this generator
     * @param sourceOfRandomness
     *      uses the given instance of {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that generates {@link Integer}s {@code x} that are not congruent modulo 2
     */
    public static Gen<Integer> odd(final int start,
                                   final int stopExclusive,
                                   final Random sourceOfRandomness) {
        if (start >= stopExclusive) {
            final String message = "The given lower bound '%s' must be smaller than the given upper bound '%s'.";
            throw new IllegalArgumentException(String.format(message, start, stopExclusive));
        }
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

    /**
     * Constructs a generator that generates {@link Double}s. The generated {@link Double}s are within the
     * interval [start; stopExclusive).
     *
     * @param start
     *      lower bound of the interval from which {@link Double}s are generated
     * @param stopExclusive
     *      upper bound (exclusive) of the interval from which {@link Double}s are generated
     * @return
     *      a {@code Gen}erator that generates {@link Double}s that are within the interval [start; stopExclusive)
     */
    public static Gen<Double> choose(final double start,
                                     final double stopExclusive) {
        return choose(start, stopExclusive, new Random());
    }

    /**
     * Constructs a generator that generates {@link Double}s. The generated {@link Double}s are within the
     * interval [start; stopExclusive).
     *
     * Uses the given instance of {@link java.util.Random} as source of randomness.
     *
     * @param start
     *      lower bound of the interval from which {@link Double}s are generated
     * @param stopExclusive
     *      upper bound (exclusive) of the interval from which {@link Double}s are generated
     * @param sourceOfRandomness
     *      uses the given instance of {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that generates {@link Double}s that are within the interval [start; stopExclusive)
     */
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
