package com.asrevo.cvhome.aot;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ClassUtils;

/**
 * What a native image needs to talk to Stripe, for the services that do (billing, payment).
 *
 * <p>
 * {@code stripe-java} ships no native-image metadata and the GraalVM reachability repository has none for it, and the
 * SDK is reflection all the way down: Gson fills every response model field by field, and every request's
 * {@code *Params} object is turned into form parameters the same way. Nothing fails at build time; the first Stripe
 * call of a native service does.
 * </p>
 *
 * <ul>
 *     <li><strong>Models</strong> — every class under {@code com.stripe.model}. A response is deserialised into the
 *     type the SDK method returns whether or not our code touches it, and a webhook's {@code Event} can carry any of
 *     them, so there is no smaller set that is safe.</li>
 *     <li><strong>Params</strong> — only those our code builds, and whatever they can hold. There are ten thousand
 *     param classes; the roots are found by reading the constant pools of our own classes for references to
 *     {@code com.stripe.param}, then followed through their fields. A new Stripe call is covered by compiling it —
 *     there is no list to forget.</li>
 * </ul>
 *
 * <p>
 * Inert in a service without {@code stripe-java} on its classpath.
 * </p>
 */
public class StripeRuntimeHints implements RuntimeHintsRegistrar {

    static final String STRIPE_CLIENT = "com.stripe.StripeClient";

    /** The response models, and the SDK classes they inherit fields from ({@code ApiResource} lives in net). */
    private static final String[] MODELS = {"classpath*:com/stripe/model/**/*.class", "classpath*:com/stripe/net/*.class"};

    private static final String OUR_CLASSES = "classpath*:com/asrevo/cvhome/**/*.class";

    private static final String STRIPE_PATH = "com/stripe/";

    private static final String PARAM_PREFIX = "com/stripe/param/";

    private static final String STRIPE_PACKAGE = "com.stripe.";

    private static final MemberCategory[] GSON = {MemberCategory.DECLARED_FIELDS,
        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS};

    static Set<String> modelClasses(ClassLoader classLoader) {
        Set<String> names = new LinkedHashSet<>();
        for (String pattern : MODELS) {
            for (Resource resource : resources(classLoader, pattern)) {
                try {
                    String url = resource.getURL().toExternalForm();
                    String path = url.substring(url.lastIndexOf(STRIPE_PATH), url.length() - ".class".length());
                    names.add(path.replace('/', '.'));
                } catch (IOException e) {
                    throw new UncheckedIOException(String.format("Could not locate %s", resource), e);
                }
            }
        }
        return names;
    }

    /**
     * The {@code com.stripe.param} classes our bytecode names — a {@code builder()} call, a field, a parameter.
     */
    static Set<String> referencedParams(ClassLoader classLoader) {
        Set<String> names = new LinkedHashSet<>();
        for (Set<String> references : BytecodeReferences.scan(classLoader, OUR_CLASSES).values()) {
            references.stream().filter(name -> name.startsWith(PARAM_PREFIX))
                    .forEach(name -> names.add(name.replace('/', '.')));
        }
        return names;
    }

    /**
     * The param roots and every Stripe type reachable from them through a field, a generic argument or a supertype.
     */
    static Set<Class<?>> paramClosure(Set<String> roots, ClassLoader classLoader) {
        Set<Class<?>> seen = new LinkedHashSet<>();
        Deque<Class<?>> queue = new ArrayDeque<>();
        roots.forEach(name -> queue.add(ClassUtils.resolveClassName(name, classLoader)));
        while (!queue.isEmpty()) {
            Class<?> type = queue.poll();
            if (!type.getName().startsWith(STRIPE_PACKAGE) || !seen.add(type)) {
                continue;
            }
            if (type.getSuperclass() != null) {
                queue.add(type.getSuperclass());
            }
            for (Field field : type.getDeclaredFields()) {
                collect(field.getGenericType(), queue);
            }
        }
        return seen;
    }

    private static void collect(Type type, Deque<Class<?>> queue) {
        if (type instanceof Class<?> c) {
            queue.add(c.isArray() ? c.getComponentType() : c);
        } else if (type instanceof ParameterizedType p) {
            collect(p.getRawType(), queue);
            for (Type argument : p.getActualTypeArguments()) {
                collect(argument, queue);
            }
        } else if (type instanceof GenericArrayType a) {
            collect(a.getGenericComponentType(), queue);
        } else if (type instanceof WildcardType w) {
            for (Type bound : w.getUpperBounds()) {
                collect(bound, queue);
            }
        }
    }

    private static Resource[] resources(ClassLoader classLoader, String pattern) {
        try {
            return new PathMatchingResourcePatternResolver(classLoader).getResources(pattern);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Could not scan %s", pattern), e);
        }
    }

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        if (!ClassUtils.isPresent(STRIPE_CLIENT, classLoader)) {
            return;
        }
        modelClasses(classLoader).forEach(name -> hints.reflection().registerType(TypeReference.of(name), GSON));
        paramClosure(referencedParams(classLoader), classLoader)
                .forEach(type -> hints.reflection().registerType(type, GSON));
    }

}
