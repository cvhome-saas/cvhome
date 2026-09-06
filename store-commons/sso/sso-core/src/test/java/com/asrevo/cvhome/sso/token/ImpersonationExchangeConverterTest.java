package com.asrevo.cvhome.sso.token;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What reaches the provider, and what never gets that far.
 *
 * <p>
 * The converter is the grant's front door: it must decline every other grant with {@code null} so the chain moves
 * on, and it must refuse a malformed request with the standard error before any account is looked up.
 * </p>
 */
class ImpersonationExchangeConverterTest {

    private static final String SUBJECT_TOKEN = "operator-token";

    private static final String TARGET = "60ab49a5-7f06-4b5a-be81-9b30bb6559ae";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String REASON = "ticket 42";

    private static final String READ = "read";

    private static final String OPENID = "openid";

    private static final String TRACE = "trace";

    private static final String TRACE_ID = "t-1";

    private final ImpersonationExchangeConverter converter = new ImpersonationExchangeConverter();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticatedClient() {
        RegisteredClient client = RegisteredClient.withId("id").clientId("console-impersonation")
                .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE).build();
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2ClientAuthenticationToken(client, ClientAuthenticationMethod.CLIENT_SECRET_BASIC, "secret"));
    }

    private static MockHttpServletRequest exchange(Map<String, String> overrides) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth2/token");
        request.setParameter(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.TOKEN_EXCHANGE.getValue());
        request.setParameter(ImpersonationExchangeConverter.SUBJECT_TOKEN, SUBJECT_TOKEN);
        request.setParameter(ImpersonationExchangeConverter.SUBJECT_TOKEN_TYPE, ImpersonationExchangeConverter.ACCESS_TOKEN_TYPE);
        request.setParameter(ImpersonationExchangeConverter.REQUESTED_SUBJECT, TARGET);
        request.setParameter(ImpersonationExchangeConverter.STORE, STORE);
        request.setParameter(ImpersonationExchangeConverter.MODE, READ);
        request.setParameter(ImpersonationExchangeConverter.REASON, REASON);
        overrides.forEach(request::setParameter);
        return request;
    }

    @Test
    void aWellFormedExchangeBecomesTheGrantsToken() {
        authenticatedClient();
        MockHttpServletRequest request = exchange(Map.of(OAuth2ParameterNames.SCOPE, OPENID, TRACE, TRACE_ID));

        Authentication converted = converter.convert(request);

        assertThat(converted).isInstanceOf(ImpersonationExchangeAuthenticationToken.class);
        ImpersonationExchangeAuthenticationToken token = (ImpersonationExchangeAuthenticationToken) converted;
        assertThat(token.getSubjectToken()).isEqualTo(SUBJECT_TOKEN);
        assertThat(token.getRequestedSubject()).isEqualTo(TARGET);
        assertThat(token.getStore()).isEqualTo(STORE);
        assertThat(token.getMode()).isEqualTo(READ);
        assertThat(token.getReason()).isEqualTo(REASON);
        assertThat(token.getScopes()).containsExactly(OPENID);
        assertThat(token.getGrantType()).isEqualTo(AuthorizationGrantType.TOKEN_EXCHANGE);
        // The grant's own parameters are fields; whatever else the form carried travels as additional parameters.
        assertThat(token.getAdditionalParameters()).containsEntry(TRACE, TRACE_ID)
                .doesNotContainKey(ImpersonationExchangeConverter.REASON);
    }

    @Test
    void anyOtherGrantIsDeclinedSoTheNextConverterAnswers() {
        authenticatedClient();
        MockHttpServletRequest request = exchange(Map.of(OAuth2ParameterNames.GRANT_TYPE, "client_credentials"));

        assertThat(converter.convert(request)).isNull();
    }

    @Test
    void anUnauthenticatedClientIsRefusedBeforeAnythingIsRead() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("x", null));

        assertThatThrownBy(() -> converter.convert(exchange(Map.of())))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(e -> ((OAuth2AuthenticationException) e).getError().getErrorCode())
                .isEqualTo(OAuth2ErrorCodes.INVALID_CLIENT);
    }

    @Test
    void aMissingReasonIsAnInvalidRequest() {
        authenticatedClient();
        MockHttpServletRequest request = exchange(Map.of());
        request.removeParameter(ImpersonationExchangeConverter.REASON);

        assertInvalidRequest(request, ImpersonationExchangeConverter.REASON);
    }

    @Test
    void aBlankStoreIsAnInvalidRequest() {
        authenticatedClient();

        assertInvalidRequest(exchange(Map.of(ImpersonationExchangeConverter.STORE, "  ")), ImpersonationExchangeConverter.STORE);
    }

    @Test
    void aSubjectTokenThatIsNotAnAccessTokenIsAnInvalidRequest() {
        authenticatedClient();

        assertInvalidRequest(exchange(Map.of(ImpersonationExchangeConverter.SUBJECT_TOKEN_TYPE,
                "urn:ietf:params:oauth:token-type:refresh_token")), ImpersonationExchangeConverter.SUBJECT_TOKEN_TYPE);
    }

    @Test
    void aRepeatedParameterIsAnInvalidRequest() {
        authenticatedClient();
        MockHttpServletRequest request = exchange(Map.of());
        request.setParameter(ImpersonationExchangeConverter.MODE, READ, "write");

        assertInvalidRequest(request, ImpersonationExchangeConverter.MODE);
    }

    private void assertInvalidRequest(MockHttpServletRequest request, String parameter) {
        assertThatThrownBy(() -> converter.convert(request))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> {
                    OAuth2AuthenticationException oauth = (OAuth2AuthenticationException) e;
                    assertThat(oauth.getError().getErrorCode()).isEqualTo(OAuth2ErrorCodes.INVALID_REQUEST);
                    assertThat(oauth.getError().getDescription()).contains(parameter);
                });
    }

}
