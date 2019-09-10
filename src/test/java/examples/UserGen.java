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
                .flatMap(firstName -> Gen.oneOf("-", ".", "_")
                .flatMap(delimiter -> lastNameGen
                .flatMap(lastName -> domainNameGen()
                .flatMap(domainName -> topLevelDomainNameGen()
                .map(topLevelDomain -> String.format("%s%s%s@%s.%s", firstName, delimiter, lastName, domainName, topLevelDomain))))));
    }

    public static Gen<User> userGen() {
        return Gen.alphaNumString(8)
                .flatMap(firstName -> Gen.alphaNumString(8)
                .flatMap(lastName -> emailGen(Gen.constant(firstName), Gen.constant(lastName))
                .flatMap(email -> Gen.alphaNumString(14)
                .map(hashedPassword -> new User(firstName + " " + lastName, email, hashedPassword)))));
    }
}
