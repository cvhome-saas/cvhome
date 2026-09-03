package com.asrevo.cvhome.sso.password;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyValidatorTest {

    private static final RealmSettings.PasswordPolicy STRICT =
            new RealmSettings.PasswordPolicy(12, true, true, true, true, 5, 0, false);

    private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

    private static User user() {
        User user = new User();
        user.setUsername("org1-admin");
        user.setEmail("jordan.diaz@example.com");
        return user;
    }

    @Test
    void everyBrokenRuleIsReportedAtOnce() {
        assertThatThrownBy(() -> validator.validate("short", user(), STRICT))
                .isInstanceOf(PasswordPolicyViolationException.class)
                .hasMessageContaining(PasswordPolicyValidator.MIN_LENGTH)
                .hasMessageContaining(PasswordPolicyValidator.UPPER)
                .hasMessageContaining(PasswordPolicyValidator.DIGIT)
                .hasMessageContaining(PasswordPolicyValidator.SPECIAL);
    }

    @Test
    void theUsernameAndTheEmailLocalPartAreRefusedInside() {
        assertThatThrownBy(() -> validator.validate("Org1-Admin-2026!", user(), STRICT))
                .hasMessageContaining(PasswordPolicyValidator.NOT_USERNAME);
        assertThatThrownBy(() -> validator.validate("Jordan.diaz-2026!", user(), STRICT))
                .hasMessageContaining(PasswordPolicyValidator.NOT_EMAIL);
    }

    @Test
    void aCompliantPasswordPasses() {
        assertThatCode(() -> validator.validate("Correct-Horse-9!", user(), STRICT)).doesNotThrowAnyException();
    }

    @Test
    void relaxedPolicyAsksLess() {
        RealmSettings.PasswordPolicy relaxed = new RealmSettings.PasswordPolicy(8, false, false, false, false, 0, 0, false);

        assertThatCode(() -> validator.validate("abcdefgh", user(), relaxed)).doesNotThrowAnyException();
    }

}
