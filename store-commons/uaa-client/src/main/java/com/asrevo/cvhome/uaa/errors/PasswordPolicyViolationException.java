package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.errors.ValidationException;

/** The password breaks one or more rules of the realm's policy. Each rule is a field error on {@code password}. */
public class PasswordPolicyViolationException extends ValidationException {

    /** The field error's parameter naming the broken rule, so a client can translate it rather than print the key. */
    public static final String RULE = "rule";

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FIELD = "password";

    protected PasswordPolicyViolationException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param rules the rule keys broken, e.g. {@code minLength}, {@code upper}, {@code digit}. Each becomes a field
     *              error on {@code password} whose {@code params.rule} is the key and whose message is the key as a
     *              fallback — the key is the contract, the sentence is the client's.
     */
    public static PasswordPolicyViolationException of(List<String> rules) {
        ErrorBuilder<PasswordPolicyViolationException> builder =
                new ErrorBuilder<>(UaaErrors.PASSWORD_POLICY_VIOLATION, PasswordPolicyViolationException::new)
                        .detail("The password does not meet the policy: %s.", String.join(", ", rules))
                        .param("rules", rules);
        for (String rule : rules) {
            builder.fieldError(new FieldError(FIELD, UaaErrors.PASSWORD_POLICY_VIOLATION.code(), rule,
                    Map.of(RULE, rule)));
        }
        return builder.build();
    }

}
