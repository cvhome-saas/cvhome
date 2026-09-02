package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** The password breaks one or more rules of the realm's policy. Each rule is a field error on {@code password}. */
public class PasswordPolicyViolationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FIELD = "password";

    protected PasswordPolicyViolationException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param rules the rule keys broken, e.g. {@code minLength}, {@code upper}, {@code digit}
     */
    public static PasswordPolicyViolationException of(List<String> rules) {
        ErrorBuilder<PasswordPolicyViolationException> builder =
                new ErrorBuilder<>(UaaErrors.PASSWORD_POLICY_VIOLATION, PasswordPolicyViolationException::new)
                        .detail("The password does not meet the policy: %s.", String.join(", ", rules))
                        .param("rules", rules);
        for (String rule : rules) {
            builder.fieldError(FIELD, UaaErrors.PASSWORD_POLICY_VIOLATION, rule);
        }
        return builder.build();
    }

}
