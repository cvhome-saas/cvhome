package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * That address already has an account.
 *
 * <p>
 * The condition was always reachable — it is the single most likely way a signup fails — but it had no name. uaa
 * lets its unique constraint decide, the shared advice renders the database's refusal as
 * {@code COMMON.DATA_INTEGRITY_VIOLATION}, and {@code UaaConflictException} re-emitted that code and its bare 409
 * to the browser. A generic conflict with no {@code fieldErrors[]} is indistinguishable from every other conflict
 * this endpoint can produce, so the console had to <em>guess</em>: {@code AuthFacade.bindTakenEmail} treats any
 * fieldless 409 as a taken address, which was also the answer an over-long address produced, and told people to
 * sign in to an account that did not exist.
 * </p>
 *
 * <p>
 * This is the code and the field error that guess was standing in for. The field path is the console's own
 * ({@code user.emailAddress}), so {@code applyToForm} binds the message to the control that caused it with no
 * special case at all.
 * </p>
 */
public class DuplicateSignupEmailException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The path the sign-up form binds a server error to — its group is nested under {@code user} to match. */
    private static final String EMAIL_FIELD = "user.emailAddress";

    protected DuplicateSignupEmailException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * Deliberately does <strong>not</strong> chain the {@code UaaConflictException} it was raised from.
     *
     * <p>
     * Spring's exception resolver walks the cause chain looking for a handler, and a chained
     * {@code RemoteServiceException} would be rendered with <em>uaa's</em> code — the generic one this type exists
     * to replace — silently discarding the specific error. Same trap {@code DuplicateStoreNameException} documents.
     * The cause is logged at the call site instead, where it is still available for diagnosis.
     * </p>
     */
    public static DuplicateSignupEmailException of(String emailAddress) {
        return new ErrorBuilder<>(TenancyErrors.SIGNUP_EMAIL_TAKEN, DuplicateSignupEmailException::new)
                .detail("An account already exists for %s.", emailAddress)
                .param("emailAddress", emailAddress)
                .fieldError(EMAIL_FIELD, TenancyErrors.SIGNUP_EMAIL_TAKEN, "That email address already has an account.")
                .build();
    }

}
