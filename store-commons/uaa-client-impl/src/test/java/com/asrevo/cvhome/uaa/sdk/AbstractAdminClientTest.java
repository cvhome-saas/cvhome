package com.asrevo.cvhome.uaa.sdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.uaa.api.errors.UaaApiException;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.errors.UaaErrors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Where uaa's problem body becomes an exception a caller can catch.
 *
 * <p>
 * This all used to collapse into one {@code ApiException} carrying the response as a string, so a caller could
 * neither branch on the code nor tell a refusal from an outage. The rule now is that a named code arrives as its own
 * type, and everything else — an unnamed code, a proxy's HTML page, a call that never arrived — arrives as
 * "unavailable", because nothing about it can be concluded.
 * </p>
 */
class AbstractAdminClientTest {

    private static final String BASE_URL = "http://uaa:9999";

    private static final String PATH = "/api/v1/admin/users/u1";

    private static final String EMPTY_BODY = "{}";

    private HttpClient httpClient;

    private TestAdminClient client;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        client = new TestAdminClient(httpClient);
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        when(response.uri()).thenReturn(URI.create(BASE_URL + PATH));
        return response;
    }

    @Test
    void aSuccessfulResponsePassesStraightThrough() {
        assertThat(catchThrowableOf(response(200, EMPTY_BODY))).isNull();
        assertThat(catchThrowableOf(response(204, ""))).isNull();
    }

    private Throwable catchThrowableOf(HttpResponse<String> response) {
        try {
            client.verify(response);
            return null;
        } catch (UaaApiException e) {
            return e;
        }
    }

    @Test
    void aCodeTheCatalogNamesArrivesAsItsOwnType() {
        Throwable thrown = catchThrowableOf(response(404,
                String.format("{\"code\":\"%s\",\"detail\":\"no such user\"}", UaaErrors.USER_NOT_FOUND.code())));

        assertThat(thrown).isInstanceOf(UaaUserNotFoundException.class);
    }

    @Test
    void aCodeThisSdkDoesNotNameArrivesAsUndecidedButKeepsWhatUaaAnswered() {
        Throwable thrown = catchThrowableOf(response(409, "{\"code\":\"UAA.SOMETHING.NEW\"}"));

        assertThat(thrown).isInstanceOf(UaaApiUnavailableException.class);
        assertThat(((UaaApiUnavailableException) thrown).remoteStatus()).isEqualTo(409);
        assertThat(((UaaApiUnavailableException) thrown).remoteCode()).isEqualTo("UAA.SOMETHING.NEW");
    }

    /**
     * A proxy returning an HTML error page, or a gateway returning plain text, must not turn describing the failure
     * into a second failure.
     */
    @Test
    void aBodyThatIsNotAProblemDocumentIsStillDescribed() {
        assertThat(catchThrowableOf(response(502, "<html><body>Bad Gateway</body></html>")))
                .isInstanceOf(UaaApiUnavailableException.class);
        assertThat(catchThrowableOf(response(500, ""))).isInstanceOf(UaaApiUnavailableException.class);
        assertThat(catchThrowableOf(response(500, "   "))).isInstanceOf(UaaApiUnavailableException.class);
    }

    @Test
    void aRequestThatNeverArrivedIsReportedAsUnavailable() throws Exception {
        when(httpClient.send(org.mockito.ArgumentMatchers.any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("connection refused"));

        assertThatThrownBy(() -> client.get(BASE_URL + PATH))
                .isInstanceOf(UaaApiUnavailableException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void anInterruptedRequestRestoresTheInterruptFlagForTheCaller() throws Exception {
        when(httpClient.send(org.mockito.ArgumentMatchers.any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new InterruptedException("stopped"));

        assertThatThrownBy(() -> client.get(BASE_URL + PATH)).isInstanceOf(UaaApiUnavailableException.class);
        assertThat(Thread.interrupted()).isTrue();
    }

    /**
     * The SDK is abstract because each resource client adds its own paths; a test needs a concrete one, exactly as
     * {@code AdminUserClient} is.
     */
    private static final class TestAdminClient extends AbstractAdminClient {

        private TestAdminClient(HttpClient httpClient) {
            super(BASE_URL, "admin-sdk", "s3cr3t", httpClient);
        }

        private void verify(HttpResponse<String> response) throws UaaApiException {
            verifyResponse(response);
        }

        private void get(String url) throws UaaApiException {
            sendAndVerify(HttpRequest.newBuilder().uri(URI.create(url)).GET().build());
        }
    }
}
