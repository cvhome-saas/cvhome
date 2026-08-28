package com.asrevo.cvhome.content.errors;

import java.io.Serial;
import java.util.Collection;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * A well-formed request that a content business rule refuses: an illegal status transition, a publish with the
 * default locale still empty, a full banner placement, a menu nested too deep, an edit to a published policy
 * version. Renders as HTTP 422.
 */
public class ContentRuleException extends OperationNotAllowedException {

    private static final String ID = "id";

    @Serial
    private static final long serialVersionUID = 1L;

    protected ContentRuleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ContentRuleException transitionNotAllowed(Long id, Object from, Object to) {
        return new ErrorBuilder<>(ContentErrors.TRANSITION_NOT_ALLOWED, ContentRuleException::new)
                .detail("Content %s cannot move from %s to %s.", id, from, to)
                .param(ID, id).param("from", from).param("to", to).build();
    }

    public static ContentRuleException publishIncomplete(Long id, Collection<FieldError> fieldErrors) {
        return new ErrorBuilder<>(ContentErrors.PUBLISH_INCOMPLETE, ContentRuleException::new)
                .detail("Content %s cannot be published until the default locale is complete.", id)
                .param(ID, id).fieldErrors(fieldErrors).build();
    }

    public static ContentRuleException bannerCapacity(Object placement, int capacity, Long conflictingId) {
        return new ErrorBuilder<>(ContentErrors.BANNER_CAPACITY_EXCEEDED, ContentRuleException::new)
                .detail("Placement %s already holds its %s live banner(s); %s would overlap.", placement, capacity,
                        conflictingId)
                .param("placement", placement).param("capacity", capacity).param("conflictingId", conflictingId)
                .build();
    }

    public static ContentRuleException menuDepth(Object handle) {
        return new ErrorBuilder<>(ContentErrors.MENU_DEPTH_EXCEEDED, ContentRuleException::new)
                .detail("Menu %s may nest one level deep only.", handle)
                .param("handle", handle).build();
    }

    public static ContentRuleException policyVersionImmutable(Long id, int version) {
        return new ErrorBuilder<>(ContentErrors.POLICY_VERSION_IMMUTABLE, ContentRuleException::new)
                .detail("Version %s of policy %s is published and cannot be edited; create a new version.", version,
                        id)
                .param(ID, id).param("version", version).build();
    }

}
