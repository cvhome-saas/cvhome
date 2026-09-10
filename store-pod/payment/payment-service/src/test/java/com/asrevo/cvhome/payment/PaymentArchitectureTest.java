package com.asrevo.cvhome.payment;

import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the payment domain, enforced — the rules live in {@link CvhomeArchitectureRules} — plus the
 * authorization gate: every request handler carries {@code @PreAuthorize} or is named in {@link #ANONYMOUS} with its
 * reason. The rule refuses a {@code /private/} entry and an entry that no longer names a handler.
 *
 * <p>Every private handler is gated: the gateway's initiate and status carry the same-pod token (audit A2), the
 * token echo is gone (A10) and the two enum reads on {@code PaymentConfigurationController} ask for a signed-in
 * principal (A17), so nothing under {@code /private/} needs, or may have, an entry here.</p>
 */
@AnalyzeClasses(packages = PaymentArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class PaymentArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.payment";

    /** The two anonymous surfaces: the storefront's payment-type read and the provider's webhook. */
    static final Set<String> ANONYMOUS = Set.of(
            // storefront lists the enabled payment types of a store before any session exists
            "PublicPaymentConfigurationController#getSupportedPaymentTypes",
            // the provider's webhook; the signature is the credential (A5 moves the check ahead of the outbox)
            "PublicPaymentWebhookApi#webhook");

    @ArchTest
    static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);

    @ArchTest
    static final ArchRule SERVICES_STAY_OFF_THE_WEB = CvhomeArchitectureRules.servicesDoNotDependOnOwnApi(DOMAIN);

    @ArchTest
    static final ArchRule ENTITIES_STAY_BELOW = CvhomeArchitectureRules.entitiesDoNotDependOnServices(DOMAIN);

    /**
     * payment still serves its shopper auth endpoints from {@code controller.v1.auth}; the deviation is declared so no
     * new controller location can appear.
     */
    @ArchTest
    static final ArchRule CONTROLLERS_IN_API =
            CvhomeArchitectureRules.controllersLiveIn(DOMAIN, "com.asrevo.cvhome.payment.controller..");

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    @ArchTest
    static final ArchRule HANDLERS_ARE_GATED_OR_ANONYMOUS =
            CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD, ANONYMOUS);

    @ArchTest
    static final ArchRule ANONYMOUS_LIST_IS_LIVE = CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, ANONYMOUS);

    private PaymentArchitectureTest() {
    }

}
