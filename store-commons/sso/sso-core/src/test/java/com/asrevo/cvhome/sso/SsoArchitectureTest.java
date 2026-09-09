package com.asrevo.cvhome.sso;

import java.util.HashSet;
import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the SSO server, enforced. It predates the {@code api} convention — its controllers live under
 * {@code web} — so that package is declared as the one legacy location rather than left unchecked.
 *
 * <p>Plus the authorization gate, under the store-core policy: the shared {@code publicApiSecurity} chain permits
 * {@code /api/v1/public/**} and each shell authenticates everything else, so an anonymous handler is one under
 * {@code /public/} and is named in {@link #ANONYMOUS}; a handler outside it that carries no token is named in
 * {@link #AUTHENTICATED_ONLY}. Two of those are opened further by an explicit matcher in a shell — see the set.
 */
@AnalyzeClasses(packages = SsoArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class SsoArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.sso";

    /** The one-time-link and pre-login surface: what a browser calls before it has a session. */
    static final Set<String> ANONYMOUS = Set.of(
            // self-registration (cua: a shopper; uaa: when the settings allow it)
            "PublicRegistrationController#register",
            // the login page's context and its settings (which factors, which providers, the branding)
            "PublicLoginController#context", "PublicLoginController#settings",
            // the brokered-login providers the login page offers, and discovery by e-mail domain
            "PublicIdpController#visible", "PublicIdpController#discover",
            // an invitation link: preview it, accept it; the token is the credential
            "PublicInvitationController#preview", "PublicInvitationController#accept",
            // a password-reset link: preview it, accept it; the token is the credential
            "PublicPasswordResetController#preview", "PublicPasswordResetController#accept");

    /**
     * Outside {@code /public/} and carrying no token. {@code AuthController#me} answers any signed-in principal — and
     * cua opens the path by matcher so the storefront can ask "who am I" before a session exists (by-design register).
     * {@code LinkConfirmController#confirm} is the password step of a brokered login: it is on the application chain
     * on purpose, so CSRF applies and the parked {@code PendingLink} in the session — plus the password — is the
     * credential; uaa opens the path by matcher for the same reason.
     */
    static final Set<String> AUTHENTICATED_ONLY = Set.of(
            // who is signed in; cua additionally permits the path for the storefront's pre-session probe
            "AuthController#me",
            // the password confirmation that links a brokered identity to an existing account
            "LinkConfirmController#confirm");

    @ArchTest
    static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);

    @ArchTest
    static final ArchRule SERVICES_STAY_OFF_THE_WEB = CvhomeArchitectureRules.servicesDoNotDependOnOwnApi(DOMAIN);

    @ArchTest
    static final ArchRule ENTITIES_STAY_BELOW = CvhomeArchitectureRules.entitiesDoNotDependOnServices(DOMAIN);

    @ArchTest
    static final ArchRule CONTROLLERS_IN_WEB = CvhomeArchitectureRules.controllersLiveIn(DOMAIN, "..sso.web..");

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    @ArchTest
    static final ArchRule HANDLERS_ARE_GATED_OR_ANONYMOUS = CvhomeArchitectureRules
            .handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.CORE, ANONYMOUS, AUTHENTICATED_ONLY);

    @ArchTest
    static final ArchRule ANONYMOUS_LIST_IS_LIVE =
            CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, union(ANONYMOUS, AUTHENTICATED_ONLY));

    private SsoArchitectureTest() {
    }

    private static Set<String> union(Set<String> left, Set<String> right) {
        Set<String> all = new HashSet<>(left);
        all.addAll(right);
        return Set.copyOf(all);
    }

}
