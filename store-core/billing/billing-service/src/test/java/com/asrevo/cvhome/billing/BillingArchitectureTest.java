package com.asrevo.cvhome.billing;

import java.util.HashSet;
import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the billing domain, enforced — the rules live in {@link CvhomeArchitectureRules} — plus the
 * authorization gate: every request handler carries {@code @PreAuthorize} or is named in {@link #ANONYMOUS} (under
 * {@code /public/}) or {@link #AUTHENTICATED_ONLY} (a bare session is the gate) with its reason. The rule refuses an
 * anonymous entry outside {@code /public/} and an entry that no longer names a handler.
 */
@AnalyzeClasses(packages = BillingArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class BillingArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.billing";

    /** The plan catalogue the sign-up page shows, and Stripe's webhook, whose signature is the credential. */
    static final Set<String> ANONYMOUS = Set.of(
            // sign-up and pricing pages list the plans before any account exists
            "PlanCatalogApi#listPlans",
            // Stripe's webhook; the signature header is verified in the handler
            "StripeWebhookApi#events");

    /** Every billing read and write outside {@code /public/} carries a token. */
    static final Set<String> AUTHENTICATED_ONLY = Set.of();

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
    static final ArchRule HANDLERS_ARE_GATED_OR_ANONYMOUS = CvhomeArchitectureRules
            .handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.CORE, ANONYMOUS, AUTHENTICATED_ONLY);

    @ArchTest
    static final ArchRule ANONYMOUS_LIST_IS_LIVE =
            CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, union(ANONYMOUS, AUTHENTICATED_ONLY));

    private BillingArchitectureTest() {
    }

    private static Set<String> union(Set<String> left, Set<String> right) {
        Set<String> all = new HashSet<>(left);
        all.addAll(right);
        return Set.copyOf(all);
    }

}
