package com.asrevo.cvhome.merchant;

import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the merchant domain, enforced — the rules live in {@link CvhomeArchitectureRules} — plus the
 * authorization gate: every request handler carries {@code @PreAuthorize} or is named in {@link #ANONYMOUS} with its
 * reason. The rule refuses a {@code /private/} entry and an entry that no longer names a handler.
 */
@AnalyzeClasses(packages = MerchantArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class MerchantArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.merchant";

    /**
     * The storefront reads the store record with no token (A14, accepted: the record carries {@code audit} and the
     * domains, and the Next.js storefront needs it before any session exists). {@code RouterController}'s
     * {@code public/} pair is the edge's domain lookup: saas-gateway asks whether to issue a certificate for a
     * domain and which store headers to stamp on it; the caller is Caddy, which holds no token.
     */
    static final Set<String> ANONYMOUS = Set.of(
            // storefront read of the store by the request's store id (A14)
            "ExternalMerchantStoreApi#getStore",
            // storefront read of the store by code, and the language list the storefront renders
            "MerchantStoreApi#store", "MerchantStoreApi#supportedLanguages",
            // saas-gateway (Caddy) asks for TLS and the routing headers of a custom domain
            "RouterController#ask", "RouterController#getLookupHeadersByDomain");

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

    private MerchantArchitectureTest() {
    }

}
