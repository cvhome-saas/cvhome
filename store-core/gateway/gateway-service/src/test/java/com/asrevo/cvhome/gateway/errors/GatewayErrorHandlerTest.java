package com.asrevo.cvhome.gateway.errors;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.errors.web.ErrorHandlingProperties;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import static org.assertj.core.api.Assertions.assertThat;

/** The gateway's typed failures render as the platform's problem detail, status from the code's category. */
class GatewayErrorHandlerTest {

    private static final String READ_ONLY = "This operator may act read-only.";

    private final GatewayErrorHandler handler =
            new GatewayErrorHandler(new ProblemDetailFactory(new ErrorHandlingProperties(null, false)));

    @Test
    void aRefusalIs403WithTheCodeAndUaasError() {
        ResponseEntity<ProblemDetail> response = handler.onBaseException(
                ImpersonationRefusedException.of("access_denied", READ_ONLY));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        ProblemDetail body = response.getBody();
        assertThat(body.getProperties()).containsEntry(ProblemDetailFactory.CODE, "GATEWAY.IMPERSONATION.REFUSED")
                .containsKey(ProblemDetailFactory.TRACE_ID);
        assertThat(body.getDetail()).isEqualTo(READ_ONLY);
    }

    @Test
    void amissingSessionIs401() {
        assertThat(handler.onBaseException(SessionRequiredException.of()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void theOtherConditionsMapToTheirCategories() {
        assertThat(handler.onBaseException(ImpersonationAlreadyActiveException.actingAs("m")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(handler.onBaseException(ImpersonationInvalidException.missing("reason")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(handler.onBaseException(ImpersonationUnavailableException.of(new RuntimeException("x"), "uaa")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

}
