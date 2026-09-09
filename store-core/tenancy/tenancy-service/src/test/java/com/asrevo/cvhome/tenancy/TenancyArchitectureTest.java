package com.asrevo.cvhome.tenancy;

import java.util.HashSet;
import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the tenancy domain, enforced — the rules live in {@link CvhomeArchitectureRules} — plus the
 * authorization gate: every request handler carries {@code @PreAuthorize} or is named in {@link #ANONYMOUS} (under
 * {@code /public/}) or {@link #AUTHENTICATED_ONLY} (a bare session is the gate) with its reason. The rule refuses an
 * anonymous entry outside {@code /public/} and an entry that no longer names a handler.
 *
 * <p>Not listed on purpose: {@code AuthApi#me} and {@code AuthApi#current} (audit A10 — the P3 branch deletes the
 * first and gates the second with {@code isAuthenticated()}); the gate rule is red here until that lands, and green
 * with no edit once it does. The interim handler walk in {@code TenancyApisTest} on that branch is superseded by
 * this class once both are in.
 */
@AnalyzeClasses(packages = TenancyArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class TenancyArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.tenancy";

    /** What a browser reaches before it has an account: sign-up, the platform's public properties, the theme lists. */
    static final Set<String> ANONYMOUS = Set.of(
            // the sign-up form creates the org and its first admin
            "SignUpApi#create",
            // the platform suffix and domain the console and storefront need to build URLs
            "SaasApi#saasProperties",
            // the store-creation wizard's theme, colour-theme and social-provider lists
            "StoreManagerApi#themes", "StoreManagerApi#colorThemes", "StoreManagerApi#socialLinkProviders");

    /** Any signed-in principal, no store in the request; the chain's authentication is the whole gate. */
    static final Set<String> AUTHENTICATED_ONLY = Set.of(
            // the invitee accepts with the token from the mail; the account is whoever is signed in
            "OrgMemberApi#accept",
            // the signed-in user's own account, by the principal's name
            "UserAccountApi#current",
            // the role catalogue a user may assign; read from uaa, no tenant data
            "UserAccountApi#assignableRoles");

    @ArchTest
    static final ArchRule API_GOES_THROUGH_SERVICES = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);

    @ArchTest
    static final ArchRule SERVICES_STAY_OFF_THE_WEB = CvhomeArchitectureRules.servicesDoNotDependOnOwnApi(DOMAIN);

    @ArchTest
    static final ArchRule ENTITIES_STAY_BELOW = CvhomeArchitectureRules.entitiesDoNotDependOnServices(DOMAIN);

    /**
     * tenancy predates the {@code ..api..} convention and keeps its controllers under {@code manager.controller};
     * the deviation is declared here so no <em>new</em> location can appear.
     */
    @ArchTest
    static final ArchRule CONTROLLERS_IN_API =
            CvhomeArchitectureRules.controllersLiveIn(DOMAIN,
                    "com.asrevo.cvhome.tenancy.manager.controller..", "com.asrevo.cvhome.tenancy.controller..");

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    @ArchTest
    static final ArchRule HANDLERS_ARE_GATED_OR_ANONYMOUS = CvhomeArchitectureRules
            .handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.CORE, ANONYMOUS, AUTHENTICATED_ONLY);

    @ArchTest
    static final ArchRule ANONYMOUS_LIST_IS_LIVE =
            CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, union(ANONYMOUS, AUTHENTICATED_ONLY));

    private TenancyArchitectureTest() {
    }

    private static Set<String> union(Set<String> left, Set<String> right) {
        Set<String> all = new HashSet<>(left);
        all.addAll(right);
        return Set.copyOf(all);
    }

}
