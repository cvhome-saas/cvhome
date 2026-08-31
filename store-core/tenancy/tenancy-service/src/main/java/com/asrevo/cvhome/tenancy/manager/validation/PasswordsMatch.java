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
 * The two passwords on a signup are the same.
 *
 * <p>
 * The confirmation field existed on the wire and was read by nothing: a body with {@code password: "a"} and
 * {@code repeatPassword: "b"} was accepted, and the account was created with the first of the two. The form
 * compared them, so the only visitor this could reach was one not using the form — which is precisely the caller
 * a public endpoint has to assume.
 * </p>
 *
 * <p>
 * Class-level, because it needs two fields; the violation is reported on {@code repeatPassword}, which is the field
 * whose value should change.
 * </p>
 */
@Documented
@Constraint(validatedBy = PasswordsMatchValidator.class)
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordsMatch {

    String message() default "must repeat the password exactly";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
