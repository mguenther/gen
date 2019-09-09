package examples;

import net.mguenther.gen.Gen;

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
                .thenApply(topLevelDomain -> String.format("%s%s%s@%s.%s", firstName, delimiter, lastName, domainName, topLevelDomain))))));
    }

    public static Gen<User> userGen() {
        return Gen.alphaNumString(8)
                .thenCompose(firstName -> Gen.alphaNumString(8)
                .thenCompose(lastName -> emailGen(Gen.constant(firstName), Gen.constant(lastName))
                .thenCompose(email -> Gen.alphaNumString(14)
                .thenApply(hashedPassword -> new User(firstName + " " + lastName, email, hashedPassword)))));
    }
}
