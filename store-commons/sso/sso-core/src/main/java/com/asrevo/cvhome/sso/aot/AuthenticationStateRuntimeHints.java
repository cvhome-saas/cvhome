package com.asrevo.cvhome.sso.aot;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Lets a native uaa or cua persist an authentication and read it back — in a session, and in an authorization row.
 *
 * <p>
 * Both keep sessions in JDBC ({@code spring-session-jdbc}) as Java-serialized attributes — the security context,
 * the request saved across a login, a brokered login's pending link — and a native image can only (de)serialize a
 * class it was told about. The first native sign-in answered 500: "SerializationConstructorAccessor class not found
 * for declaringClass: FactorGrantedAuthority", a type Spring Security 7 adds to every authenticated context and
 * nothing registers.
 * </p>
 *
 * <p>
 * What a session holds depends on the flow — a password login, a brokered social login, an impersonation — and on the
 * Spring Security release, so no traced list stays right. Registered instead: every {@code Serializable} class of
 * Spring Security and of this module, found by scanning, and the JDK value and collection types they are built from
 * ({@code AuthenticationStateRuntimeHintsTest} serializes real session contents and fails on any class it writes that
 * is not here).
 * </p>
 */
public class AuthenticationStateRuntimeHints implements RuntimeHintsRegistrar {

    static final String SECURITY_PACKAGE = "org.springframework.security";

    /** Where the serializable types of a session come from: the framework's and ours. */
    static final List<String> PACKAGES = List.of(SECURITY_PACKAGE, "com.asrevo.cvhome.sso");

    /** The JDK types those objects are made of; stable across releases, so named. */
    static final List<String> JDK_TYPES = List.of(
            "java.lang.Boolean", "java.lang.Integer", "java.lang.Long", "java.lang.Number", "java.lang.String",
            "java.lang.String$CaseInsensitiveComparator", "java.lang.Enum",
            "java.net.URI", "java.net.URL", "java.time.Instant", "java.time.Ser", "java.util.Locale", "java.util.UUID",
            "java.util.ArrayList", "java.util.LinkedList", "java.util.Arrays$ArrayList",
            "java.util.HashMap", "java.util.LinkedHashMap", "java.util.TreeMap",
            "java.util.HashSet", "java.util.LinkedHashSet", "java.util.TreeSet",
            "java.util.Collections$UnmodifiableCollection", "java.util.Collections$UnmodifiableList",
            "java.util.Collections$UnmodifiableRandomAccessList", "java.util.Collections$UnmodifiableSet",
            "java.util.Collections$UnmodifiableSortedSet", "java.util.Collections$UnmodifiableMap",
            "java.util.Collections$SingletonList", "java.util.Collections$SingletonSet",
            "java.util.Collections$SingletonMap", "java.util.Collections$EmptyList", "java.util.Collections$EmptySet",
            "java.util.Collections$EmptyMap", "java.util.CollSer");

    /** What Jackson needs of a type it builds through a mixin's creator. */
    private static final MemberCategory[] JSON = {MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
        MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS};

    /** Spring Security's Jackson mixins: {@code FactorGrantedAuthorityMixin}, {@code OAuth2UserAuthorityMixin} ... */
    static Set<Class<?>> mixins(ClassLoader classLoader) {
        return scan(classLoader, List.of(SECURITY_PACKAGE),
                (reader, factory) -> reader.getClassMetadata().getClassName().endsWith("Mixin"));
    }

    static Set<Class<?>> serializableTypes(ClassLoader classLoader) {
        return scan(classLoader, PACKAGES, new AssignableTypeFilter(Serializable.class));
    }

    private static Set<Class<?>> scan(ClassLoader classLoader, List<String> packages,
                                      TypeFilter filter) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                // Abstract classes too: a superclass's fields are part of the serialized form, and mixins are abstract.
                return definition.getMetadata().isIndependent() && !definition.getMetadata().isInterface();
            }
        };
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter(filter);
        Set<Class<?>> found = new LinkedHashSet<>();
        for (String basePackage : packages) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                try {
                    found.add(Class.forName(candidate.getBeanClassName(), false, classLoader));
                } catch (ClassNotFoundException | LinkageError e) {
                    // An optional integration whose dependency is absent (LDAP, SAML, CAS): nothing to serialize.
                }
            }
        }
        return found;
    }

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        Set<Class<?>> state = serializableTypes(classLoader);
        state.forEach(type -> hints.serialization().registerType(TypeReference.of(type)));
        JDK_TYPES.forEach(type -> hints.serialization().registerType(TypeReference.of(type)));
        state.forEach(type -> hints.reflection().registerType(type, JSON));
        mixins(classLoader).forEach(type -> hints.reflection().registerType(type, JSON));
    }

}
