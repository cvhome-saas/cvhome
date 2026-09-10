package com.asrevo.cvhome.cua;

import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The shell's own controllers, gated or declared anonymous. cua is sso-core deployed with one realm per store; the
 * SSO server's handlers live under {@code com.asrevo.cvhome.sso} and are covered by {@code SsoArchitectureTest}. What
 * this class scans is only what the shell adds: the merchant-facing shopper and identity-provider administration
 * (gated, and {@code MerchantApisTest} asserts the token) and the storefront's social-login list.
 *
 * <p>The layering rules are not bound here: the shell has no {@code api}, {@code services} or {@code entity} package,
 * and {@code UaaShellArchitectureTest} is the model for what a shell may not contain.
 */
@AnalyzeClasses(packages = CuaArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class CuaArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.cua";

    /** The one storefront read: which social logins a store offers, asked before a shopper has any session. */
    static final Set<String> ANONYMOUS = Set.of(
            // storefront login page lists the enabled social logins of the store
            "PublicSocialLoginController#enabledLogins");

    @ArchTest
    static final ArchRule CONTROLLERS_IN_WEB = CvhomeArchitectureRules.controllersLiveIn(DOMAIN, "..cua.web..");

    @ArchTest
    static final ArchRule NO_TEST_SUPPORT_IN_PRODUCTION = CvhomeArchitectureRules.noTestSupportInProduction();

    @ArchTest
    static final ArchRule HANDLERS_ARE_GATED_OR_ANONYMOUS =
            CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(DOMAIN, HandlerPolicy.POD, ANONYMOUS);

    @ArchTest
    static final ArchRule ANONYMOUS_LIST_IS_LIVE = CvhomeArchitectureRules.anonymousAllowListIsLive(DOMAIN, ANONYMOUS);

    private CuaArchitectureTest() {
    }

}
