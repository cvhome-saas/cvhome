package com.asrevo.cvhome.aot;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * What the transactional outbox (namastack) does reflectively, for the services that use it. The library ships no
 * native-image support, and each of these failed a native service at run time rather than at build time:
 *
 * <ul>
 *     <li><strong>Its own scheduled work.</strong> The heartbeat and the record poller are handed to Spring's
 *     {@code TaskScheduler} as a {@code ScheduledMethodRunnable} over a method the library looks up by name, which
 *     Spring then invokes reflectively — "Cannot reflectively invoke method
 *     OutboxInstanceRegistry.performHeartbeatAndCleanup()" (billing). No annotation marks them, so the library
 *     classes are found by their bytecode referring to {@code ScheduledMethodRunnable}, and their public methods
 *     registered.</li>
 *     <li><strong>Our handlers.</strong> Every {@code @OutboxHandler} method is found and invoked reflectively,
 *     and its payload is written to the outbox table as JSON and read back into the parameter's type.</li>
 *     <li><strong>Our events.</strong> An {@code @OutboxEvent(key = "#this.userId()")} is evaluated with SpEL against
 *     the event, which calls its accessors reflectively — "Method userId() cannot be found on type
 *     UserCreatedEvent" (uaa) — and the event is written as JSON.</li>
 * </ul>
 *
 * <p>
 * Both found by scanning — the library's bytecode for the first, {@value #BASE_PACKAGE} for the second — so a new
 * handler is covered by existing. Inert in a service without the outbox.
 * </p>
 */
public class OutboxRuntimeHints implements RuntimeHintsRegistrar {

    static final String BASE_PACKAGE = "com.asrevo.cvhome";

    static final String OUTBOX_HANDLER = "io.namastack.outbox.annotation.OutboxHandler";

    static final String OUTBOX_EVENT = "io.namastack.outbox.annotation.OutboxEvent";

    /** Every class file of the library, read for references to {@value #SCHEDULED_METHOD_RUNNABLE}. */
    static final String OUTBOX_CLASSES = "classpath*:io/namastack/outbox/**/*.class";

    static final String SCHEDULED_METHOD_RUNNABLE = "org/springframework/scheduling/support/ScheduledMethodRunnable";

    private final BindingReflectionHintsRegistrar bindings = new BindingReflectionHintsRegistrar();

    /**
     * Classes under {@code basePackage} with at least one method carrying {@code annotation}, directly or as a
     * meta-annotation.
     */
    static Set<Class<?>> declaringClasses(ClassLoader classLoader, String basePackage, String annotation) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                return definition.getMetadata().isIndependent();
            }
        };
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter((reader, factory) -> reader.getAnnotationMetadata().hasAnnotatedMethods(annotation));
        Set<Class<?>> found = new LinkedHashSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
            found.add(ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader));
        }
        return found;
    }

    /** Classes under {@value #BASE_PACKAGE} carrying {@code annotation} on the type. */
    static Set<Class<?>> annotatedClasses(ClassLoader classLoader, String annotation) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                return definition.getMetadata().isIndependent();
            }
        };
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter((reader, factory) -> reader.getAnnotationMetadata().hasAnnotation(annotation));
        Set<Class<?>> found = new LinkedHashSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            found.add(ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader));
        }
        return found;
    }

    static Set<Method> annotatedMethods(Class<?> type, Class<? extends Annotation> annotation) {
        Set<Method> methods = new LinkedHashSet<>();
        ReflectionUtils.doWithMethods(type, method -> {
            if (MergedAnnotations.from(method).isPresent(annotation)) {
                methods.add(method);
            }
        });
        return methods;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        if (!ClassUtils.isPresent(OUTBOX_HANDLER, classLoader)) {
            return;
        }
        BytecodeReferences.scan(classLoader, OUTBOX_CLASSES).forEach((type, references) -> {
            if (references.contains(SCHEDULED_METHOD_RUNNABLE)) {
                hints.reflection().registerType(TypeReference.of(type), MemberCategory.INVOKE_PUBLIC_METHODS);
            }
        });
        for (Class<?> event : annotatedClasses(classLoader, OUTBOX_EVENT)) {
            hints.reflection().registerType(event, MemberCategory.INVOKE_PUBLIC_METHODS);
            bindings.registerReflectionHints(hints.reflection(), event);
        }
        Class<? extends Annotation> handler =
                (Class<? extends Annotation>) ClassUtils.resolveClassName(OUTBOX_HANDLER, classLoader);
        for (Class<?> type : declaringClasses(classLoader, BASE_PACKAGE, OUTBOX_HANDLER)) {
            for (Method method : annotatedMethods(type, handler)) {
                hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
                bindings.registerReflectionHints(hints.reflection(), method.getGenericParameterTypes());
            }
        }
    }

}
