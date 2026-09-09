package com.asrevo.cvhome.podregistry;

import java.util.HashSet;
import java.util.Set;

import com.asrevo.cvhome.testsupport.arch.CvhomeArchitectureRules;
import com.asrevo.cvhome.testsupport.arch.HandlerPolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The layering of the podregistry domain, enforced — the rules live in {@link CvhomeArchitectureRules} — plus the
 * authorization gate: every request handler carries {@code @PreAuthorize}. Both allow-lists are empty and stay so:
 * the registry has no anonymous surface, and the rule is what keeps the next handler from adding one silently.
 */
@AnalyzeClasses(packages = PodRegistryArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
final class PodRegistryArchitectureTest {

    static final String DOMAIN = "com.asrevo.cvhome.podregistry";

    /** No anonymous handler: every pod read and write is a super-admin or service-principal token. */
    static final Set<String> ANONYMOUS = Set.of();

    /** No session-only handler either. */
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

    private PodRegistryArchitectureTest() {
    }

    private static Set<String> union(Set<String> left, Set<String> right) {
        Set<String> all = new HashSet<>(left);
        all.addAll(right);
        return Set.copyOf(all);
    }

}
