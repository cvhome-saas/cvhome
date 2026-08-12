package com.asrevo.cvhome.billing.api.v1;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.dto.CheckoutSessionView;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.ImmediateCancelForbiddenException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionChangeRejectedException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.service.SubscriptionService;
import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder;

import lombok.RequiredArgsConstructor;

/**
 * A store's own subscription.
 *
 * <p>
 * Store-core convention, not the pod one: the store arrives as an explicit {@code store} parameter typed
 * {@link StoreMerchantId}, and every method is gated on it. The permission evaluator denies by default, so a token
 * with no {@code case} behind it 403s silently — which is why {@code STORE-CORE.BILLING.*} exists in both
 * {@code CustomPermissionEvaluator} and {@code PermissionAccessChecker}.
 * </p>
 *
 * <p>
 * Reading and managing are separate tokens on purpose. A store moderator should be able to see the plan they work
 * under; spending the org's money is a different act, and belongs to whoever owns the card.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionApi {

    /**
     * Where Stripe returns a paying customer. Both are pages of the seller console, which already exist for
     * tenancy's checkout, so the flow does not need a second pair.
     */
    private static final String SUCCESS_PATH = "/public/subscription/success";

    private static final String CANCEL_PATH = "/public/subscription/fail";

    private static final String SELLER_UI = "seller-ui";

    private final SubscriptionService subscriptionService;

    private final ServiceDomainProperties serviceDomainProperties;

    /**
     * What the store is on, when it renews, and anything already scheduled to change.
     *
     * @throws SubscriptionNotFoundException billing has never seen this store, or it is not this caller's to see
     */
    @GetMapping("current")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-CORE.BILLING.READ')")
    public SubscriptionView current(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                    @RequestParam("store") StoreMerchantId store)
            throws SubscriptionNotFoundException {
        return subscriptionService.current(store, tenantScopeOf(identity));
    }

    /**
     * Opens a Stripe checkout and returns where to send the customer.
     *
     * <p>
     * Answers 200 with the URL in a body, rather than the 302 tenancy's equivalent returns. A single-page
     * console has to open the URL itself, and a browser will not usefully follow a redirect out of an XHR; a body
     * also makes the endpoint assertable from a {@code .http} file.
     * </p>
     *
     * <p>
     * Gated on {@code MANAGE}, not {@code READ}: this is the endpoint that leads to a charge.
     * </p>
     */
    @PostMapping("checkout")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-CORE.BILLING.MANAGE')")
    public CheckoutSessionView checkout(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                        @RequestParam("store") StoreMerchantId store,
                                        @RequestBody CheckoutRequest request,
                                        HttpServletRequest httpRequest)
            throws SubscriptionNotFoundException, PlanPriceNotFoundException, SubscriptionChangeRejectedException,
            BillingProviderUnavailableException {
        RedirectionUrlBuilder urlBuilder = sellerConsoleUrls(httpRequest);
        return subscriptionService.checkout(store, tenantScopeOf(identity), request.planPriceId(),
                urlBuilder.getRedirectionUrl(SUCCESS_PATH), urlBuilder.getRedirectionUrl(CANCEL_PATH));
    }

    /**
     * Moves the store to another plan.
     *
     * <p>
     * One endpoint for both directions, because the direction is not the caller's to choose — see
     * {@link com.asrevo.cvhome.billing.service.SubscriptionService#changePlan}. The response says what actually
     * happened: an upgrade comes back already on the new plan, a downgrade comes back with
     * {@code pendingPlanChange} set and the old plan still in force.
     * </p>
     */
    @PostMapping("plan")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-CORE.BILLING.MANAGE')")
    public SubscriptionView changePlan(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                       @RequestParam("store") StoreMerchantId store,
                                       @RequestBody PlanChangeRequest request)
            throws SubscriptionNotFoundException, PlanPriceNotFoundException, SubscriptionChangeRejectedException,
            BillingProviderUnavailableException, IllegalSubscriptionTransitionException {
        return subscriptionService.changePlan(store, tenantScopeOf(identity), request.planPriceId());
    }

    /**
     * Switches renewal off, or — for an administrator — ends the subscription now.
     */
    @PostMapping("cancel")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-CORE.BILLING.MANAGE')")
    public SubscriptionView cancel(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                   @RequestParam("store") StoreMerchantId store,
                                   @RequestBody CancelRequest request)
            throws SubscriptionNotFoundException, BillingProviderUnavailableException,
            IllegalSubscriptionTransitionException, ImmediateCancelForbiddenException {
        return subscriptionService.cancel(store, tenantScopeOf(identity), request.immediate(),
                identity.isSuperAdmin());
    }

    /**
     * Switches renewal back on, and calls off a scheduled downgrade if one is pending.
     */
    @PostMapping("resume")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-CORE.BILLING.MANAGE')")
    public SubscriptionView resume(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                   @RequestParam("store") StoreMerchantId store)
            throws SubscriptionNotFoundException, BillingProviderUnavailableException,
            IllegalSubscriptionTransitionException {
        return subscriptionService.resume(store, tenantScopeOf(identity));
    }

    /**
     * Where Stripe returns the customer to, built from the configured seller-console domain rather than from
     * anything the request carries — a caller-supplied return URL would be an open redirect out of a payment flow.
     *
     * <p>
     * The scheme and port come from the forwarding headers so the URL is right behind the gateway, which terminates
     * on a different port than the console is served from.
     * </p>
     */
    private RedirectionUrlBuilder sellerConsoleUrls(HttpServletRequest request) {
        String scheme = Optional.ofNullable(request.getHeader(RedirectionUrlBuilder.SCHEMA_HEADER_KEY))
                .orElse(request.getScheme());
        int port = Optional.ofNullable(request.getHeader(RedirectionUrlBuilder.PORT_HEADER_KEY))
                .map(Integer::parseInt)
                .orElse(request.getServerPort());
        return new RedirectionUrlBuilder(scheme, port, serviceDomainProperties.getService(SELLER_UI));
    }

    /**
     * The org a read must be confined to, or {@code null} for a caller entitled to span orgs.
     *
     * <p>
     * Belt and braces on purpose. {@code @PreAuthorize} is the first gate, but the shared
     * {@code StoreRoleAccessChecker} cannot currently tell which org a store belongs to — it carries a {@code TODO}
     * saying so and returns true for any store once the caller holds the org-admin role. Billing does know, because
     * every subscription row records its org, so it narrows the query rather than relying on that check alone. Remove
     * this and one org's admin can read another org's spend.
     * </p>
     */
    private ManagerOrgId tenantScopeOf(UserOrgStoreIdentity identity) {
        return identity.isSuperAdmin() ? null : identity.org();
    }

}
