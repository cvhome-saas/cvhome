package com.asrevo.cvhome.billing.errors;

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

import com.asrevo.cvhome.billing.api.errors.BillingApiErrors;
import com.asrevo.cvhome.billing.commons.errors.BillingErrors;
import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every condition-named exception in the billing vocabulary, exercised through its own static factory.
 *
 * <p>
 * Same shape as uaa-client's: nineteen near-identical classes, each a protected constructor and one factory naming
 * a code. Discovered rather than listed, so a new one is covered the moment it is added and cannot quietly keep a
 * copy-pasted neighbour's error code.
 * </p>
 *
 * <p>
 * The billing-specific assertion is the last one. {@code BillingApiUnavailableException.wrapping} is the caller-side
 * translation of a failure that reached billing from Stripe, and it must report <em>billing's</em> status rather
 * than the provider's — the provider's status describes a conversation the caller was never part of, and travels in
 * the params instead. That is the same rule ProblemDetailFactory enforces on the rendering side.
 * </p>
 */
class BillingErrorFactoriesTest {

    private static final String SAMPLE = "sample";
    private static final String[] PACKAGES = {
        "com.asrevo.cvhome.billing.commons.errors", "com.asrevo.cvhome.billing.api.errors",
    };

    private static final Map<Class<?>, Supplier<Object>> SAMPLES = Map.ofEntries(
            Map.entry(String.class, () -> SAMPLE),
            Map.entry(Object.class, () -> SAMPLE),
            Map.entry(long.class, () -> 1L),
            Map.entry(Long.class, () -> 1L),
            Map.entry(int.class, () -> 1),
            Map.entry(Integer.class, () -> 1),
            Map.entry(boolean.class, () -> true),
            Map.entry(Throwable.class, () -> new IllegalStateException("cause")),
            Map.entry(Class.class, () -> String.class),
            Map.entry(RemoteErrorContext.class, () -> new RemoteErrorContext("BILLING.X", "detail", Map.of(),
                    List.of(), "billing", 503, "trace", null)));

    private static Stream<Method> factories() {
        JavaClasses imported = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(PACKAGES);
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

    private static Object invoke(Method factory) throws Exception {
        Object[] args = Stream.of(factory.getParameterTypes())
                .map(BillingErrorFactoriesTest::sample).toArray();
        return factory.invoke(null, args);
    }

    @Test
    void everyBillingExceptionIsDiscovered() {
        assertThat(factories().map(m -> m.getDeclaringClass().getSimpleName()).distinct())
                .hasSizeGreaterThanOrEqualTo(15);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("factories")
    void aFactoryBuildsItsOwnTypeAndNamesABillingCode(Method factory) throws Exception {
        Object thrown = invoke(factory);

        assertThat(thrown).isInstanceOf(factory.getDeclaringClass()).isInstanceOf(BaseException.class);
        ErrorPayload payload = ((BaseException) thrown).payload();
        assertThat(payload.errorCode()).isNotNull();
        assertThat(payload.errorCode().category()).isNotNull();
        assertThat(((BaseException) thrown).getMessage()).contains(payload.errorCode().code());
    }

    @Test
    void theTwoVocabulariesKeepTheirOwnPrefixes() {
        assertThat(BillingErrors.values())
                .allSatisfy(error -> assertThat(error.code()).startsWith("BILLING."));
        assertThat(BillingApiErrors.CATALOG).isNotNull();
    }

    @Test
    void wrappingAProviderFailureReportsBillingsStatusRatherThanStripes() {
        ExternalProviderException fromStripe = ExternalProviderException
                .of(BillingErrors.PLAN_NOT_FOUND, TestProviderException::new)
                .provider("stripe").providerCode("resource_missing").providerStatus(402).build();

        RemoteServiceException wrapped = com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException
                .wrapping(fromStripe);

        assertThat(wrapped.remoteStatus())
                .isEqualTo(BillingErrors.PLAN_NOT_FOUND.category().httpStatus())
                .isNotEqualTo(402);
        assertThat(wrapped.remoteCode()).isEqualTo(BillingErrors.PLAN_NOT_FOUND.code());
    }

    @Test
    void wrappingAPeerFailureKeepsThatPeersStatus() {
        RemoteServiceException fromPeer = RemoteServiceException
                .of(BillingErrors.PLAN_NOT_FOUND, TestRemoteException::new)
                .remoteService("tenancy").remoteCode("TENANCY.X").remoteStatus(409).build();

        RemoteServiceException wrapped = com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException
                .wrapping(fromPeer);

        assertThat(wrapped.remoteStatus()).isEqualTo(409);
    }

    @Test
    void wrappingAPlainFailureCarriesNoCodeOfItsOwn() {
        RemoteServiceException wrapped = com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException
                .wrapping(new IllegalStateException("socket closed"));

        assertThat(wrapped.remoteCode()).isNull();
        assertThat(wrapped.remoteStatus()).isZero();
    }

    private static final class TestProviderException extends ExternalProviderException {
        private TestProviderException(ErrorPayload payload, Throwable cause, String provider, String code,
                                      int status) {
            super(payload, cause, provider, code, status);
        }
    }

    private static final class TestRemoteException extends RemoteServiceException {
        private TestRemoteException(ErrorPayload payload, Throwable cause, String service, String code, int status) {
            super(payload, cause, service, code, status);
        }
    }
}
