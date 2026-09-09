package com.asrevo.cvhome.testsupport.arch;

import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.testsupport.arch.fixtures.ClassGatedApi;
import com.asrevo.cvhome.testsupport.arch.fixtures.GatedApi;
import com.asrevo.cvhome.testsupport.arch.fixtures.LeakyApi;
import com.asrevo.cvhome.testsupport.arch.fixtures.NotAController;
import com.asrevo.cvhome.testsupport.arch.fixtures.PublicApi;
import com.asrevo.cvhome.testsupport.arch.fixtures.StorefrontApi;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The gate rule against fixture controllers: a token passes, a declared anonymous handler passes, and every way the
 * allow-list can lie — a forgotten gate, a {@code /private/} entry on a pod, a non-{@code /public/} entry on
 * store-core, an entry for a handler that no longer exists — fails with a message that says what to do.
 */
class CvhomeArchitectureRulesTest {

    private static final String DOMAIN = "com.asrevo.cvhome.testsupport.arch.fixtures";

    private static final String ARCH_TEST = "FixturesArchitectureTest";

    private static final String STOREFRONT_CART = "StorefrontApi#cart";

    private static final String LEAKY_CUSTOMERS = "LeakyApi#customers";

    private static final String PUBLIC_CATALOG = "PublicApi#catalog";

    private static final String PUBLIC_ME = "PublicApi#me";

    private static final String GATED_ORDERS = "GatedApi#orders";

    private static JavaClasses classes(Class<?>... fixtures) {
        return new ClassFileImporter().importClasses(fixtures);
    }

    @Nested
    class OnAPodService {

        @Test
        void gatedHandlersPassWithAnEmptyAllowList() {
            ArchRule rule = CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD, Set.of());

            assertThatCode(() -> rule.check(classes(GatedApi.class, ClassGatedApi.class, NotAController.class)))
                    .doesNotThrowAnyException();
        }

        @Test
        void aDeclaredAnonymousHandlerPasses() {
            ArchRule rule = CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD,
                    Set.of(STOREFRONT_CART));

            assertThatCode(() -> rule.check(classes(GatedApi.class, StorefrontApi.class))).doesNotThrowAnyException();
        }

        @Test
        void anUngatedHandlerOutsideTheListFailsAndNamesTheFix() {
            ArchRule rule = CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD, Set.of());

            assertThatThrownBy(() -> rule.check(classes(StorefrontApi.class)))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining(STOREFRONT_CART)
                    .hasMessageContaining("/api/v1/cart/{code}")
                    .hasMessageContaining("add one, or list \"StorefrontApi#cart\" in the anonymous allow-list of")
                    .hasMessageContaining(ARCH_TEST);
        }

        @Test
        void aPrivateHandlerOnTheListFailsEvenThoughListed() {
            ArchRule rule = CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD,
                    Set.of(LEAKY_CUSTOMERS));

            assertThatThrownBy(() -> rule.check(classes(LeakyApi.class)))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining(LEAKY_CUSTOMERS)
                    .hasMessageContaining("/api/v1/private/customers")
                    .hasMessageContaining("/private/ is authenticated by the pod chain");
        }

        @Test
        void aPrivateHandlerWithNoGateFails() {
            ArchRule rule = CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD, Set.of());

            assertThatThrownBy(() -> rule.check(classes(LeakyApi.class)))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining(LEAKY_CUSTOMERS)
                    .hasMessageContaining("has no @PreAuthorize");
        }

        @Test
        void aGatedHandlerOnTheListFails() {
            ArchRule rule = CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD,
                    Set.of(GATED_ORDERS));

            assertThatThrownBy(() -> rule.check(classes(GatedApi.class)))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining(GATED_ORDERS)
                    .hasMessageContaining("gated by @PreAuthorize and also allow-listed");
        }

        @Test
        void hasNoAuthenticatedOnlyTier() {
            Set<String> none = Set.of();
            Set<String> me = Set.of(PUBLIC_ME);
            assertThatThrownBy(() -> CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD, none, me))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class OnAStoreCoreService {

        @Test
        void anAnonymousEntryUnderPublicPasses() {
            ArchRule rule = CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.CORE,
                    Set.of(PUBLIC_CATALOG), Set.of(PUBLIC_ME));

            assertThatCode(() -> rule.check(classes(PublicApi.class, GatedApi.class))).doesNotThrowAnyException();
        }

        @Test
        void anAnonymousEntryOutsidePublicFails() {
            ArchRule rule = CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.CORE,
                    Set.of(PUBLIC_CATALOG, PUBLIC_ME));

            assertThatThrownBy(() -> rule.check(classes(PublicApi.class)))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining(PUBLIC_ME)
                    .hasMessageContaining("/api/v1/plans/me")
                    .hasMessageContaining("move the entry to the authenticatedOnly set of");
        }

        @Test
        void anAuthenticatedOnlyEntryUnderPublicFails() {
            ArchRule rule = CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.CORE,
                    Set.of(PUBLIC_ME), Set.of(PUBLIC_CATALOG));

            assertThatThrownBy(() -> rule.check(classes(PublicApi.class)))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining(PUBLIC_CATALOG)
                    .hasMessageContaining("move it to the anonymous set");
        }
    }

    @Nested
    class TheAllowListIsLive {

        @Test
        void entriesThatNameAHandlerPass() {
            ArchRule rule = CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, Set.of(STOREFRONT_CART, GATED_ORDERS));

            assertThatCode(() -> rule.check(classes(GatedApi.class, StorefrontApi.class))).doesNotThrowAnyException();
        }

        @Test
        void aStaleEntryFails() {
            ArchRule rule = CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, Set.of(STOREFRONT_CART, "StorefrontApi#gone"));

            assertThatThrownBy(() -> rule.check(classes(StorefrontApi.class)))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("\"StorefrontApi#gone\" names no request handler")
                    .hasMessageContaining(ARCH_TEST);
        }

        @Test
        void aMappedMethodOnANonControllerIsNotAHandler() {
            ArchRule rule = CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, Set.of("NotAController#ignored"));

            assertThatThrownBy(() -> rule.check(classes(NotAController.class))).isInstanceOf(AssertionError.class);
        }

        @Test
        void anEmptyListPasses() {
            ArchRule rule = CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, Set.of());

            assertThatCode(() -> rule.check(classes(GatedApi.class))).doesNotThrowAnyException();
        }
    }

}
