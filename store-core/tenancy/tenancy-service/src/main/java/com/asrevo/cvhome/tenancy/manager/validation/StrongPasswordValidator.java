package com.asrevo.cvhome.tenancy.manager.validation;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.asrevo.cvhome.tenancy.manager.dto.SignUpUser;

/**
 * The two password rules that need something other than a length: a screen, and a look at the rest of the form.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, SignUpUser> {

    /**
     * The property a violation is reported on — and, doing double duty below, the commonest password there is.
     * One constant rather than two identical literals, which checkstyle refuses.
     */
    static final String PASSWORD = "password";

    /**
     * The passwords a credential-stuffing list opens with, filtered to those a length minimum does not already stop.
     *
     * <p>
     * <strong>A floor, not a screen.</strong> Twenty-odd entries catch the handful that dominate every breach corpus
     * and nothing else; {@code Passw0rd2024} sails through. A real screen is a check against a breached-password
     * corpus, which the platform has no service for. Kept short on purpose so it stays honest about what it is, and
     * kept identical to the console's list so the two cannot disagree about the same password.
     * </p>
     */
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "12345678", "123456789", "1234567890", "123123123", "11111111", "00000000",
            PASSWORD, "password1", "password123", "passw0rd",
            "qwertyuiop", "qwerty123", "abc12345", "iloveyou",
            "sunshine", "princess", "football", "baseball",
            "welcome1", "admin123", "letmein1", "monkey123");

    /**
     * The shortest personal token worth matching.
     *
     * <p>
     * Three would flag {@code Ann} and {@code Ada} inside any password containing those letters in a row —
     * {@code bandana} fails for someone named Ana — which is a rule people work around by adding a character rather
     * than by choosing a better password. Four is where the match starts meaning something.
     * </p>
     */
    private static final int MIN_PERSONAL_TOKEN = 4;

    @Override
    public boolean isValid(SignUpUser user, ConstraintValidatorContext context) {
        if (user == null || user.password() == null || user.password().isEmpty()) {
            return true;
        }

        String password = user.password().toLowerCase(Locale.ROOT);
        if (COMMON_PASSWORDS.contains(password) || personalTokens(user).stream().anyMatch(password::contains)) {
            Violations.reportOn(context, PASSWORD);
            return false;
        }
        return true;
    }

    /**
     * The name and address fragments a password must not contain.
     *
     * <p>
     * {@code ada.lovelace2024} is a password whose owner has just typed the rest of it into the same request, and it
     * is the most common shape of weak password a length rule accepts. Only the address's local part is used: every
     * account at one provider shares its domain, so {@code gmail} would fail half the passwords on the platform for
     * saying nothing about this account.
     * </p>
     */
    private static List<String> personalTokens(SignUpUser user) {
        return Stream.of(user.firstName(), user.lastName(), localPart(user.emailAddress()))
                .filter(Objects::nonNull)
                .map(value -> value.strip().toLowerCase(Locale.ROOT))
                .filter(value -> value.length() >= MIN_PERSONAL_TOKEN)
                .toList();
    }

    private static String localPart(String email) {
        return email == null ? null : email.split("@")[0];
    }

}
