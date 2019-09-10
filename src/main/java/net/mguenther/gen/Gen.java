package net.mguenther.gen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gen<T> {

    private final Function<Random, T> valueGenerator;

    private final Random sourceOfRandomness;

    private Gen(final Supplier<T> valueProvider,
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

    /**
     * Constructs a new generator that wraps {@code this} generator and applies the given {@code mappingFn}
     * when producing samples. Retains the source of randomness of {@code this} generator.
     *
     * @param mappingFn
     *      mapping function that transforms samples of {@code this} generator
     * @param <U>
     *      parameterized type of generated samples
     * @return
     *      a new generator that wraps {@code this} generator and applies the given {@code mappingFn}
     *      when producing samples
     */
    public <U> Gen<U> map(final Function<? super T, ? extends U> mappingFn) {
        return new Gen<>(r -> mappingFn.apply(sample()), sourceOfRandomness);
    }

    /**
     * Constructs a new generator that wraps {@code this} generator and applies the given {@code mappingFn}
     * when producing samples. Exposes the source of randomness of {@code this} generator to the mapping
     * function. Retains the source of randomness of {@code this} generator.
     *
     * @param mappingFn
     *      mapping function that transforms samples of {@code this} generator
     * @param <U>
     *      parameterized type of generated samples
     * @return
     *      a new generator that wraps {@code this} generator and applies the given {@code mappingFn}
     *      when producing samples
     */
    public <U> Gen<U> map(final BiFunction<Random, ? super T, ? extends U> mappingFn) {
        return new Gen<>(r -> mappingFn.apply(r, sample()), sourceOfRandomness);
    }

    /**
     * Constructs a new generator that wraps {@code this} generator by combining it with a different
     * generator. Retains the source of randomness of {@code this} generator.
     *
     * @param mappingFn
     *      mapping function for the combination of {@code this} generator with another generator
     * @param <U>
     *      parameterized type of generated samples
     * @return
     *      a new generator obtained by combining {@code this} generator with another generator
     */
    public <U> Gen<U> flatMap(final Function<? super T, ? extends Gen<U>> mappingFn) {
        return new Gen<>(r -> mappingFn.apply(sample()).sample(), sourceOfRandomness);
    }

    /**
     * Constructs a new generator that wraps {@code this} generator by combining it with a different
     * generator. Exposes the source of randomness of {@code this} generator to the mapping function.
     * Retains the source of randomness of {@code this} generator for the resulting generator, but not
     * implicitly for the generator that {@code this} generator is combined with. It is recommended to
     * pass the source of randomness of {@code this} generator explicitly to the generator to combine with.
     *
     * See the underneath example:
     *
     * <code>
     *     Gen<Tuple> tupleGen = Gen.nonNegativeInteger(new Random(1L))
     *       .flatMap((r1, x) -> Gen.nonNegativeInteger(r1)
     *       .flatMap((r2, x) -> Gen.nonNegativeInteger(r2)
     *       .map(z -> new Tuple(x, y, z))));
     * </code>
     *
     * {@code flatMap} should always be used in this way. This ensure that the resulting generator
     * uses the same source of randomness for all individual operations all the way through.
     *
     * @param mappingFn
     *      mapping function for the combination of {@code this} generator with another generator
     * @param <U>
     *      parameterized type of generated samples
     * @return
     *      a new generator obtained by combining {@code this} generator with another generator
     */
    public <U> Gen<U> flatMap(final BiFunction<Random, ? super T, ? extends Gen<U>> mappingFn) {
        return new Gen<>(r -> mappingFn.apply(r, sample()).sample(), sourceOfRandomness);
    }

    private static final int DEFAULT_LIMIT_FOR_SUCH_THAT = 100_000;

    /**
     * Constructs a new generator that automatically discards samples that do not satisfy the
     * given {@link java.util.Predicate}. To prevent infinite-loops, this method limits the
     * number of samples to a default of 100000 (cf. {@code DEFAULT_LIMIT_FOR_SUCH_THAT}.
     *
     * @param predicate
     *      samples need to satisfy this predicate, otherwise they are discarded by the
     *      generator
     * @throws IllegalStateException
     *      in case the maximum number of samples have been reached and no sample was found
     *      that satisfied the predicate
     * @return
     *      a new generator that discards samples if they do not satisfy the given predicate
     */
    public Gen<T> suchThat(final Predicate<? super T> predicate) {
        return suchThat(predicate, DEFAULT_LIMIT_FOR_SUCH_THAT);
    }

    /**
     * Constructs a new generator that automatically discards samples that do not satisfy the
     * given {@link java.util.Predicate}. To prevent infinite-loops, this method limits the
     * number of samples to a default of 100000 (cf. {@code DEFAULT_LIMIT_FOR_SUCH_THAT}.
     *
     * @param predicate
     *      samples need to satisfy this predicate, otherwise they are discarded by the
     *      generator
     * @param maxNumberOfSamples
     *      the maximum number of samples generated and tested against the given predicate
     *      until this generator gives up
     * @throws IllegalStateException
     *      in case the maximum number of samples have been reached and no sample was found
     *      that satisfied the predicate
     * @return
     *      a new generator that discards samples if they do not satisfy the given predicate
     */
    public Gen<T> suchThat(final Predicate<? super T> predicate, final int maxNumberOfSamples) {
        final Function<Random, T> suchThatFn = r -> Stream.iterate(this, t -> t)
                .limit(maxNumberOfSamples)
                .map(Gen::sample)
                .filter(predicate)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tried " + maxNumberOfSamples + " sample(s), but was unable to find one that satisfies the given predicate."));
        return new Gen<>(suchThatFn, sourceOfRandomness);
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
                .map(n -> start + (n % (stopExclusive - start)));
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
                .map(n -> n % 2 != 0 ? n + 1 : n);
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
                .map(n -> n % 2 == 0 ? n + 1 : n);
    }

    /**
     * Constructs a generator that generates {@link java.lang.Double} values that are within the
     * interval [0.0; 1.0).
     *
     * @return
     *      a {@code Gen}erator that generates {@link java.lang.Double}s within the interval [0.0; 1.0)
     */
    public static Gen<Double> normalizedDouble() {
        return normalizedDouble(new Random());
    }

    /**
     * Constructs a generator that generates {@link java.lang.Double} values that are within the
     * interval [0.0; 1.0).
     *
     * Uses the given instance of {@link java.util.Random} as source of randomness.
     *
     * @param sourceOfRandomness
     *      uses the given instance of {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that generates {@link java.lang.Double}s within the interval [0.0; 1.0)
     */
    public static Gen<Double> normalizedDouble(final Random sourceOfRandomness) {
        return nonNegativeInteger(sourceOfRandomness)
                .map(n -> n / ((double) Integer.MAX_VALUE + 1));
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
                .map(d -> start + d * (stopExclusive - start));
    }

    /**
     * Constructs a generator that distributes samples between the generators {@code genT1} and {@code genT2}
     * with respect to the given threshold. Each call to {@link Gen#sample()} of this generator produces a
     * variate x within [0.0; 1.0). If x < threshold, {@code genT1} is used to generate a sample, otherwise
     * {@code genT2}.
     *
     * @param threshold
     *      a threshold value between 0.0 (exclusive) and 1.0 (exclusive)
     * @param genT1
     *      the generator to use for sample generation if the variate is smaller than the given threshold
     * @param genT2
     *      the generator to use for sample generation if the variate is larger than or equal to the given threshold
     * @param <T>
     *      parameterized type of values produced by either {@code genT1} and {@code genT2}
     * @return
     *      a {@code Gen}erator that distributes samples between two given generators with respect to a
     *      certain threshold
     */
    public static <T> Gen<T> weighted(final double threshold,
                                      final Gen<T> genT1,
                                      final Gen<T> genT2) {
        return weighted(threshold, genT1, genT2, new Random());
    }

    /**
     * Constructs a generator that distributes samples between the generators {@code genT1} and {@code genT2}
     * with respect to the given threshold. Each call to {@link Gen#sample()} of this generator produces a
     * variate x within [0.0; 1.0). If x < threshold, {@code genT1} is used to generate a sample, otherwise
     * {@code genT2}.
     *
     * Uses the given instance of {@link java.util.Random} as source of randomness.
     *
     * @param threshold
     *      a threshold value between 0.0 (exclusive) and 1.0 (exclusive)
     * @param genT1
     *      the generator to use for sample generation if the variate is smaller than the given threshold
     * @param genT2
     *      the generator to use for sample generation if the variate is larger than or equal to the given threshold
     * @param sourceOfRandomness
     *      uses the given instance of {@link java.util.Random} as source of randomness
     * @param <T>
     *      parameterized type of values produced by either {@code genT1} and {@code genT2}
     * @return
     *      a {@code Gen}erator that distributes samples between two given generators with respect to a
     *      certain threshold
     */
    public static <T> Gen<T> weighted(final double threshold,
                                      final Gen<T> genT1,
                                      final Gen<T> genT2,
                                      final Random sourceOfRandomness) {
        if (threshold <= 0.0 || threshold >= 1.0) throw new IllegalArgumentException("threshold of weighted generator must be within (0.0; 1.0)");
        return normalizedDouble(sourceOfRandomness)
                .flatMap(probability -> probability < threshold ? genT1 : genT2);
    }

    private static final String NUMERICAL_ALPHABET = "0123456789";

    private static final String ALPHANUMERICAL_ALPHABET =
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase() +
                    NUMERICAL_ALPHABET;

    /**
     * Constructs a generator that produces a {@link java.lang.String} of the requested {@code length}.
     * The {@link java.lang.String} comprises characters from the ASCII alphabet, with the restriction
     * that the characters must be printable (and therefore are in decimal range 32 to 126).
     *
     * Uses the given instance of {@link java.util.Random} as source of randomness.
     *
     * @param length
     *      the length of generated ASCII strings
     * @return
     *      a {@code Gen}erator that produces {@link java.lang.String}s of {@code length}, where each
     *      {@link java.lang.String} is comprised of printable ASCII characters
     */
    public static Gen<String> asciiString(final int length) {
        return asciiString(length, new Random());
    }

    /**
     * Constructs a generator that produces a {@link java.lang.String} of the requested {@code length}.
     * The {@link java.lang.String} comprises characters from the ASCII alphabet, with the restriction
     * that the characters must be printable (and therefore are in decimal range 32 to 126).
     *
     * Uses the given instance of {@link java.util.Random} as source of randomness.
     *
     * @param length
     *      the length of generated ASCII strings
     * @param sourceOfRandomness
     *      uses the given instance of {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that produces {@link java.lang.String}s of {@code length}, where each
     *      {@link java.lang.String} is comprised of printable ASCII characters
     */
    public static Gen<String> asciiString(final int length,
                                          final Random sourceOfRandomness) {
        if (length < 0) throw new IllegalArgumentException("The requested length of generated strings cannot be negative.");
        return listOfN(choose(32, 127, sourceOfRandomness), length)
                .map(list -> list
                        .stream()
                        .map(Character::toChars)
                        .map(String::valueOf)
                        .reduce("", String::concat, String::concat));
    }

    /**
     * Constructs a generator that produces {@link java.lang.String}s of length {@code lenght} using an
     * alphanumerical alphabet.
     *
     * @param length
     *      the length of the generated alphanumerical strings
     * @return
     *      a {@code Gen}erator that produces {@link java.lang.String}s of length {@code length} using
     *      an alphanumerical alphabet
     */
    public static Gen<String> alphaNumString(final int length) {
        return alphaNumString(length, new Random());
    }

    /**
     * Constructs a generator that produces {@link java.lang.String}s of length {@code lenght} using an
     * alphanumerical alphabet.
     *
     * @param length
     *      the length of the generated alphanumerical strings
     * @param sourceOfRandomness
     *      uses the given instance {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that produces {@link java.lang.String}s of length {@code length} using
     *      an alphanumerical alphabet
     */
    public static Gen<String> alphaNumString(final int length,
                                             final Random sourceOfRandomness) {
        return fromAlphabetString(length, ALPHANUMERICAL_ALPHABET, sourceOfRandomness);
    }

    /**
     * Constructs a generator that produces {@link java.lang.String}s of the provided {@code length} using a
     * numerical alphabet.
     *
     * @param length
     *      the length of the generated numerical strings
     * @return
     *      a {@code Gen}erator that produces {@link java.lang.String}s of length {@code length} using
     *      a numerical alphabet
     */
    public static Gen<String> numString(final int length) {
        return numString(length, new Random());
    }

    /**
     * Constructs a generator that produces {@link java.lang.String}s of the provided {@code length} using a
     * numerical alphabet.
     *
     * @param length
     *      the length of the generated numerical strings
     * @param sourceOfRandomness
     *      uses the given instance of {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that produces {@link java.lang.String}s of length {@code length} using
     *      a numerical alphabet
     */
    public static Gen<String> numString(final int length,
                                        final Random sourceOfRandomness) {
        return fromAlphabetString(length, NUMERICAL_ALPHABET, sourceOfRandomness);
    }

    /**
     * Constructs a generator that produces {@link java.lang.String}s of the provided {@code length} using the
     * given {@code alphabet}.
     *
     * Uses the given instance of {@link java.util.Random} as source of randomness.
     *
     * @param length
     *      the length of the generated strings
     * @param alphabet
     *      the alphabet used to generate strings
     * @return
     *      a {@code Gen}erator that produces {@link java.lang.String}s of {@code length}, where each
     *      {@link java.lang.String} is comprised of characters from the given alphabet
     */
    public static Gen<String> fromAlphabetString(final int length,
                                                 final String alphabet) {
        return fromAlphabetString(length, alphabet, new Random());
    }

    /**
     * Constructs a generator that produces {@link java.lang.String}s of the provided {@code length} using the
     * given {@code alphabet}.
     *
     * Uses the given instance of {@link java.util.Random} as source of randomness.
     *
     * @param length
     *      the length of the generated strings
     * @param alphabet
     *      the alphabet used to generate strings
     * @param sourceOfRandomness
     *      uses the given instance of {@link java.util.Random} as source of randomness
     * @return
     *      a {@code Gen}erator that produces {@link java.lang.String}s of {@code length}, where each
     *      {@link java.lang.String} is comprised of characters from the given alphabet
     */
    public static Gen<String> fromAlphabetString(final int length,
                                                 final String alphabet,
                                                 final Random sourceOfRandomness) {
        if (length < 0) throw new IllegalArgumentException("The requested length of generated strings cannot be negative.");
        if (alphabet == null || alphabet.isEmpty()) throw new IllegalArgumentException("The given alphabet may not be null or empty.");
        return listOfN(choose(0, alphabet.length(), sourceOfRandomness), length)
                .map(list -> list
                        .stream()
                        .map(alphabet::charAt)
                        .map(String::valueOf)
                        .reduce("", String::concat, String::concat));
    }
}
