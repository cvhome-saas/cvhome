package com.asrevo.cvhome.testsupport.arch;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaMember.Predicates.declaredIn;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
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

    private static final String REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";

    private static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";

    private static final String PRE_AUTHORIZE = "org.springframework.security.access.prepost.PreAuthorize";

    private static final String PRIVATE = "/private/";

    private static final String PUBLIC = "/public/";

    private static final String NO_PATH = "";

    private static final AntPathMatcher PATHS = new AntPathMatcher();

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
        return classes().that().areAnnotatedWith(REST_CONTROLLER)
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

    /**
     * Every request-mapped handler of a {@code @RestController} in the domain carries {@code @PreAuthorize} (on the
     * method or its class), or is declared anonymous by name in {@code anonymous}. Nothing in the filter chain
     * distinguishes a deliberately public endpoint from one that forgot its gate; the allow-list is what does, and it
     * is read by a person in the service's {@code <Domain>ArchitectureTest} next to a one-line reason per entry.
     *
     * <p>An entry is {@code SimpleClassName#methodName} ({@code "CartApi#create"}); overloads share one entry. The
     * {@code policy} adds the chain's own rule: on a {@link HandlerPolicy#POD} service a listed handler whose path
     * contains {@code /private/} fails, on a {@link HandlerPolicy#CORE} service a listed handler whose path does not
     * contain {@code /public/} fails — see {@link #handlersAreGatedOrDeclaredAnonymous(String, HandlerPolicy, Set, Set)}
     * for the store-core handlers that are meant to stay behind a bare session. A handler that is gated <em>and</em>
     * listed fails too, so the list never says something the code does not.
     *
     * <p>Pair it with {@link #anonymousAllowListIsLive(String, Set)} so a deleted endpoint cannot leave a stale entry.
     */
    public static ArchRule handlersAreGatedOrDeclaredAnonymous(String domain, HandlerPolicy policy, Set<String> anonymous) {
        return handlersAreGatedOrDeclaredAnonymous(domain, policy, anonymous, Set.of());
    }

    /**
     * As {@link #handlersAreGatedOrDeclaredAnonymous(String, HandlerPolicy, Set)}, with the {@link HandlerPolicy#CORE}
     * escape hatch: {@code authenticatedOnly} names the handlers outside {@code /public/} that deliberately carry no
     * token because any authenticated principal may call them (a "who am I" read). An entry there whose path is under
     * {@code /public/} fails, because that handler is anonymous and belongs in the other set. {@link HandlerPolicy#POD}
     * has no such tier — a {@code /private/} handler always needs a token — so it rejects a non-empty set outright.
     */
    public static ArchRule handlersAreGatedOrDeclaredAnonymous(String domain, HandlerPolicy policy, Set<String> anonymous,
            Set<String> authenticatedOnly) {
        if (policy == HandlerPolicy.POD && !authenticatedOnly.isEmpty()) {
            throw new IllegalArgumentException(
                    "HandlerPolicy.POD has no authenticated-only tier: a /private/ handler carries a token or is a finding");
        }
        return methods().that(handlersOf(domain))
                .should(new GatedOrDeclaredAnonymous(architectureTestOf(domain), policy, anonymous, authenticatedOnly))
                .as("every request handler is gated by @PreAuthorize or declared anonymous")
                .allowEmptyShould(true);
    }

    /**
     * Every entry of an allow-list — {@code anonymous} and, on store-core, {@code authenticatedOnly}; pass their union
     * or call it twice — still names a request-mapped handler of a {@code @RestController} in the domain. Without this
     * a deleted or renamed endpoint keeps its exemption forever, and the next handler to take the name inherits it.
     */
    public static ArchRule anonymousAllowListIsLive(String domain, Set<String> anonymous) {
        return all(allowListEntries(domain, anonymous))
                .should(new ResolvesToAHandler(architectureTestOf(domain)))
                .as("every anonymous allow-list entry names a live request handler")
                .allowEmptyShould(true);
    }

    private static String api(String domain) {
        return sub(domain, "api");
    }

    private static String sub(String domain, String leaf) {
        return String.format("%s.%s..", domain, leaf);
    }

    private static DescribedPredicate<JavaMethod> handlersOf(String domain) {
        DescribedPredicate<JavaClass> controllers = resideInAPackage(String.format("%s..", domain))
                .and(annotatedWith(REST_CONTROLLER));
        return DescribedPredicate.<JavaMethod>describe(
                String.format("request handlers of @RestController classes in %s", domain),
                method -> declaredIn(controllers).test(method) && (method.isAnnotatedWith(REQUEST_MAPPING)
                        || method.isMetaAnnotatedWith(REQUEST_MAPPING)));
    }

    /** {@code com.asrevo.cvhome.checkout} names {@code CheckoutArchitectureTest}, where the allow-list lives. */
    private static String architectureTestOf(String domain) {
        String leaf = domain.substring(domain.lastIndexOf('.') + 1);
        return String.format("%s%sArchitectureTest", leaf.substring(0, 1).toUpperCase(), leaf.substring(1));
    }

    private static String key(JavaMethod method) {
        return String.format("%s#%s", method.getOwner().getSimpleName(), method.getName());
    }

    /**
     * Class-level prefix times method-level path, as Spring merges {@code @GetMapping} & co. into a request mapping.
     * Combined the way Spring combines them, with a separator supplied when neither side spells one: the services
     * write {@code @RequestMapping("api/v1/signup")} over {@code @PostMapping("public/create")}, and a plain
     * concatenation would read that as {@code api/v1/signuppublic/create} — no {@code /public/} segment, and a
     * {@code /private/} one hidden the same way.
     */
    private static List<String> effectivePaths(JavaMethod method) {
        Method reflected = method.reflect();
        RequestMapping onClass = AnnotatedElementUtils.findMergedAnnotation(reflected.getDeclaringClass(), RequestMapping.class);
        RequestMapping onMethod = AnnotatedElementUtils.findMergedAnnotation(reflected, RequestMapping.class);
        List<String> paths = new ArrayList<>();
        for (String prefix : pathsOf(onClass)) {
            for (String suffix : pathsOf(onMethod)) {
                paths.add(PATHS.combine(prefix, suffix));
            }
        }
        return paths;
    }

    private static String[] pathsOf(RequestMapping mapping) {
        return mapping == null || mapping.path().length == 0 ? new String[] {NO_PATH} : mapping.path();
    }

    private static ClassesTransformer<AllowListEntry> allowListEntries(String domain, Set<String> declared) {
        DescribedPredicate<JavaMethod> handlers = handlersOf(domain);
        return new AbstractClassesTransformer<>("anonymous allow-list entries") {
            @Override
            public Iterable<AllowListEntry> doTransform(JavaClasses classes) {
                Set<String> live = new HashSet<>();
                for (JavaClass candidate : classes) {
                    for (JavaMethod method : candidate.getMethods()) {
                        if (handlers.test(method)) {
                            live.add(key(method));
                        }
                    }
                }
                return declared.stream().sorted().map(entry -> new AllowListEntry(entry, live.contains(entry))).toList();
            }
        };
    }

    private record AllowListEntry(String key, boolean live) {
    }

    private static final class ResolvesToAHandler extends ArchCondition<AllowListEntry> {

        private final String architectureTest;

        ResolvesToAHandler(String architectureTest) {
            super("resolve to a request handler");
            this.architectureTest = architectureTest;
        }

        @Override
        public void check(AllowListEntry entry, ConditionEvents events) {
            if (entry.live()) {
                events.add(SimpleConditionEvent.satisfied(entry, String.format("%s is a live handler", entry.key())));
                return;
            }
            events.add(SimpleConditionEvent.violated(entry, String.format("""
                    allow-list entry "%s" names no request handler: the endpoint was deleted or renamed, \
                    remove the entry from the anonymous allow-list of %s""", entry.key(), architectureTest)));
        }
    }

    private static final class GatedOrDeclaredAnonymous extends ArchCondition<JavaMethod> {

        private final String architectureTest;

        private final HandlerPolicy policy;

        private final Set<String> anonymous;

        private final Set<String> authenticatedOnly;

        GatedOrDeclaredAnonymous(String architectureTest, HandlerPolicy policy, Set<String> anonymous,
                Set<String> authenticatedOnly) {
            super("be gated by @PreAuthorize or declared anonymous");
            this.architectureTest = architectureTest;
            this.policy = policy;
            this.anonymous = Set.copyOf(anonymous);
            this.authenticatedOnly = Set.copyOf(authenticatedOnly);
        }

        @Override
        public void check(JavaMethod method, ConditionEvents events) {
            String handler = key(method);
            List<String> paths = effectivePaths(method);
            String where = String.format("%s (%s)", handler, String.join(", ", paths));
            Optional<String> problem = problemWith(method, handler, paths);
            events.add(problem.map(text -> SimpleConditionEvent.violated(method, String.format("%s %s", where, text)))
                    .orElseGet(() -> SimpleConditionEvent.satisfied(method, String.format("%s is accounted for", where))));
        }

        private Optional<String> problemWith(JavaMethod method, String handler, List<String> paths) {
            boolean gated = method.isAnnotatedWith(PRE_AUTHORIZE) || method.getOwner().isAnnotatedWith(PRE_AUTHORIZE);
            boolean listedAnonymous = anonymous.contains(handler);
            boolean listedAuthenticated = authenticatedOnly.contains(handler);
            if (gated) {
                return listedAnonymous || listedAuthenticated
                        ? Optional.of(String.format("is gated by @PreAuthorize and also allow-listed; remove the entry from %s",
                                architectureTest))
                        : Optional.empty();
            }
            if (listedAnonymous) {
                return anonymousEntryProblem(paths);
            }
            if (listedAuthenticated) {
                return paths.stream().anyMatch(path -> path.contains(PUBLIC))
                        ? Optional.of("is listed as authenticated-only but /public/ is permitAll: move it to the anonymous set")
                        : Optional.empty();
            }
            return Optional.of(String.format(
                    "has no @PreAuthorize: add one, or list \"%s\" in the anonymous allow-list of %s with a one-line reason",
                    handler, architectureTest));
        }

        private Optional<String> anonymousEntryProblem(List<String> paths) {
            if (policy == HandlerPolicy.POD && paths.stream().anyMatch(path -> path.contains(PRIVATE))) {
                return Optional.of(String.format("""
                        is declared anonymous but /private/ is authenticated by the pod chain: \
                        add @PreAuthorize and remove the entry from %s""", architectureTest));
            }
            if (policy == HandlerPolicy.CORE && paths.stream().noneMatch(path -> path.contains(PUBLIC))) {
                return Optional.of(String.format("""
                        is declared anonymous but the store-core chain authenticates everything outside /public/: \
                        add @PreAuthorize, or move the entry to the authenticatedOnly set of %s \
                        if any signed-in principal may call it""", architectureTest));
            }
            return Optional.empty();
        }
    }

}
