package com.asrevo.cvhome.uaa.errors;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
 * Every condition-named exception in the uaa vocabulary, exercised through its own static factory.
 *
 * <p>
 * There are 46 of these classes and they are near-identical: a protected constructor and one
 * {@code of(...)} that names a {@link UaaErrors} constant. Written out by hand that is 46 copies of the same three
 * assertions, which nobody maintains and which therefore never catch the one bug the shape actually invites — a
 * copy-pasted class that kept the previous file's error code. Discovering the classes instead means a new exception
 * is covered the moment it is added, and cannot quietly point at the wrong code.
 * </p>
 *
 * <p>
 * Discovery is ArchUnit's importer because it is already the repo's tool for asking structural questions, and
 * because a hand-kept list is exactly the thing that goes stale.
 * </p>
 */
class UaaErrorFactoriesTest {

    private static final String PACKAGE = "com.asrevo.cvhome.uaa.errors";
    private static final String SAMPLE = "sample";

    /**
     * A value per parameter type the factories actually take. A map rather than an if-chain so that adding a type is
     * one line and the method stays under the complexity bound.
     */
    private static final Map<Class<?>, Supplier<Object>> SAMPLES = Map.ofEntries(
            Map.entry(String.class, () -> SAMPLE),
            Map.entry(UUID.class, () -> UUID.fromString("00000000-0000-0000-0000-000000000001")),
            Map.entry(long.class, () -> 1L),
            Map.entry(Long.class, () -> 1L),
            Map.entry(int.class, () -> 1),
            Map.entry(Integer.class, () -> 1),
            Map.entry(boolean.class, () -> true),
            Map.entry(Boolean.class, () -> true),
            Map.entry(Duration.class, () -> Duration.ofMinutes(1)),
            Map.entry(Throwable.class, () -> new IllegalStateException("cause")));

    private static Stream<Method> factories() {
        JavaClasses imported = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(PACKAGE);
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
        // Deliberately loud: a factory that grows a parameter type this test cannot build must be handled here rather
        // than silently skipped, which is how a discovery test quietly stops covering anything.
        throw new IllegalArgumentException(
                String.format("No sample value for factory parameter type %s", type.getName()));
    }

    @Test
    void everyExceptionInTheVocabularyIsDiscovered() {
        assertThat(factories()).hasSizeGreaterThanOrEqualTo(46);
        assertThat(factories().map(m -> m.getDeclaringClass().getSimpleName()).distinct())
                .hasSizeGreaterThanOrEqualTo(46);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("factories")
    void aFactoryBuildsItsOwnTypeAndNamesAUaaCode(Method factory) throws Exception {
        Object[] args = Stream.of(factory.getParameterTypes()).map(UaaErrorFactoriesTest::sample).toArray();

        Object thrown = factory.invoke(null, args);

        assertThat(thrown).isInstanceOf(factory.getDeclaringClass()).isInstanceOf(BaseException.class);
        ErrorPayload payload = ((BaseException) thrown).payload();
        assertThat(payload.errorCode())
                .as("%s must name a UaaErrors constant", factory.getDeclaringClass().getSimpleName())
                .isInstanceOf(UaaErrors.class);
        assertThat(payload.errorCode().category()).isNotNull();
        assertThat(payload.params()).isNotNull();
        assertThat(payload.fieldErrors()).isNotNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("factories")
    void aFactorysMessageCarriesItsCodeSoALogLineIdentifiesTheCondition(Method factory) throws Exception {
        Object[] args = Stream.of(factory.getParameterTypes()).map(UaaErrorFactoriesTest::sample).toArray();

        BaseException thrown = (BaseException) factory.invoke(null, args);

        assertThat(thrown.getMessage()).isEqualTo(thrown.payload().toMessage())
                .contains(thrown.payload().errorCode().code());
    }
}
