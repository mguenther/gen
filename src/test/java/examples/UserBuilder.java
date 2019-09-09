package examples;

import net.mguenther.gen.Gen;

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
