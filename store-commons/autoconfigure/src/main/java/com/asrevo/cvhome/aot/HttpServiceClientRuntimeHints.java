package com.asrevo.cvhome.aot;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * What a native image needs to call a peer service through its {@code -external-api} interface.
 *
 * <p>
 * {@code WebClientsUtils} turns each interface into two JDK proxies at run time — Spring's
 * {@code HttpServiceProxyFactory} proxy, and the typed-error wrapper around it that re-throws a remote failure as the
 * checked exception the method declares — and a native image can only create a proxy class it was told about when
 * it was built. The wrapper also invokes the interface's methods reflectively, and the request and response bodies
 * are bound by Jackson.
 * </p>
 *
 * <p>
 * Found by scanning rather than listed: several clients are built per pod at run time ({@code buildClient(pod, …)})
 * instead of being beans, so bean-driven AOT processing would not see them, and a hand-kept list is exactly the kind
 * of copy that drifts. Every interface under {@value #BASE_PACKAGE} with an {@code @HttpExchange} method qualifies;
 * the ones only ever implemented by a controller cost a few unused proxy entries.
 * </p>
 */
public class HttpServiceClientRuntimeHints implements RuntimeHintsRegistrar {

    static final String BASE_PACKAGE = "com.asrevo.cvhome";

    static Set<Class<?>> clientInterfaces(ClassLoader classLoader) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                return definition.getMetadata().isInterface() && definition.getMetadata().isIndependent();
            }
        };
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter((reader, factory) ->
                reader.getAnnotationMetadata().hasAnnotatedMethods(HttpExchange.class.getName()));
        Set<Class<?>> found = new LinkedHashSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            found.add(ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader));
        }
        return found;
    }

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        Set<Class<?>> clients = clientInterfaces(classLoader);
        for (Class<?> client : clients) {
            hints.proxies().registerJdkProxy(client);
            hints.proxies().registerJdkProxy(AopProxyUtils.completeJdkProxyInterfaces(client));
        }
        // @HttpExchange is @Reflective: Spring's own processor registers each method for invocation and its request and
        // response bodies for binding, exactly as it would for an interface it had registered itself.
        new ReflectiveRuntimeHintsRegistrar().registerRuntimeHints(hints, clients.toArray(Class<?>[]::new));
    }

}
