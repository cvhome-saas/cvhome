package com.asrevo.cvhome.catalog.errors;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every condition-named exception in the catalog vocabulary, exercised through its own static factory.
 *
 * <p>
 * Same discovery shape as uaa-client's and billing's: the classes are near-identical, so a hand-written test per
 * class is the thing nobody maintains and which therefore never catches the bug the shape invites — a copy-pasted
 * class that kept its neighbour's error code.
 * </p>
 */
class CatalogErrorFactoriesTest {

    private static final String SAMPLE = "sample";

    private static final Map<Class<?>, Supplier<Object>> SAMPLES = Map.ofEntries(
            Map.entry(String.class, () -> SAMPLE),
            Map.entry(Object.class, () -> SAMPLE),
            Map.entry(long.class, () -> 1L),
            Map.entry(Long.class, () -> 1L),
            Map.entry(int.class, () -> 1),
            Map.entry(Integer.class, () -> 1),
            Map.entry(boolean.class, () -> true),
            Map.entry(Class.class, () -> String.class),
            Map.entry(Throwable.class, () -> new IllegalStateException("cause")));

    private static Stream<Method> factories() {
        JavaClasses imported = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.asrevo.cvhome.catalog.errors");
        List<Method> found = new ArrayList<>();
        for (var javaClass : imported) {
            Class<?> type = javaClass.reflect();
            if (!BaseException.class.isAssignableFrom(type) || Modifier.isAbstract(type.getModifiers())) {
                continue;
            }
            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())
                        && type.isAssignableFrom(method.getReturnType())) {
                    found.add(method);
                }
            }
        }
        return found.stream().sorted((a, b) -> (a.getDeclaringClass().getSimpleName() + a.getName())
                .compareTo(b.getDeclaringClass().getSimpleName() + b.getName()));
    }

    private static Object sample(Class<?> type) {
        Supplier<Object> supplier = SAMPLES.get(type);
        if (supplier != null) {
            return supplier.get();
        }
        if (Collection.class.isAssignableFrom(type)) {
            return List.of(SAMPLE);
        }
        throw new IllegalArgumentException(
                String.format("No sample value for factory parameter type %s", type.getName()));
    }

    @Test
    void everyCatalogExceptionIsDiscovered() {
        assertThat(factories().map(m -> m.getDeclaringClass().getSimpleName()).distinct())
                .hasSizeGreaterThanOrEqualTo(10);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("factories")
    void aFactoryBuildsItsOwnTypeAndNamesACatalogCode(Method factory) throws Exception {
        Object[] args = Stream.of(factory.getParameterTypes())
                .map(CatalogErrorFactoriesTest::sample).toArray();

        Object thrown = factory.invoke(null, args);

        assertThat(thrown).isInstanceOf(factory.getDeclaringClass()).isInstanceOf(BaseException.class);
        ErrorPayload payload = ((BaseException) thrown).payload();
        assertThat(payload.errorCode()).isNotNull();
        assertThat(payload.errorCode().category()).isNotNull();
        assertThat(((BaseException) thrown).getMessage()).contains(payload.errorCode().code());
    }

    @Test
    void theVocabularyKeepsItsOwnPrefix() {
        assertThat(CatalogErrors.values())
                .allSatisfy(error -> assertThat(error.code()).startsWith("CATALOG."));
    }
}
