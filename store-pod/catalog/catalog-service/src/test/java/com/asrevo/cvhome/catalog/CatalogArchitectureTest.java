package com.asrevo.cvhome.catalog;

import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the catalog domain, enforced — the rules live in {@link CvhomeArchitectureRules} — plus the
 * authorization gate: every request handler carries {@code @PreAuthorize} or is named in {@link #ANONYMOUS} with its
 * reason. The rule refuses a {@code /private/} entry and an entry that no longer names a handler.
 */
@AnalyzeClasses(packages = CatalogArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class CatalogArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.catalog";

    /**
     * The storefront's catalog reads, all outside {@code /private/}: the product, category, manufacturer, group,
     * image and relationship reads a guest browses, and the checkout-side detailed product read. Each has a
     * {@code /private/} twin for the console, which is gated.
     */
    static final Set<String> ANONYMOUS = Set.of(
            // storefront category tree and category page
            "CategoryApi#hierarchy", "CategoryApi#getByFriendlyUrl",
            // checkout and storefront read the priced product(s) by SKU
            "ExternalProductApi#getDetailedProduct", "ExternalProductApi#getDetailedProducts",
            // storefront brand filter of a category
            "ManufacturerApi#listByCategory",
            // storefront listing, search, type-ahead and product page
            "ProductApiV2#list", "ProductApiV2#search", "ProductApiV2#suggest", "ProductApiV2#getByFriendlyUrl",
            // storefront product group (featured, new arrivals) by code
            "ProductGroupApi#get",
            // storefront gallery of a product
            "ProductImageApi#list",
            // storefront "related products" of a product
            "ProductRelationshipApi#related");

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

    private CatalogArchitectureTest() {
    }

}
