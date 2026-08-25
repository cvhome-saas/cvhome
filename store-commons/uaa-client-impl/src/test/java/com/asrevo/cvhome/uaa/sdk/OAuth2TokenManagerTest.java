package com.asrevo.cvhome.uaa.sdk;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The client_credentials exchange every uaa admin call depends on.
 *
 * <p>
 * Two behaviours are load-bearing. The token is cached until it is nearly expired, because otherwise every admin
 * call would cost a second round trip; and a failure here is reported as "uaa unavailable" rather than as an
 * authentication error, because from the caller's side the request it asked for was never attempted at all. The
 * failure also deliberately drops the response body — a rejected token exchange can echo our own client secret back.
 * </p>
 */
class OAuth2TokenManagerTest {

    private static final String BASE_URL = "http://uaa:9999";

    private static final String CLIENT_ID = "admin-sdk";

    private static final String CLIENT_SECRET = "s3cr3t";

    private static final String TOKEN = "access-token-1";

    private static final String REFRESHED = "access-token-2";

    private static final String AUTHORIZATION = "Authorization";

    private static final String EMPTY_BODY = "{}";

    private HttpClient httpClient;

    private OAuth2TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        tokenManager = new OAuth2TokenManager(BASE_URL, CLIENT_ID, CLIENT_SECRET, httpClient);
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }

    private static String token(String value, long expiresIn) {
        return String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":%d,"
                + "\"scope\":\"super_admin\"}", value, expiresIn);
    }

    @SuppressWarnings("unchecked")
    private void answerWith(HttpResponse<String>... responses) throws Exception {
        if (responses.length == 1) {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(responses[0]);
        } else {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(responses[0], java.util.Arrays.copyOfRange(responses, 1, responses.length));
        }
    }

    @SuppressWarnings("unchecked")
    private HttpRequest lastRequest() throws Exception {
        ArgumentCaptor<HttpRequest> request = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(request.capture(), any(HttpResponse.BodyHandler.class));
        return request.getValue();
    }

    @Test
    void theTokenIsFetchedWithTheConfiguredCredentialsInTheForm() throws Exception {
        answerWith(response(200, token(TOKEN, 3600)));

        assertThat(tokenManager.getAccessToken()).isEqualTo(TOKEN);
        assertThat(lastRequest().uri()).hasToString("http://uaa:9999/oauth2/token");
    }

    @Test
    void aTokenThatIsStillValidIsReusedRatherThanFetchedAgain() throws Exception {
        answerWith(response(200, token(TOKEN, 3600)));

        assertThat(tokenManager.getAccessToken()).isEqualTo(TOKEN);
        assertThat(tokenManager.getAccessToken()).isEqualTo(TOKEN);

        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    /**
     * The manager refreshes a minute before the stated expiry, so a token whose life is shorter than that skew is
     * already stale the moment it arrives and the next call has to fetch a new one.
     */
    @Test
    void aTokenAlreadyInsideTheRefreshSkewIsNotReused() throws Exception {
        answerWith(response(200, token(TOKEN, 30)), response(200, token(REFRESHED, 30)));

        assertThat(tokenManager.getAccessToken()).isEqualTo(TOKEN);
        assertThat(tokenManager.getAccessToken()).isEqualTo(REFRESHED);

        verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    /**
     * uaa may be configured for either client authentication method, so a rejection of the form-encoded credentials
     * is retried as HTTP Basic before the exchange is called a failure.
     */
    @Test
    void credentialsRejectedInTheFormAreRetriedAsBasicAuth() throws Exception {
        answerWith(response(401, EMPTY_BODY), response(200, token(TOKEN, 3600)));

        assertThat(tokenManager.getAccessToken()).isEqualTo(TOKEN);

        ArgumentCaptor<HttpRequest> requests = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(2)).send(requests.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest fallback = requests.getAllValues().get(1);
        String expected = Base64.getEncoder()
                .encodeToString(String.format("%s:%s", CLIENT_ID, CLIENT_SECRET).getBytes());
        assertThat(fallback.headers().firstValue(AUTHORIZATION))
                .isEqualTo(Optional.of(String.format("Basic %s", expected)));
    }

    @Test
    void anExchangeRejectedBothWaysIsReportedAsUaaBeingUnavailable() throws Exception {
        answerWith(response(401, EMPTY_BODY), response(401, EMPTY_BODY));

        assertThatThrownBy(() -> tokenManager.getAccessToken())
                .isInstanceOf(UaaApiUnavailableException.class)
                .hasMessageContaining("access token");
    }

    /**
     * The body is deliberately not carried into the exception: a failed token exchange can echo the client secret
     * back, and this detail reaches a client response.
     */
    @Test
    void theRejectedExchangeNeverCarriesTheResponseBodyOrTheSecret() throws Exception {
        answerWith(response(401, String.format("{\"error\":\"invalid_client\",\"secret\":\"%s\"}", CLIENT_SECRET)),
                response(401, EMPTY_BODY));

        assertThatThrownBy(() -> tokenManager.getAccessToken())
                .isInstanceOf(UaaApiUnavailableException.class)
                .hasMessageNotContaining(CLIENT_SECRET)
                .hasMessageNotContaining("invalid_client");
    }

    @Test
    void aTransportFailureIsReportedAsUaaBeingUnavailable() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("connection refused"));

        assertThatThrownBy(() -> tokenManager.getAccessToken())
                .isInstanceOf(UaaApiUnavailableException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void anInterruptedExchangeRestoresTheInterruptFlagForTheCaller() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("stopped"));

        assertThatThrownBy(() -> tokenManager.getAccessToken()).isInstanceOf(UaaApiUnavailableException.class);
        assertThat(Thread.interrupted()).isTrue();
    }
}
