package com.asrevo.cvhome.aot;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * Lets a native image build our value objects from text and from JSON.
 *
 * <p>
 * {@code PodId}, {@code StoreMerchantId}, {@code PlanId} and the rest are records with a {@code String} constructor,
 * and three things construct them reflectively: the configuration binder ({@code com.asrevo.cvhome.pods[0].id}), MVC's
 * conversion of a path variable or request parameter, and Jackson. Every pod service refused to start natively with
 * "Failed to bind properties under 'com.asrevo.cvhome.pods[0].id' to PodId".
 * </p>
 *
 * <p>
 * Covered: everything in {@value #DOMAIN_PACKAGE}, and every {@code Identifier} anywhere under {@value #BASE_PACKAGE}
 * (billing's ids live in billing-commons). Their constructors and static factories may be invoked, and they are
 * registered for JSON binding the way Spring registers a controller's body types.
 * </p>
 */
public class ValueObjectRuntimeHints implements RuntimeHintsRegistrar {

    static final String BASE_PACKAGE = "com.asrevo.cvhome";

    static final String DOMAIN_PACKAGE = "com.asrevo.cvhome.commons.domain";

    static final String IDENTIFIER = "com.asrevo.cvhome.commons.domain.Identifier";

    /** A class directly in the domain package (a nested one included), not in a sub-package. */
    private static final Pattern DOMAIN_CLASS = Pattern.compile("com\\.asrevo\\.cvhome\\.commons\\.domain\\.[^.]+");

    private final BindingReflectionHintsRegistrar bindings = new BindingReflectionHintsRegistrar();

    static Set<Class<?>> valueObjects(ClassLoader classLoader) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter(new AssignableTypeFilter(ClassUtils.resolveClassName(IDENTIFIER, classLoader)));
        scanner.addIncludeFilter(new RegexPatternTypeFilter(DOMAIN_CLASS));
        Set<Class<?>> found = new LinkedHashSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            found.add(ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader));
        }
        return found;
    }

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        if (!ClassUtils.isPresent(IDENTIFIER, classLoader)) {
            return;
        }
        for (Class<?> type : valueObjects(classLoader)) {
            hints.reflection().registerType(type, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
            bindings.registerReflectionHints(hints.reflection(), type);
        }
    }

}
