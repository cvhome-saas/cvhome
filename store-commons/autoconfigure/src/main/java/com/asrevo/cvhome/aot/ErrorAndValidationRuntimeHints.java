package com.asrevo.cvhome.aot;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

/**
 * The two halves of a refused request a native image cannot do without being told: our validators, and the error
 * body that reports them.
 *
 * <ul>
 *     <li><strong>Validators.</strong> Hibernate Validator constructs each {@code ConstraintValidator} reflectively and
 *     reads its constraint annotation's attributes the same way. Spring's AOT support registers them from the bean
 *     classes it inspects — and skips a class whose method constraints Hibernate Validator rejects (HV000151, which
 *     tenancy's and checkout's external APIs trip), so tenancy's signup failed natively: "Failed to instantiate
 *     StrongPasswordValidator: No default constructor found".</li>
 *     <li><strong>The error contract.</strong> {@code ProblemDetailFactory} puts {@code FieldError} records into the
 *     body's properties, which no controller signature names, so Jackson could not read their components: "Record
 *     components not available for record class FieldError".</li>
 * </ul>
 *
 * <p>
 * Both found by scanning {@value #BASE_PACKAGE}. Inert where Bean Validation is absent (the validator half).
 * </p>
 */
public class ErrorAndValidationRuntimeHints implements RuntimeHintsRegistrar {

    static final String BASE_PACKAGE = "com.asrevo.cvhome";

    static final String ERRORS_PACKAGE = "com.asrevo.cvhome.errors";

    static final String CONSTRAINT_VALIDATOR = "jakarta.validation.ConstraintValidator";

    private final BindingReflectionHintsRegistrar bindings = new BindingReflectionHintsRegistrar();

    static Set<Class<?>> scan(ClassLoader classLoader, String basePackage, TypeFilter filter) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                return definition.getMetadata().isIndependent() && definition.getMetadata().isConcrete();
            }
        };
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter(filter);
        Set<Class<?>> found = new LinkedHashSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
            found.add(ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader));
        }
        return found;
    }

    /** The error body's value types: the records of the error contract, not its exceptions. */
    static Set<Class<?>> errorRecords(ClassLoader classLoader) {
        return scan(classLoader, ERRORS_PACKAGE, (reader, factory) -> "java.lang.Record"
                .equals(reader.getClassMetadata().getSuperClassName()));
    }

    /** The constraint annotation a validator implements, from {@code ConstraintValidator<A, T>}. */
    static Class<?> constraintOf(Class<?> validator, Class<?> constraintValidator) {
        for (Class<?> type = validator; type != null; type = type.getSuperclass()) {
            for (Type implemented : type.getGenericInterfaces()) {
                if (implemented instanceof ParameterizedType parameterized
                        && parameterized.getRawType() == constraintValidator
                        && parameterized.getActualTypeArguments()[0] instanceof Class<?> annotation) {
                    return annotation;
                }
            }
        }
        return null;
    }

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        bindings.registerReflectionHints(hints.reflection(), errorRecords(classLoader).toArray(Class<?>[]::new));
        if (!ClassUtils.isPresent(CONSTRAINT_VALIDATOR, classLoader)) {
            return;
        }
        Class<?> constraintValidator = ClassUtils.resolveClassName(CONSTRAINT_VALIDATOR, classLoader);
        for (Class<?> validator : scan(classLoader, BASE_PACKAGE, new AssignableTypeFilter(constraintValidator))) {
            hints.reflection().registerType(validator, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            Class<?> constraint = constraintOf(validator, constraintValidator);
            if (constraint != null) {
                hints.reflection().registerType(constraint, MemberCategory.INVOKE_PUBLIC_METHODS);
            }
        }
    }

}
