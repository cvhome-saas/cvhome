package com.asrevo.cvhome.testsupport.arch;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The layering every domain follows, as executable rules. A service's {@code <Domain>ArchitectureTest} binds them to
 * its own base package:
 *
 * <pre>
 * &#64;AnalyzeClasses(packages = CatalogArchitectureTest.DOMAIN, importOptions = ImportOption.DoNotIncludeTests.class)
 * class CatalogArchitectureTest {
 *     static final String DOMAIN = "com.asrevo.cvhome.catalog";
 *     &#64;ArchTest static final ArchRule api = CvhomeArchitectureRules.apiDoesNotTouchRepositories(DOMAIN);
 * }
 * </pre>
 *
 * The rules are deliberately scoped to one domain: calling <em>another</em> pod's {@code -external-api} client from
 * {@code -core} is how services talk to each other here, and must not be flagged.
 */
public final class CvhomeArchitectureRules {

    private static final String SERVICES = "services";

    private CvhomeArchitectureRules() {
    }

    /** Controllers orchestrate through services; they never reach this domain's repositories directly. */
    public static ArchRule apiDoesNotTouchRepositories(String domain) {
        return noClasses().that().resideInAPackage(api(domain))
                .should().dependOnClassesThat().resideInAPackage(sub(domain, "repositories"))
                .as("api must go through services, not repositories")
                .allowEmptyShould(true);
    }

    /**
     * Business logic does not know about its own web layer. {@code ..api.errors..} is deliberately exempt: the typed
     * exception a service throws is part of the domain's error contract and lives beside the api that surfaces it.
     */
    public static ArchRule servicesDoNotDependOnOwnApi(String domain) {
        return noClasses().that().resideInAPackage(sub(domain, SERVICES))
                .should().dependOnClassesThat(
                        resideInAPackage(api(domain)).and(not(resideInAPackage(String.format("%s.api.errors..", domain)))))
                .as("services must not depend on their own api package")
                .allowEmptyShould(true);
    }

    /** Persistence types stay below the service layer. */
    public static ArchRule entitiesDoNotDependOnServices(String domain) {
        return noClasses().that().resideInAPackage(sub(domain, "entity"))
                .should().dependOnClassesThat().resideInAnyPackage(sub(domain, SERVICES), api(domain))
                .as("entities must not depend on services or api")
                .allowEmptyShould(true);
    }

    /** Every REST controller sits under an {@code api} package, so the rules above can see it. */
    public static ArchRule controllersLiveInApi(String domain) {
        return controllersLiveIn(domain);
    }

    /**
     * The same rule for a service that still has controllers outside {@code ..api..}. Pass the legacy packages
     * explicitly ({@code "..manager.controller.."}) so the deviation is declared and new controllers cannot quietly
     * add more locations.
     */
    public static ArchRule controllersLiveIn(String domain, String... legacyPackages) {
        String[] allowed = new String[legacyPackages.length + 1];
        allowed[0] = api(domain);
        System.arraycopy(legacyPackages, 0, allowed, 1, legacyPackages.length);
        return classes().that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().resideInAnyPackage(allowed)
                .as("@RestController classes belong in a declared api package")
                .allowEmptyShould(true);
    }

    /** Test-only infrastructure never ships in a production jar. */
    public static ArchRule noTestSupportInProduction() {
        return noClasses().should().dependOnClassesThat().resideInAPackage("com.asrevo.cvhome.testsupport..")
                .as("main code must not depend on test-support")
                .allowEmptyShould(true);
    }

    private static String api(String domain) {
        return sub(domain, "api");
    }

    private static String sub(String domain, String leaf) {
        return String.format("%s.%s..", domain, leaf);
    }

}
