package com.asrevo.cvhome.checkout;

import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the checkout domain, enforced — all five rules, no declared deviation. The previous checkout
 * needed two; the rewrite was the chance to clear them.
 *
 * <p>Plus the authorization gate: every request handler carries {@code @PreAuthorize} or is named in
 * {@link #ANONYMOUS}, the storefront's guest surface. Adding an anonymous endpoint means adding it here with its
 * reason; the rule refuses a {@code /private/} entry and an entry that no longer names a handler.
 */
@AnalyzeClasses(packages = "com.asrevo.cvhome.checkout", importOptions = ImportOption.DoNotIncludeTests.class)
final class CheckoutArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.checkout";

    /**
     * The guest storefront, which calls with no token. Carts: the code is a UUID and possession is the credential.
     * Checkout: the service answers {@code CHECKOUT.ORDER.LOGIN_REQUIRED} itself when the store demands a session.
     * Status: a guest presents the order reference (A3). Countries: reference data for the address form.
     */
    static final Set<String> ANONYMOUS = Set.of(
            "CartApi#create", "CartApi#upsert", "CartApi#get", "CartApi#removeLine",
            "CheckoutApi#checkout", "CheckoutApi#status",
            "CountryApi#countries");

    @ArchTest
    static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);

    @ArchTest
    static final ArchRule SERVICES_STAY_OFF_THE_WEB = CvhomeArchitectureRules.servicesDoNotDependOnOwnApi(DOMAIN);

    @ArchTest
    static final ArchRule ENTITIES_STAY_BELOW = CvhomeArchitectureRules.entitiesDoNotDependOnServices(DOMAIN);

    @ArchTest
    static final ArchRule CONTROLLERS_IN_API = CvhomeArchitectureRules.controllersLiveInApi(DOMAIN);

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    @ArchTest
    static final ArchRule HANDLERS_ARE_GATED_OR_ANONYMOUS =
            CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD, ANONYMOUS);

    @ArchTest
    static final ArchRule ANONYMOUS_LIST_IS_LIVE = CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, ANONYMOUS);

    private CheckoutArchitectureTest() {
    }

}
