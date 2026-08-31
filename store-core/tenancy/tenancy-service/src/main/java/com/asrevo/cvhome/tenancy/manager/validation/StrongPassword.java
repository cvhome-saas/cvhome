package com.asrevo.cvhome.tenancy.manager.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * The password is neither one of the passwords everybody tries first nor the name of the account it protects.
 *
 * <p>
 * This account owns an organization and, until a password reset flow exists, is unrecoverable: there is no
 * administrator above the first one. The two rules here are the ones a length minimum does not already cover, and
 * they are deliberately <strong>not</strong> composition rules — see {@code SignUpUser.MIN_PASSWORD_LENGTH}.
 * </p>
 *
 * <p>
 * The console applies the same two rules on the form, and that is not redundancy: the form's job is to say so
 * before the round trip, this one's is to be true for callers that never loaded the form. When one changes, change
 * both — {@code store-core/console-ui/src/app/shared/validators/password-strength.ts} is the other half.
 * </p>
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default "must not be a common password or contain your name or email address";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
