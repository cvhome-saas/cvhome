package com.asrevo.cvhome.aot;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

/**
 * What Hibernate does reflectively while it builds the session factory, beyond what Spring's JPA AOT support covers
 * (the entities and their {@code @Convert} converters). Each one stopped a native service at start:
 *
 * <ul>
 *     <li><strong>Custom column types.</strong> {@code @Type(TsVectorType.class)} names a {@code UserType} Hibernate
 *     constructs itself — "Failed to instantiate TsVectorType: No default constructor found" (catalog).</li>
 *     <li><strong>Identifier arrays.</strong> Every entity gets a multi-id loader that binds its ids as one SQL array,
 *     and builds that array reflectively from the id's type — "Cannot reflectively instantiate the array class
 *     'java.util.UUID[]'" (uaa, cua).</li>
 * </ul>
 *
 * <p>
 * Both found by scanning {@value #BASE_PACKAGE}, so a new type or entity is covered by existing. Inert in a service
 * without Hibernate.
 * </p>
 */
public class HibernateRuntimeHints implements RuntimeHintsRegistrar {

    static final String BASE_PACKAGE = "com.asrevo.cvhome";

    static final String USER_TYPE = "org.hibernate.usertype.UserType";

    static final String ENTITY = "jakarta.persistence.Entity";

    private static final Set<String> ID_ANNOTATIONS = Set.of("jakarta.persistence.Id", "jakarta.persistence.EmbeddedId");

    static Set<Class<?>> scan(ClassLoader classLoader, TypeFilter filter) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter(filter);
        Set<Class<?>> found = new LinkedHashSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            found.add(ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader));
        }
        return found;
    }

    /**
     * The types of the fields an entity is identified by, inherited ones included ({@code BaseEntity} declares most).
     */
    static Set<Class<?>> identifierTypes(Class<?> entity) {
        Set<Class<?>> types = new LinkedHashSet<>();
        for (Class<?> type = entity; type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                for (Annotation annotation : field.getDeclaredAnnotations()) {
                    if (ID_ANNOTATIONS.contains(annotation.annotationType().getName())) {
                        types.add(field.getType());
                    }
                }
            }
        }
        return types;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        if (!ClassUtils.isPresent(USER_TYPE, classLoader)) {
            return;
        }
        Class<?> userType = ClassUtils.resolveClassName(USER_TYPE, classLoader);
        for (Class<?> type : scan(classLoader, new AssignableTypeFilter(userType))) {
            hints.reflection().registerType(type, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        }
        Class<? extends Annotation> entity = (Class<? extends Annotation>) ClassUtils.resolveClassName(ENTITY, classLoader);
        for (Class<?> type : scan(classLoader, new AnnotationTypeFilter(entity))) {
            identifierTypes(type).forEach(id -> hints.reflection().registerType(id.arrayType()));
        }
    }

}
