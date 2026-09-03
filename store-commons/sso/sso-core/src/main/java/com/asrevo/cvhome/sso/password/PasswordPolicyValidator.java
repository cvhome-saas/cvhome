package com.asrevo.cvhome.sso.password;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;

/**
 * The realm's password rules, applied to a candidate. Every broken rule is reported, not just the first, so a
 * person fixing a password is told everything at once.
 */
@Component
public class PasswordPolicyValidator {

    static final String MIN_LENGTH = "minLength";

    static final String UPPER = "upper";

    static final String LOWER = "lower";

    static final String DIGIT = "digit";

    static final String SPECIAL = "special";

    static final String NOT_USERNAME = "notUsername";

    static final String NOT_EMAIL = "notEmail";

    private static final int MIN_LOCAL_PART = 4;

    public void validate(String raw, User target, RealmSettings.PasswordPolicy policy)
            throws PasswordPolicyViolationException {
        String candidate = raw == null ? "" : raw;
        List<String> broken = new ArrayList<>();
        broken.addAll(shapeRules(candidate, policy));
        broken.addAll(identityRules(candidate, target));
        if (!broken.isEmpty()) {
            throw PasswordPolicyViolationException.of(broken);
        }
    }

    private static List<String> shapeRules(String candidate, RealmSettings.PasswordPolicy policy) {
        List<String> broken = new ArrayList<>();
        if (candidate.length() < policy.minLength()) {
            broken.add(MIN_LENGTH);
        }
        if (policy.requireUpper() && candidate.chars().noneMatch(Character::isUpperCase)) {
            broken.add(UPPER);
        }
        if (policy.requireLower() && candidate.chars().noneMatch(Character::isLowerCase)) {
            broken.add(LOWER);
        }
        if (policy.requireDigit() && candidate.chars().noneMatch(Character::isDigit)) {
            broken.add(DIGIT);
        }
        if (policy.requireSpecial() && candidate.chars().allMatch(Character::isLetterOrDigit)) {
            broken.add(SPECIAL);
        }
        return broken;
    }

    /** A password must not contain the account's own name or the local part of its email. */
    private static List<String> identityRules(String candidate, User target) {
        List<String> broken = new ArrayList<>();
        if (target == null) {
            return broken;
        }
        String lower = candidate.toLowerCase(Locale.ROOT);
        if (target.getUsername() != null && lower.contains(target.getUsername().toLowerCase(Locale.ROOT))) {
            broken.add(NOT_USERNAME);
        }
        if (target.getEmail() != null) {
            String local = target.getEmail().split("@")[0].toLowerCase(Locale.ROOT);
            if (local.length() >= MIN_LOCAL_PART && lower.contains(local)) {
                broken.add(NOT_EMAIL);
            }
        }
        return broken;
    }

}
