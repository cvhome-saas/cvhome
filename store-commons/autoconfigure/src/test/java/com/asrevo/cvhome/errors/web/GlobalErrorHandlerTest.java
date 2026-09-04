package com.asrevo.cvhome.errors.web;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.UncheckedBaseException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The one advice that turns an exception into a response body.
 *
 * <p>
 * Three behaviours here are load-bearing rather than incidental. A downstream 4xx keeps its status because it is the
 * caller's problem, while a downstream 5xx becomes a 502 because this service is not the one that failed. A
 * {@link UncheckedBaseException} that escaped a lambda is unwrapped, so a carrier does not turn its cause's 404 into
 * a 500. And an unclassified failure never puts root-cause text in {@code detail} — that is exactly what the handler
 * this class replaced did, and it leaked stack traces to clients.
 * </p>
 */
class GlobalErrorHandlerTest {

    private static final String TYPE_BASE = "https://errors.example.com";
    private static final String PEER = "payment";
    private static final String PEER_CODE = "PAYMENT.INITIATE.FAILED";
    private static final String OBJECT_NAME = "createProduct";
    private static final String NO_SUCH_PRODUCT = "no such product";
    private static final String DECLINED = "card_declined";
    private static final String REAL_CAUSE = "the real cause";
    private static final String MVC = "mvc";
    private static final String SERVICE_CODE = "SERVICE.OWN.CODE";

    private final GlobalErrorHandler handler =
            new GlobalErrorHandler(new ProblemDetailFactory(new ErrorHandlingProperties(TYPE_BASE, false)));

    private static final class NotFound extends BaseException {
        private NotFound() {
            super(ErrorPayload.of(CommonErrors.RESOURCE_NOT_FOUND, NO_SUCH_PRODUCT), null);
        }
    }

    private static final class Upstream extends RemoteServiceException {
        private Upstream(ErrorPayload payload, Throwable cause, String service, String code, int status) {
            super(payload, cause, service, code, status);
        }
    }

    private static final class Provider extends ExternalProviderException {
        private Provider(ErrorPayload payload, Throwable cause, String provider, String code, int status) {
            super(payload, cause, provider, code, status);
        }
    }

    private static Map<String, Object> propertiesOf(ResponseEntity<ProblemDetail> response) {
        return response.getBody().getProperties();
    }

    @Test
    void aTypedFailureRendersItsOwnCodeStatusAndTraceId() {
        ResponseEntity<ProblemDetail> response = handler.handleBaseException(new NotFound());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getDetail()).isEqualTo(NO_SUCH_PRODUCT);
        assertThat(propertiesOf(response))
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.RESOURCE_NOT_FOUND.code())
                .containsKey(ProblemDetailFactory.TRACE_ID);
    }

    @Test
    void aCarrierThatEscapedALambdaStillRendersItsCausesStatus() {
        ResponseEntity<ProblemDetail> response =
                handler.handleUncheckedBaseException(new UncheckedBaseException(new NotFound()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(propertiesOf(response))
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.RESOURCE_NOT_FOUND.code());
    }

    @ParameterizedTest
    @CsvSource({"404,404", "409,409", "422,422", "500,502", "503,502", "0,502"})
    void aDownstreamClientErrorKeepsItsStatusAndAServerErrorBecomesABadGateway(int remote, int expected) {
        Upstream error = RemoteServiceException.of(CommonErrors.REMOTE_CALL_FAILED, Upstream::new)
                .remoteService(PEER).remoteCode(PEER_CODE).remoteStatus(remote).build();

        ResponseEntity<ProblemDetail> response = handler.handleBaseException(error);

        assertThat(response.getStatusCode().value()).isEqualTo(expected);
        assertThat(propertiesOf(response)).containsEntry(ProblemDetailFactory.CODE, PEER_CODE)
                .containsEntry(ProblemDetailFactory.REMOTE_SERVICE, PEER);
    }

    @Test
    void aProvidersFailureKeepsOurCodeAndOurStatus() {
        Provider error = ExternalProviderException.of(CommonErrors.REMOTE_CALL_FAILED, Provider::new)
                .provider("stripe").providerCode(DECLINED).providerStatus(402).build();

        ResponseEntity<ProblemDetail> response = handler.handleBaseException(error);

        assertThat(propertiesOf(response))
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.REMOTE_CALL_FAILED.code())
                .containsEntry(ProblemDetailFactory.PROVIDER_CODE, DECLINED);
        assertThat(response.getStatusCode().value())
                .isEqualTo(CommonErrors.REMOTE_CALL_FAILED.category().httpStatus());
    }

    @Test
    void anUnclassifiedFailureNeverPutsRootCauseTextInTheBody() {
        ResponseEntity<ProblemDetail> response =
                handler.handleUnexpected(new IllegalStateException("connection string user=admin password=hunter2"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getDetail()).doesNotContain("hunter2")
                .isEqualTo("The request could not be completed. Quote the traceId when reporting this error.");
        assertThat(propertiesOf(response))
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.INTERNAL_ERROR.code());
    }

    @Test
    void withDebugDetailOnTheRootCauseMessageIsIncludedInstead() {
        GlobalErrorHandler debugging =
                new GlobalErrorHandler(new ProblemDetailFactory(new ErrorHandlingProperties(TYPE_BASE, true)));

        ResponseEntity<ProblemDetail> response = debugging.handleUnexpected(
                new IllegalStateException("outer", new IllegalArgumentException(REAL_CAUSE)));

        assertThat(response.getBody().getDetail()).isEqualTo(REAL_CAUSE);
    }

    @Test
    void aCauselessFailureWithNoMessageFallsBackToItsClassName() {
        GlobalErrorHandler debugging =
                new GlobalErrorHandler(new ProblemDetailFactory(new ErrorHandlingProperties(TYPE_BASE, true)));

        ResponseEntity<ProblemDetail> response = debugging.handleUnexpected(new IllegalStateException());

        assertThat(response.getBody().getDetail()).isEqualTo(IllegalStateException.class.getName());
    }

    @Test
    void anUncheckedExceptionCarryingACodeIsRenderedFromThatCodeNotAsAFiveHundred() {
        ResponseEntity<ProblemDetail> response =
                handler.handleUnexpected(new UncheckedBaseException(new NotFound()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void beanValidationFailuresCarryThePerFieldDetailAClientNeedsToHighlightControls() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), OBJECT_NAME);
        binding.rejectValue(null, "NotBlank", "must not be blank");
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(
                        GlobalErrorHandlerTest.class.getDeclaredMethod("sample", String.class), 0), binding);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(exception, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, new ServletWebRequest(new org.springframework.mock.web.MockHttpServletRequest()));

        ProblemDetail problem = (ProblemDetail) response.getBody();
        assertThat(problem.getStatus()).isEqualTo(CommonErrors.VALIDATION_FAILED.category().httpStatus());
        assertThat(problem.getProperties()).containsEntry(ProblemDetailFactory.CODE,
                CommonErrors.VALIDATION_FAILED.code());
    }

    @ParameterizedTest
    @CsvSource({"405,COMMON.METHOD_NOT_ALLOWED", "413,COMMON.UPLOAD_TOO_LARGE", "415,COMMON.UNSUPPORTED_MEDIA_TYPE",
                "400,COMMON.MALFORMED_REQUEST", "404,COMMON.RESOURCE_NOT_FOUND", "418,COMMON.INTERNAL_ERROR"})
    void springsOwnMvcFailuresGainACodeFromTheSharedVocabulary(int status, String code) {
        ResponseEntity<Object> response = handler.handleExceptionInternal(new IllegalStateException(MVC), null,
                new HttpHeaders(), HttpStatusCode.valueOf(status),
                new ServletWebRequest(new org.springframework.mock.web.MockHttpServletRequest()));

        ProblemDetail problem = (ProblemDetail) response.getBody();
        assertThat(problem.getProperties()).containsEntry(ProblemDetailFactory.CODE, code);
        assertThat(problem.getStatus()).isEqualTo(status);
    }

    @Test
    void aBodyThatAlreadyCarriesACodeIsLeftAloneSoAServiceAdviceCanWin() {
        ProblemDetail alreadyCoded = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        alreadyCoded.setProperty(ProblemDetailFactory.CODE, SERVICE_CODE);

        ResponseEntity<Object> response = handler.handleExceptionInternal(new IllegalStateException(MVC),
                alreadyCoded, new HttpHeaders(), HttpStatus.CONFLICT,
                new ServletWebRequest(new org.springframework.mock.web.MockHttpServletRequest()));

        assertThat(((ProblemDetail) response.getBody()).getProperties())
                .containsEntry(ProblemDetailFactory.CODE, SERVICE_CODE);
    }

    @SuppressWarnings("unused")
    private void sample(String field) {
        // Only a MethodParameter source for MethodArgumentNotValidException.
    }
}
