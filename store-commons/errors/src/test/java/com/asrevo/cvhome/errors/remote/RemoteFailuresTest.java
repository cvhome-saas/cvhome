package com.asrevo.cvhome.errors.remote;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UnmappedRemoteFailureException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Describing a failed call to another cvhome service, which has to work when nothing else did.
 *
 * <p>
 * Everything here runs after something has already gone wrong, so the governing rule is that a failed call must never
 * fail again while being described. A proxy's HTML error page, a JSON document of some entirely different shape, a
 * number where text was expected — each has to produce a usable exception rather than a second stack trace that
 * hides the first.
 * </p>
 */
class RemoteFailuresTest {

    private static final String CATALOG_SERVICE = "catalog";

    private static final String PATH = "/api/v1/products/1";

    private static final String SERVICE_KEY = "service";

    private static final String PATH_KEY = "path";

    private static final String REMOTE_STATUS_KEY = "remoteStatus";

    private static final String CODE_KEY = "code";

    private static final String DETAIL_KEY = "detail";

    private static final String PARAMS_KEY = "params";

    private static final String FIELD_ERRORS_KEY = "fieldErrors";

    private static final String NOT_FOUND_CODE = "CATALOG.PRODUCT.NOT_FOUND";

    private static final int NOT_FOUND = 404;

    private static final String NO_SUCH_PRODUCT = "No such product";

    private static final String TRACE_ID = "abc12345";

    private static final String NOT_A_MAP = "not a map";

    private static final String SKU_KEY = "sku";

    private static final String SKU = "SKU-1";

    private static final String INVENTORY = "inventory";

    private static final String FIELD_KEY = "field";

    private static final String NAME = "name";

    private static final String NOT_BLANK = "NOT_BLANK";

    private static final String MESSAGE_KEY = "message";

    private static final String MUST_NOT_BE_BLANK = "must not be blank";

    private static final String MAX_KEY = "max";

    private static final String CONNECTION_REFUSED = "connection refused";

    private static RemoteErrorContext contextOf(Map<String, Object> problem, int status) {
        return RemoteFailures.contextOf(problem, CATALOG_SERVICE, PATH, status, null);
    }

    @Nested
    class ReadingTheProblemDocument {

        @Test
        void aBodyThatCouldNotBeDecodedStillProducesAUsableContext() {
            RemoteErrorContext context = contextOf(null, 502);

            assertThat(context.code()).isNull();
            assertThat(context.detail()).isNull();
            assertThat(context.service()).isEqualTo(CATALOG_SERVICE);
            assertThat(context.status()).isEqualTo(502);
            assertThat(context.params()).containsEntry(SERVICE_KEY, CATALOG_SERVICE).containsEntry(PATH_KEY, PATH);
        }

        @Test
        void theCodeDetailAndTraceIdAreReadFromTheDocument() {
            RemoteErrorContext context = contextOf(Map.of(CODE_KEY, NOT_FOUND_CODE, DETAIL_KEY, NO_SUCH_PRODUCT,
                    "traceId", TRACE_ID), NOT_FOUND);

            assertThat(context.code()).isEqualTo(NOT_FOUND_CODE);
            assertThat(context.detail()).isEqualTo(NO_SUCH_PRODUCT);
            assertThat(context.traceId()).isEqualTo(TRACE_ID);
        }

        @Test
        void aScalarOfTheWrongTypeIsCoercedRatherThanRejected() {
            RemoteErrorContext context = contextOf(Map.of(CODE_KEY, 42, DETAIL_KEY, true), NOT_FOUND);

            assertThat(context.code()).isEqualTo("42");
            assertThat(context.detail()).isEqualTo("true");
        }

        @Test
        void aDocumentOfAnEntirelyDifferentShapeIsIgnoredFieldByField() {
            RemoteErrorContext context = contextOf(
                    Map.of(PARAMS_KEY, NOT_A_MAP, FIELD_ERRORS_KEY, "not a list"), NOT_FOUND);

            assertThat(context.fieldErrors()).isEmpty();
            assertThat(context.params()).containsOnlyKeys(SERVICE_KEY, PATH_KEY, REMOTE_STATUS_KEY);
        }

        @Test
        void theRemotesOwnParamsAreCarriedThrough() {
            RemoteErrorContext context = contextOf(Map.of(PARAMS_KEY, Map.of(SKU_KEY, SKU)), NOT_FOUND);

            assertThat(context.params()).containsEntry(SKU_KEY, SKU);
        }

        /**
         * The caller fills in the service, path and status it knows; a remote that named them itself was more
         * specific, so its answer has to survive.
         */
        @Test
        void whereTheRemoteNamedTheServiceItselfItsAnswerWins() {
            RemoteErrorContext context = contextOf(
                    Map.of(PARAMS_KEY, Map.of(SERVICE_KEY, INVENTORY, REMOTE_STATUS_KEY, 409)), NOT_FOUND);

            assertThat(context.params()).containsEntry(SERVICE_KEY, INVENTORY).containsEntry(REMOTE_STATUS_KEY, 409);
        }

        @Test
        void fieldErrorsAreReadWithTheirCodesMessagesAndParams() {
            RemoteErrorContext context = contextOf(Map.of(FIELD_ERRORS_KEY, List.of(
                    Map.of(FIELD_KEY, NAME, CODE_KEY, NOT_BLANK, MESSAGE_KEY, MUST_NOT_BE_BLANK,
                            PARAMS_KEY, Map.of(MAX_KEY, 10)))), 400);

            assertThat(context.fieldErrors()).singleElement().satisfies(error -> {
                assertThat(error.field()).isEqualTo(NAME);
                assertThat(error.code()).isEqualTo(NOT_BLANK);
                assertThat(error.message()).isEqualTo(MUST_NOT_BE_BLANK);
                assertThat(error.params()).containsEntry(MAX_KEY, 10);
            });
        }

        @Test
        void aFieldErrorWithoutAFieldNameIsSkippedRatherThanRecordedAsNull() {
            RemoteErrorContext context = contextOf(Map.of(FIELD_ERRORS_KEY, List.of(
                    Map.of(CODE_KEY, NOT_BLANK), NOT_A_MAP)), 400);

            assertThat(context.fieldErrors()).isEmpty();
        }
    }

    @Nested
    class ResolvingAgainstACatalog {

        @Test
        void anApiWithoutACatalogEntryForTheCodeGetsTheUntypedFailure() {
            RemoteServiceException e = RemoteFailures.resolve(RemoteErrorCatalog.none(),
                    contextOf(Map.of(CODE_KEY, NOT_FOUND_CODE), NOT_FOUND));

            assertThat(e).isInstanceOf(UnmappedRemoteFailureException.class);
            assertThat(e.errorCode()).isEqualTo(CommonErrors.REMOTE_CALL_FAILED);
            assertThat(e.remoteCode()).isEqualTo(NOT_FOUND_CODE);
            assertThat(e.remoteStatus()).isEqualTo(NOT_FOUND);
        }

        @Test
        void aRemoteThatTimedOutFurtherDownIsReportedAsATimeoutNotAFailedCall() {
            RemoteServiceException gateway = RemoteFailures.resolve(RemoteErrorCatalog.none(), contextOf(null, 504));
            RemoteServiceException request = RemoteFailures.resolve(RemoteErrorCatalog.none(), contextOf(null, 408));

            assertThat(gateway.errorCode()).isEqualTo(CommonErrors.REMOTE_TIMEOUT);
            assertThat(request.errorCode()).isEqualTo(CommonErrors.REMOTE_TIMEOUT);
        }

        @Test
        void aFailureWithoutADetailStillSaysWhichCallFailed() {
            RemoteServiceException e = RemoteFailures.resolve(RemoteErrorCatalog.none(), contextOf(null, 500));

            assertThat(e.getMessage()).contains(CATALOG_SERVICE);
        }

        @Test
        void theRemotesDetailIsPreferredOverTheGeneratedOne() {
            RemoteServiceException e = RemoteFailures.resolve(RemoteErrorCatalog.none(),
                    contextOf(Map.of(DETAIL_KEY, NO_SUCH_PRODUCT), NOT_FOUND));

            assertThat(e.getMessage()).contains(NO_SUCH_PRODUCT);
        }
    }

    @Nested
    class CallsThatNeverArrived {

        @Test
        void aRefusedConnectionIsReportedAsUnavailable() {
            RemoteServiceException e = RemoteFailures.unreachable(RemoteErrorCatalog.none(), CATALOG_SERVICE, PATH,
                    new IOException(CONNECTION_REFUSED));

            assertThat(e).isInstanceOf(RemoteServiceUnavailableException.class);
            assertThat(e.params()).containsEntry(SERVICE_KEY, CATALOG_SERVICE).containsEntry(PATH_KEY, PATH);
        }

        /**
         * Reachable-but-slow and not-reachable-at-all need different remedies, so they are different types. The
         * timeout is usually wrapped, so the whole cause chain has to be examined.
         */
        @Test
        void aReadTimeoutIsReportedAsATimeoutEvenWhenItIsWrapped() {
            RemoteServiceException direct = RemoteFailures.unreachable(RemoteErrorCatalog.none(), CATALOG_SERVICE,
                    PATH, new SocketTimeoutException("read timed out"));
            RemoteServiceException wrapped = RemoteFailures.unreachable(RemoteErrorCatalog.none(), CATALOG_SERVICE,
                    PATH, new IllegalStateException(new IOException(new HttpTimeoutException("timed out"))));

            assertThat(direct).isInstanceOf(RemoteServiceTimeoutException.class);
            assertThat(wrapped).isInstanceOf(RemoteServiceTimeoutException.class);
        }

        @Test
        void aTransportFailureWithNoCauseAtAllIsStillDescribed() {
            RemoteServiceException e = RemoteFailures.unreachable(RemoteErrorCatalog.none(), CATALOG_SERVICE, PATH,
                    null);

            assertThat(e).isInstanceOf(RemoteServiceUnavailableException.class);
        }

        @Test
        void anApiPublishingItsOwnTransportFailureTypeGetsThatTypeInstead() {
            RemoteErrorCatalog catalog = RemoteErrorCatalog.builder()
                    .unreachable(context -> RemoteServiceTimeoutException.of(context.service(),
                            context.params(), context.cause()))
                    .build();

            RemoteServiceException e = RemoteFailures.unreachable(catalog, CATALOG_SERVICE, PATH,
                    new IOException(CONNECTION_REFUSED));

            assertThat(e).isInstanceOf(RemoteServiceTimeoutException.class);
        }
    }
}
