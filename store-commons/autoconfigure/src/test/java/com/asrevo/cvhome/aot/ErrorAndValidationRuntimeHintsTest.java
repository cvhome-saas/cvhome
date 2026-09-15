package com.asrevo.cvhome.aot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;

import com.asrevo.cvhome.errors.FieldError;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A native service can reject a request: construct our validators, and write the field errors into the body.
 */
class ErrorAndValidationRuntimeHintsTest {

    private static RuntimeHints registered() {
        RuntimeHints hints = new RuntimeHints();
        new ErrorAndValidationRuntimeHints().registerHints(hints, ErrorAndValidationRuntimeHintsTest.class.getClassLoader());
        return hints;
    }

    @Test
    void theFieldErrorRecordInEveryValidationBodyIsBindable() throws NoSuchMethodException {
        // Its components are what Jackson could not read natively.
        assertThat(RuntimeHintsPredicates.reflection()
                .onMethodInvocation(FieldError.class.getMethod(FieldError.class.getRecordComponents()[0].getName())))
                .accepts(registered());
    }

    @Test
    void aValidatorMayBeConstructedAndItsConstraintRead() {
        RuntimeHints hints = registered();

        assertThat(RuntimeHintsPredicates.reflection().onType(StubValidator.class)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection().onType(StubConstraint.class)
                .withMemberCategory(MemberCategory.INVOKE_PUBLIC_METHODS)).accepts(hints);
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(ErrorAndValidationRuntimeHints.class::isInstance);
    }

    /** Stands in for tenancy's @StrongPassword. */
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = StubValidator.class)
    public @interface StubConstraint {

        String message() default "weak";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

    }

    public static class StubValidator implements ConstraintValidator<StubConstraint, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value != null;
        }

    }

}
