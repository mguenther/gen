# Gen

[![Build Status](https://travis-ci.org/mguenther/gen.svg?branch=master)](https://travis-ci.org/mguenther/gen.svg)

**Gen** provides the `Gen` monad for Java 8+. Using the `Gen` monad, developers can implement a generative approach for creating test fixtures that helps to write unit and integration tests in a lean and non-obtrusive way.

## Why?

Typically, the `Gen` monad is a concept used in property-based testing libraries, such as ScalaCheck and the like. Property-based tests verify statements about the output of your code base on some given input, where the same statement is verified for many admissible and inadmissible inputs alike. Such tests rely heavily upon randomly generated objects. But even if you do not commit to property-based testing as a concept in your testsuite, having abstractions for randomly generating objects from your domain can simplify testing a lot.

In a typical, Unit-Test based setup, large portions of your tests consist of methods that construct test fixtures. Even if you put in a good amount of work in designing your test fixtures, it is cumbersome to provide the means for parameterization, as composability of test fixtures is an issue. The `Gen` monad is composable by design, making it easy to randomly generate complex objects from a couple of small methods.

Oftentimes you find yourself in need for a test fixture for some class, but the actual test logic only cares about a single parameter that goes into the constructor of that class. You still have to come up with values for the rest of the parameters, even if they provide no value for the test. Not only does this increase the code inside your test case, but it clouds the distinction between parameters that are relevant for that particular test and those that are not. A generator-based approach can help here as well.

## Usage

### Generators for domain classes

Suppose you have a simple domain class `User`.

```java
@Getter
@ToString
@EqualsAndHashCode(of = "email")
@RequiredArgsConstructor
public class User {

  private final String username;
  private final String email;
  private final String hashedPassword;
}
```

A generator `userGen` could be implemented like this.

```java
public class UserGen {

  private static Gen<String> topLevelDomainNameGen() {
    return Gen.oneOf("com", "de", "at", "ch", "ca", "uk", "gov", "edu");
  }

  private static Gen<String> domainNameGen() {
    return Gen.oneOf("mguenther", "google", "spiegel");
  }

  private static Gen<String> emailGen(final Gen<String> firstNameGen, final Gen<String> lastNameGen) {
    return firstNameGen
      .thenCompose(firstName -> Gen.oneOf("-", ".", "_")
      .thenCompose(delimiter -> lastNameGen
      .thenCompose(lastName -> domainNameGen()
      .thenCompose(domainName -> topLevelDomainNameGen()
      .thenApply(topLevelDomain -> String.format("%s%s@%s.%s", firstName, delimiter, lastName, domainName, topLevelDomain))))));
    }

  public static Gen<User> userGen() {
    return Gen.alphaNumString(8)
      .thenCompose(firstName -> Gen.alphaNumString(8)
      .thenCompose(lastName -> emailGen(Gen.constant(firstName), Gen.constant(lastName))
      .thenCompose(email -> Gen.alphaNumString(14)
      .thenApply(hashedPassword -> new User(firstName + " " + lastName, email, hashedPassword)))));
  }
}
```

### Randomized builders for domain classes

Another approach that I use quite often in my projects is the integration of the `Gen` monad with the builder pattern. Have a look at the following example.

```java
public class UserBuilder {

  private String username;
  
  private String email = Gen.alphaNumString(8)
    .thenCompose(firstName -> Gen.alphaNumString(8)
    .thenCompose(lastName -> Gen.oneOf("-", ".", "_")
    .thenCompose(delimiter -> Gen.oneOf("com", "de", "at", "ch", "ca", "uk", "gov", "edu")
    .thenCompose(domainName -> Gen.oneOf("mguenther", "google", "spiegel")
    .thenApply(topLevelDomain -> String.format("%s%s%s@%s.%s", firstName, delimiter, lastName, domainName, topLevelDomain))))))
    .sample();
  
  private String hashedPassword = Gen.alphaNumString(14).sample();

  public UserBuilder withUsername(final String username) {
    this.username = username;
    return this;
  }

  public UserBuilder withEmail(final String email) {
    this.email = email;
    return this;
  }

  public UserBuilder withHashedPassword(final String hashedPassword) {
    this.hashedPassword = hashedPassword;
    return this; 
  }

  public User build() {
    return new User(username, email, hashedPassword);
  }

  public UserBuilder randomizeUser() {
    return new UserBuilder();
  }

  public User randomizedUser() {
    return randomizeUser().build();
  }
}
```

Using a `Gen`-enabled builder let's you focus on the actual testing logic in your unit test. You simply override the attribute that contributes to the testcase using the builder pattern, but rely on `Gen` to provide randomized values from your domain for the rest of the member variables.

Of course, you can implement the exact same behavior if you parameterize individual generators like we did in section *Generators for domain classes*. But this might get tedious quickly due to Java's verbosity and lack of default method parameters.

### Source of randomness

The `Gen` monad uses `java.util.Random` as source of randomness. All factory methods of the `Gen` monad provide an overloaded method that accepts an instance of `java.util.Random` as parameter. This is advisable as you build more complex generators and use the generators for regression tests: By looking at failed unit tests and checking their random seed, you can easily reproduce the error situation. Simply add a unit test that applies the same random seed to the generator in order to produce the same object that led to the error in the first place.

In the following example, we retain the source of randomness for all individual generators that contribute to the complex generator `userGen` from our first example.

```java
public class UserGen {
  public static Gen<User> userGen(final Random sourceOfRandomness) {
    return Gen.alphaNumString(8, sourceOfRandomness)
      .thenCompose((r1, firstName) -> Gen.alphaNumString(8, r1)
      .thenCompose((r2, lastName) -> emailGen(Gen.constant(firstName, r2), Gen.constant(lastName, r2))
      .thenCompose((r3, email) -> Gen.alphaNumString(14, r3)
      .thenApply(hashedPassword -> new User(firstName + " " + lastName, email, hashedPassword)))));
  }
    
  public static Gen<String> emailGen(final Gen<String> firstNameGen, final Gen<String> lastNameGen) {
    return firstNameGen
      .thenCompose((r1, firstName) -> Gen.oneOf("-", ".", "_", r1)
      .thenCompose((r2, delimiter) -> lastNameGen
      .thenCompose((r3, lastName) -> domainNameGen(r3)
      .thenCompose((r4, domainName) -> topLevelDomainNameGen(r4)
      .thenApply(topLevelDomain -> String.format("%s%s%s@%s.%s", firstName, delimiter, lastName, domainName, topLevelDomain))))));
  }
    
  private static Gen<String> domainNameGen(final Random sourceOfRandomness) {
    return Gen.oneOf(Arrays.asList("mguenther", "google", "spiegel"), sourceOfRandomness);
  }

  private static Gen<String> topLevelDomainNameGen(final Random sourceOfRandomness) {
    return Gen.oneOf(Arrays.asList("com", "de", "at", "ch", "ca", "uk", "gov", "edu"), sourceOfRandomness);
  }
}
```

### Combinators

The `Gen` monad in its current state offers two combinators `thenApply` and `thenCompose`. In functional programming lingo `thenApply` would be your `map`, while `thenCompose` would be your `flatMap`.

## License

This work is released under the terms of the Apache 2.0 license.

<p>
    <div align="center">
        <div><img src="made-in-darmstadt.jpg"></div>
        <div><a href="https://mguenther.net">mguenther.net</a></div>
    </div>
</p>