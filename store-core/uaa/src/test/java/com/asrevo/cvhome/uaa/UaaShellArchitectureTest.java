package com.asrevo.cvhome.uaa;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * uaa is a shell, and this is what keeps it one.
 *
 * <p>
 * uaa and cua are one server deployed twice: everything they do lives in {@code sso-core}, and each shell supplies
 * only its deployment's identity — how the issuer is pinned, how a realm is resolved, its seeds and its front end.
 * The reason to enforce that rather than trust it is the history: the two servers were written separately, uaa was
 * hardened in #319, and cua never received any of it. A service class that lands here instead of in sso-core is
 * the first step back to two divergent servers, and it will look perfectly reasonable in review.
 * </p>
 *
 * <p>
 * Configuration and edge adapters are welcome here — that is what a shell is for. Persistent state, business
 * services and endpoints are not.
 * </p>
 */
@AnalyzeClasses(packages = UaaShellArchitectureTest.SHELL, importOptions = ImportOption.DoNotIncludeTests.class)
final class UaaShellArchitectureTest {

    static final String SHELL = "com.asrevo.cvhome.uaa";

    private static final String WHY = """
            the SSO server lives in sso-core, shared with cua; a %s here is a fork of it in all but name""";

    @ArchTest
    static final ArchRule NO_ENTITIES = noClasses().should()
            .beAnnotatedWith("jakarta.persistence.Entity")
            .because(WHY.formatted("persistent entity"));

    @ArchTest
    static final ArchRule NO_REPOSITORIES = noClasses().should()
            .beAnnotatedWith("org.springframework.stereotype.Repository")
            .because(WHY.formatted("repository"));

    @ArchTest
    static final ArchRule NO_SERVICES = noClasses().should()
            .beAnnotatedWith("org.springframework.stereotype.Service")
            .because(WHY.formatted("service"));

    @ArchTest
    static final ArchRule NO_CONTROLLERS = noClasses().should()
            .beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .orShould()
            .beAnnotatedWith("org.springframework.stereotype.Controller")
            .because(WHY.formatted("controller"));

    private UaaShellArchitectureTest() {
    }

}
