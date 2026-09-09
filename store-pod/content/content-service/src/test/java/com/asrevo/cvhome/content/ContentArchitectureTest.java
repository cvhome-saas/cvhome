package com.asrevo.cvhome.content;

import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the content domain, enforced — the rules live in {@link CvhomeArchitectureRules} — plus the
 * authorization gate: every request handler carries {@code @PreAuthorize} or is named in {@link #ANONYMOUS} with its
 * reason. The rule refuses a {@code /private/} entry and an entry that no longer names a handler.
 */
@AnalyzeClasses(packages = ContentArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class ContentArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.content";

    /**
     * {@code /api/v1/storefront/**}: the published site as the guest storefront renders it. Everything the console
     * edits lives under {@code /private/content/**} and is gated; this surface is the read-only projection of it.
     */
    static final Set<String> ANONYMOUS = Set.of(
            // the site shell, a page, a post and its listing, the post categories
            "StorefrontApi#site", "StorefrontApi#page", "StorefrontApi#posts", "StorefrontApi#post",
            "StorefrontApi#postCategories",
            // banners, a page layout, the FAQ, a menu, a policy text
            "StorefrontApi#banners", "StorefrontApi#layout", "StorefrontApi#faq", "StorefrontApi#menu",
            "StorefrontApi#policy",
            // the sitemap and the redirect table the storefront serves
            "StorefrontApi#sitemap", "StorefrontApi#redirect");

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

    private ContentArchitectureTest() {
    }

}
