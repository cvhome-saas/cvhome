package com.asrevo.cvhome.tenancy.manager.validation;

import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.asrevo.cvhome.tenancy.manager.dto.SignUpUser;

/**
 * Compares the two passwords and reports the mismatch on {@code repeatPassword}.
 */
public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, SignUpUser> {

    static final String CONFIRMATION_FIELD = "repeatPassword";

    /**
     * A null or empty password is left alone.
     *
     * <p>
     * {@code @NotBlank} on both fields already says that, and saying it twice puts two errors on one control — the
     * console shows the highest-precedence one, so the visitor would be told to repeat a password they have not
     * typed yet.
     * </p>
     */
    @Override
    public boolean isValid(SignUpUser user, ConstraintValidatorContext context) {
        if (user == null || user.password() == null || user.password().isEmpty()) {
            return true;
        }
        if (Objects.equals(user.password(), user.repeatPassword())) {
            return true;
        }
        Violations.reportOn(context, CONFIRMATION_FIELD);
        return false;
    }

}
