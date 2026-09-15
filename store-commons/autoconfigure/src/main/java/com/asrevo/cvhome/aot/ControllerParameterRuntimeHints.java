package com.asrevo.cvhome.aot;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lets MVC build our request objects from query parameters in a native image.
 *
 * <p>
 * A handler parameter that is one of our own classes and not a {@code @RequestBody} — catalog's
 * {@code ProductSearchCriteria}, filled from {@code ?q=&category=&sort=} — is a model attribute: MVC finds its
 * constructor and setters reflectively. Spring's AOT support registers a controller's bodies and return types, not
 * these, and catalog's native search answered 500: "No primary or single unique constructor found for class
 * ProductSearchCriteria". Every parameter type under {@value #BASE_PACKAGE} of every request-mapped method is
 * registered for binding (a body registered twice costs nothing), found by scanning, so a new criteria object is
 * covered by existing.
 * </p>
 */
public class ControllerParameterRuntimeHints implements RuntimeHintsRegistrar {

    static final String BASE_PACKAGE = "com.asrevo.cvhome";

    private static final String OUR_TYPES = "com.asrevo.cvhome.";

    private final BindingReflectionHintsRegistrar bindings = new BindingReflectionHintsRegistrar();

    static Set<Type> parameterTypes(ClassLoader classLoader) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        Set<Type> types = new LinkedHashSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> controller = ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader);
            ReflectionUtils.doWithMethods(controller, method -> collect(method, types),
                    method -> MergedAnnotations.from(method).isPresent(RequestMapping.class));
        }
        return types;
    }

    private static void collect(Method method, Set<Type> types) {
        Class<?>[] raw = method.getParameterTypes();
        Type[] generic = method.getGenericParameterTypes();
        for (int i = 0; i < raw.length; i++) {
            if (raw[i].getName().startsWith(OUR_TYPES)) {
                types.add(generic[i]);
            }
        }
    }

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        bindings.registerReflectionHints(hints.reflection(), parameterTypes(classLoader).toArray(Type[]::new));
    }

}
