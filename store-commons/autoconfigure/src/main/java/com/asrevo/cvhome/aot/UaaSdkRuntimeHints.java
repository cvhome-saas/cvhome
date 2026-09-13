package com.asrevo.cvhome.aot;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.ClassUtils;

/**
 * Lets a native service use the uaa admin SDK ({@code store-commons/uaa-client-impl}), which tenancy manages users
 * through.
 *
 * <p>
 * The SDK is plain Java — {@code java.net.http} and its own Jackson mapper — so nothing Spring processes at build time
 * ever sees its request and response types, including the private token record it reads uaa's answer into. The native
 * tenancy service exited on its first call: "Record components not available for record class
 * OAuth2TokenManager$TokenResponse". Every class of the SDK and of its API module is registered for JSON binding.
 * Inert in a service without the SDK.
 * </p>
 */
public class UaaSdkRuntimeHints implements RuntimeHintsRegistrar {

    static final String SDK = "com.asrevo.cvhome.uaa.sdk.OAuth2TokenManager";

    /** The SDK's implementation package and its API module's types. */
    static final String[] PACKAGES = {"com.asrevo.cvhome.uaa.sdk", "com.asrevo.cvhome.uaa.domain"};

    private final BindingReflectionHintsRegistrar bindings = new BindingReflectionHintsRegistrar();

    static Set<Class<?>> sdkTypes(ClassLoader classLoader) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                return definition.getMetadata().isIndependent() && !definition.getMetadata().isInterface();
            }
        };
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter((reader, factory) -> true);
        Set<Class<?>> found = new LinkedHashSet<>();
        for (String basePackage : PACKAGES) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                found.add(ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader));
            }
        }
        return found;
    }

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        if (!ClassUtils.isPresent(SDK, classLoader)) {
            return;
        }
        bindings.registerReflectionHints(hints.reflection(), sdkTypes(classLoader).toArray(Class<?>[]::new));
    }

}
