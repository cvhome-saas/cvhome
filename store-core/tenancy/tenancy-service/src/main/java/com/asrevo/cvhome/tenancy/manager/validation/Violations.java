package com.asrevo.cvhome.tenancy.manager.validation;

import jakarta.validation.ConstraintValidatorContext;

/**
 * Puts a class-level constraint's violation on the field the visitor has to change.
 *
 * <p>
 * A class-level constraint reports against the object by default, so its {@code fieldErrors[]} entry arrives with an
 * empty path and the console has no control to bind it to — it becomes a toast saying something is wrong with a form
 * where every field looks fine. Redirecting the violation to a property node is what makes
 * {@code applyFieldErrors} able to place it, and it is two lines that are easy to forget, so both password
 * constraints share them here.
 * </p>
 */
final class Violations {

    private Violations() {
    }

    /**
     * Re-anchors the default message on {@code property}, and disables the object-level violation that would
     * otherwise be reported alongside it.
     */
    static void reportOn(ConstraintValidatorContext context, String property) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(property)
                .addConstraintViolation();
    }

}
