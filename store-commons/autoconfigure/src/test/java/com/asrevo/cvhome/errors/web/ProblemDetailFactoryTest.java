package com.asrevo.cvhome.errors.web;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.errors.ExternalProviderException;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.errors.RemoteServiceException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The single place a response body is built, and the one distinction it exists to keep.
 *
 * <p>
 * {@link ProblemDetailFactory#remote} re-emits a peer cvhome service's {@code code} as this response's code, because
 * that is what actually went wrong and a further hop can rebuild the typed exception from it.
 * {@link ProblemDetailFactory#external} must do the opposite: a client that received {@code code: "card_declined"}
 * would be reading Stripe's catalogue as though it were ours, and a caller's {@code RemoteErrorCatalog} — which keys
 * on our codes — would stop recognising the failure at all. Re-emitting a provider's code as ours is on the
 * reject-on-sight list, and this is the test that makes it fail rather than get reviewed.
 * </p>
 */
class ProblemDetailFactoryTest {

    private static final String TRACE = "trace-01";
    private static final String PROVIDER = "stripe";
    private static final String PROVIDER_CODE = "card_declined";
    private static final String PEER = "payment";
    private static final String PEER_CODE = "PAYMENT.INITIATE.FAILED";
    private static final String OUR_CODE = "CHECKOUT.ORDER.NOT_PLACEABLE";
    private static final String PRODUCT_CODE = "CATALOG.PRODUCT.NOT_FOUND";
    private static final String BLANK = "  ";
    private static final String DETAIL = "no such product";
    private static final String SKU = "sku";
    private static final String FROM_MDC = "from-mdc";

    private final ProblemDetailFactory factory =
            new ProblemDetailFactory(new ErrorHandlingProperties("https://errors.example.com", false));

    private enum TestErrors implements ErrorCode {
        NOT_FOUND(PRODUCT_CODE, ErrorCategory.NOT_FOUND),
        UPSTREAM(OUR_CODE, ErrorCategory.REMOTE_SERVICE),
        TEAPOT("ODD.NO_SUCH.STATUS", () -> 799);

        private final String code;
        private final ErrorCategory category;
        private final int overrideStatus;

        TestErrors(String code, ErrorCategory category) {
            this.code = code;
            this.category = category;
            this.overrideStatus = 0;
        }

        TestErrors(String code, java.util.function.IntSupplier status) {
            this.code = code;
            this.category = ErrorCategory.INTERNAL;
            this.overrideStatus = status.getAsInt();
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public ErrorCategory category() {
            return category;
        }

        int overrideStatus() {
            return overrideStatus;
        }
    }

    private static final class TestRemoteException extends RemoteServiceException {
        private TestRemoteException(com.asrevo.cvhome.errors.ErrorPayload payload, Throwable cause, String service,
                                    String code, int status) {
            super(payload, cause, service, code, status);
        }
    }

    private static final class TestProviderException extends ExternalProviderException {
        private TestProviderException(com.asrevo.cvhome.errors.ErrorPayload payload, Throwable cause, String provider,
                                      String code, int status) {
            super(payload, cause, provider, code, status);
        }
    }

    @Test
    void aCodeBecomesTheTitleTheTypeUriAndTheCodeProperty() {
        ProblemDetail problem = factory.create(TestErrors.NOT_FOUND, null, Map.of(), List.of(), TRACE);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo(PRODUCT_CODE);
        assertThat(problem.getType()).hasToString("https://errors.example.com/catalog/product/not-found");
        assertThat(problem.getProperties())
                .containsEntry(ProblemDetailFactory.CODE, PRODUCT_CODE)
                .containsEntry(ProblemDetailFactory.CATEGORY, ErrorCategory.NOT_FOUND.name())
                .containsEntry(ProblemDetailFactory.TRACE_ID, TRACE);
    }

    @Test
    void underscoresBecomeHyphensSoTheTypeUriIsDocumentationShaped() {
        ProblemDetail problem = factory.create(TestErrors.UPSTREAM, null, Map.of(), List.of(), TRACE);
        assertThat(problem.getType()).hasToString("https://errors.example.com/checkout/order/not-placeable");
    }

    @Test
    void emptyParamsAndFieldErrorsAreLeftOffTheBodyEntirely() {
        ProblemDetail problem = factory.create(TestErrors.NOT_FOUND, BLANK, Map.of(), List.of(), TRACE);

        assertThat(problem.getDetail()).isNull();
        assertThat(problem.getProperties())
                .doesNotContainKey(ProblemDetailFactory.PARAMS)
                .doesNotContainKey(ProblemDetailFactory.FIELD_ERRORS);
    }

    @Test
    void paramsAndFieldErrorsAreCarriedWhenPresent() {
        ProblemDetail problem = factory.create(TestErrors.NOT_FOUND, DETAIL, Map.of(SKU, "ABC"),
                List.of(FieldError.of(SKU, TestErrors.NOT_FOUND, "unknown")), TRACE);

        assertThat(problem.getDetail()).isEqualTo(DETAIL);
        assertThat(problem.getProperties()).containsKey(ProblemDetailFactory.PARAMS)
                .containsKey(ProblemDetailFactory.FIELD_ERRORS);
    }

    @Test
    void aCategoryWhoseStatusIsNotARealHttpStatusFallsBackToFiveHundred() {
        ProblemDetail problem = factory.create(TestErrors.TEAPOT, null, null, null, TRACE);
        assertThat(TestErrors.TEAPOT.overrideStatus()).isEqualTo(799);
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void aPeerServicesCodeBecomesOursBecauseItIsWhatActuallyWentWrong() {
        TestRemoteException error = RemoteServiceException.of(TestErrors.UPSTREAM, TestRemoteException::new)
                .remoteService(PEER).remoteCode(PEER_CODE).remoteStatus(422).build();

        ProblemDetail problem = factory.remote(error, HttpStatus.BAD_GATEWAY, TRACE);

        assertThat(problem.getProperties())
                .containsEntry(ProblemDetailFactory.CODE, PEER_CODE)
                .containsEntry(ProblemDetailFactory.REMOTE_SERVICE, PEER)
                .containsEntry(ProblemDetailFactory.REMOTE_STATUS, 422);
        assertThat(problem.getTitle()).isEqualTo(PEER_CODE);
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
    }

    @Test
    void aPeerThatNamedNoCodeLeavesOurOwnStanding() {
        TestRemoteException error = RemoteServiceException.of(TestErrors.UPSTREAM, TestRemoteException::new)
                .remoteService(PEER).build();

        ProblemDetail problem = factory.remote(error, HttpStatus.BAD_GATEWAY, TRACE);

        assertThat(problem.getProperties()).containsEntry(ProblemDetailFactory.CODE, OUR_CODE)
                .doesNotContainKey(ProblemDetailFactory.REMOTE_STATUS);
    }

    @Test
    void aProvidersCodeIsReportedAlongsideOursAndNeverAsOurs() {
        TestProviderException error = ExternalProviderException.of(TestErrors.UPSTREAM, TestProviderException::new)
                .provider(PROVIDER).providerCode(PROVIDER_CODE).providerStatus(402).build();

        ProblemDetail problem = factory.external(error, TRACE);

        assertThat(problem.getProperties())
                .containsEntry(ProblemDetailFactory.CODE, OUR_CODE)
                .containsEntry(ProblemDetailFactory.PROVIDER, PROVIDER)
                .containsEntry(ProblemDetailFactory.PROVIDER_CODE, PROVIDER_CODE)
                .containsEntry(ProblemDetailFactory.PROVIDER_STATUS, 402);
        assertThat(problem.getTitle()).isEqualTo(OUR_CODE);
        assertThat(problem.getStatus()).isEqualTo(ErrorCategory.REMOTE_SERVICE.httpStatus());
    }

    @Test
    void aProviderThatNamedNothingAddsNoProviderPropertiesAtAll() {
        TestProviderException error = ExternalProviderException.of(TestErrors.UPSTREAM, TestProviderException::new)
                .build();

        ProblemDetail problem = factory.external(error, TRACE);

        assertThat(problem.getProperties())
                .doesNotContainKey(ProblemDetailFactory.PROVIDER_CODE)
                .doesNotContainKey(ProblemDetailFactory.PROVIDER_STATUS);
    }

    @Test
    void withStatusOverridesTheStatusTheCategoryImplied() {
        ProblemDetail problem = factory.create(TestErrors.NOT_FOUND, null, Map.of(), List.of(), TRACE);
        assertThat(factory.withStatus(problem, HttpStatus.GONE).getStatus()).isEqualTo(HttpStatus.GONE.value());
    }

    @Test
    void theTraceIdComesFromTheMdcSoAReportedIdLeadsToTheStackTrace() {
        MDC.put(ProblemDetailFactory.TRACE_ID, FROM_MDC);
        try {
            assertThat(factory.traceId()).isEqualTo(FROM_MDC);
        } finally {
            MDC.remove(ProblemDetailFactory.TRACE_ID);
        }
    }

    @Test
    void withoutAnMdcTraceAShortOneIsMinted() {
        MDC.remove(ProblemDetailFactory.TRACE_ID);
        assertThat(factory.traceId()).hasSize(8).isNotEqualTo(factory.traceId());
    }

    @Test
    void debugDetailFollowsTheConfiguredProperty() {
        assertThat(factory.includeDebugDetail()).isFalse();
        assertThat(new ProblemDetailFactory(new ErrorHandlingProperties(null, true)).includeDebugDetail()).isTrue();
    }

    @Test
    void aBlankTypeBaseUriFallsBackToTheDefaultRatherThanBuildingARelativeUri() {
        ProblemDetail problem = new ProblemDetailFactory(new ErrorHandlingProperties(BLANK, false))
                .create(TestErrors.NOT_FOUND, null, Map.of(), List.of(), TRACE);
        assertThat(problem.getType()).hasToString("https://errors.asrevo.com/catalog/product/not-found");
    }
}
