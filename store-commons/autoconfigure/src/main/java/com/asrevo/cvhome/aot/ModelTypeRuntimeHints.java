package com.asrevo.cvhome.aot;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * Lets Jackson read and write our JSON shapes in a native image, however they reach it.
 *
 * <p>
 * Spring's AOT support registers the types a controller signature names. Two kinds of JSON never appear there, and
 * the load test found both on its first native run:
 * </p>
 *
 * <ul>
 *     <li>a record kept as a JSON column: content's banner, FAQ, post and policy metas, read with
 *     {@code JsonCodec.read(entity.getMeta(), BannerMeta.class)} — every storefront banner read answered 500,
 *     "Record components not available for record class BannerMeta";</li>
 *     <li>the element of a generic list: {@code ReadableTransactionList extends ReadableList<ReadableTransaction>},
 *     whose {@code T} the controller's registration does not resolve — payment's transaction list failed the same
 *     way.</li>
 * </ul>
 *
 * <p>
 * This codebase keeps its JSON shapes in {@code model} packages ({@code <domain>-commons/.../model/}), so every type
 * in a package with a {@code model} segment under {@value #BASE_PACKAGE} is registered for binding, found by
 * scanning: a new meta or list element is covered by existing.
 * </p>
 */
public class ModelTypeRuntimeHints implements RuntimeHintsRegistrar {

    static final String BASE_PACKAGE = "com.asrevo.cvhome";

    /** A type in a package that has a {@code model} segment, nested types included. */
    private static final Pattern MODEL_TYPE = Pattern.compile("com\\.asrevo\\.cvhome\\.(?:[^.]+\\.)*model\\.[^.]+(?:\\.[^.]+)*");

    private final BindingReflectionHintsRegistrar bindings = new BindingReflectionHintsRegistrar();

    static Set<Class<?>> modelTypes(ClassLoader classLoader) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                return definition.getMetadata().isIndependent() && !definition.getMetadata().isAnnotation();
            }
        };
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter(new RegexPatternTypeFilter(MODEL_TYPE));
        Set<Class<?>> found = new LinkedHashSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            found.add(ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader));
        }
        return found;
    }

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        bindings.registerReflectionHints(hints.reflection(), modelTypes(classLoader).toArray(Class<?>[]::new));
    }

}
