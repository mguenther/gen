package examples;

import net.mguenther.gen.Gen;

import java.util.Arrays;
import java.util.Random;

public class UserGenR {

    private static Gen<String> topLevelDomainNameGen(final Random sourceOfRandomness) {
        return Gen.oneOf(Arrays.asList("com", "de", "at", "ch", "ca", "uk", "gov", "edu"), sourceOfRandomness);
    }

    private static Gen<String> domainNameGen(final Random sourceOfRandomness) {
        return Gen.oneOf(Arrays.asList("mguenther", "google", "spiegel"), sourceOfRandomness);
    }

    public static Gen<String> emailGen(final Gen<String> firstNameGen, final Gen<String> lastNameGen) {
        return firstNameGen
                .thenCompose((r1, firstName) -> Gen.oneOf("-", ".", "_", r1)
                .thenCompose((r2, delimiter) -> lastNameGen
                .thenCompose((r3, lastName) -> domainNameGen(r3)
                .thenCompose((r4, domainName) -> topLevelDomainNameGen(r4)
                .thenApply(topLevelDomain -> String.format("%s%s%s@%s.%s", firstName, delimiter, lastName, domainName, topLevelDomain))))));
    }

    public static Gen<User> userGen(final Random sourceOfRandomness) {
        return Gen.alphaNumString(8, sourceOfRandomness)
                .thenCompose((r1, firstName) -> Gen.alphaNumString(8, r1)
                .thenCompose((r2, lastName) -> emailGen(Gen.constant(firstName, r2), Gen.constant(lastName, r2))
                .thenCompose((r3, email) -> Gen.alphaNumString(14, r3)
                .thenApply(hashedPassword -> new User(firstName + " " + lastName, email, hashedPassword)))));
    }
}
