package com.asrevo.cvhome.gateway.config;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import reactor.core.publisher.Mono;

public class CapturingServerOAuth2AuthorizationRequestResolver implements ServerOAuth2AuthorizationRequestResolver {

    /*
     * The forwarded values are re-encoded before they are appended, because the URI is then built
     * with build(true) — "everything here is already encoded".
     *
     * getQueryParams() hands back *decoded* values, so a redirectTo carrying a query string of its
     * own arrived as `/accept-invitation?token=abc`, and build(true) rejected the bare '?' and '='
     * as illegal in a query parameter:
     *
     *   IllegalArgumentException: Invalid character '=' for QUERY_PARAM in "/accept-invitation?token=abc"
     *
     * That surfaced as a **500 on the login redirect** for any deep link with a query string —
     * a filtered list, a selected row, an invitation link — while a bare path like /dashboard
     * worked, which is why it went unnoticed: every hand-typed URL is a bare path.
     */

    // Session key prefix to store the captured parameters
    public static final String CAPTURED_PARAMETERS_SESSION_KEY_PREFIX = "CAPTURED_OAUTH2_LOGIN_PARAMS_FOR_STATE_";

    /** uaa's authorize endpoint, under the gateway's forward prefix. */
    private static final String AUTHORIZE_PATH = "/oauth2/authorize";

    private static final String CONCAT = "%s%s";

    private static final Logger logger = LoggerFactory
            .getLogger(CapturingServerOAuth2AuthorizationRequestResolver.class);

    private final ServerOAuth2AuthorizationRequestResolver delegate;

    // Define the query parameters you want to capture from the URL
    // Make this configurable via application properties if needed
    private final List<String> parametersToCapture = List.of("redirectTo");

    public CapturingServerOAuth2AuthorizationRequestResolver(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        // You can customize the base URI if needed, but for most Spring Cloud Gateway
        // setups,
        // the default resolver handles various ways login can be initiated.
        this.delegate = new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
        return this.delegate.resolve(exchange).flatMap(authorizationRequest -> {
            if (authorizationRequest == null) {
                return Mono.empty();
            }
            return modifyAuthorizationRequestUriWithForwardedParam(exchange, authorizationRequest)
                    .map(modifiedRequest -> onThisOrigin(exchange, modifiedRequest))
                    .flatMap(modifiedRequest -> captureParametersAndStoreInSession(exchange, modifiedRequest));
        });
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
        return this.delegate.resolve(exchange, clientRegistrationId).flatMap(authorizationRequest -> {
            if (authorizationRequest == null) {
                return Mono.empty();
            }
            return modifyAuthorizationRequestUriWithForwardedParam(exchange, authorizationRequest)
                    .map(modifiedRequest -> onThisOrigin(exchange, modifiedRequest))
                    .flatMap(modifiedRequest -> captureParametersAndStoreInSession(exchange, modifiedRequest));
        });
    }

    /**
     * Sends the browser to uaa on the origin it is already on, instead of to uaa's own host.
     *
     * <p>
     * The console renders the sign-in page now, so the whole visible half of the flow has to happen on one
     * origin: the authorize request, the redirect to {@code /sign-in}, the form POST back to {@code /uaa/login},
     * and the callback. Cross-origin it still authenticates, but the session cookie uaa sets while holding the
     * saved request belongs to uaa's host and does not ride along on the form POST, so the flow restarts instead
     * of resuming — and the console cannot read the CSRF cookie to fill the form in the first place.
     * </p>
     *
     * <p>
     * Only the browser-facing endpoint moves. {@code token-uri}, {@code jwk-set-uri} and {@code user-info-uri}
     * are called by this gateway, not by a browser, and stay pointed at uaa's own address — which is also why
     * {@code authorization-uri} could be redirected at all: it is configured separately from them.
     * </p>
     *
     * <p>
     * The console answers on three hosts, so the origin is taken from the request rather than configured: a
     * person who started on {@code console-ui.gateway.com} is sent to sign in there and comes back there.
     * </p>
     */
    private OAuth2AuthorizationRequest onThisOrigin(ServerWebExchange exchange, OAuth2AuthorizationRequest request) {
        URI incoming = exchange.getRequest().getURI();
        if (Objects.isNull(incoming.getHost())) {
            return request;
        }
        String rewritten = UriComponentsBuilder.fromUriString(request.getAuthorizationRequestUri())
                .scheme(incoming.getScheme())
                .host(incoming.getHost())
                .port(incoming.getPort())
                .replacePath(CONCAT.formatted(GatewayRouteLocatorImpl.UAA_PREFIX, AUTHORIZE_PATH))
                .build(true)
                .toUriString();
        return OAuth2AuthorizationRequest.from(request).authorizationRequestUri(rewritten).build();
    }

    private Mono<OAuth2AuthorizationRequest> modifyAuthorizationRequestUriWithForwardedParam(ServerWebExchange exchange,
                                                                                             OAuth2AuthorizationRequest request) {
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        UriComponentsBuilder componentsBuilder = UriComponentsBuilder
                .fromUriString(request.getAuthorizationRequestUri());
        UriComponents originalUriComponents = UriComponentsBuilder.fromUriString(request.getAuthorizationRequestUri())
                .build();
        MultiValueMap<String, String> originalQueryParams = originalUriComponents.getQueryParams();

        queryParams.entrySet()
                .stream()
                .filter(it -> Objects.nonNull(it.getKey()) && Objects.nonNull(it.getValue()) && !it.getValue().isEmpty()
                        && !originalQueryParams.containsKey(it.getKey()))
                .forEach(param -> componentsBuilder.queryParam(param.getKey(),
                        UriUtils.encode(param.getValue().getFirst(), StandardCharsets.UTF_8)));

        String newRequestUri = componentsBuilder.build(true).toUriString();

        return Mono.just(OAuth2AuthorizationRequest.from(request).authorizationRequestUri(newRequestUri).build());
    }

    private Mono<OAuth2AuthorizationRequest> captureParametersAndStoreInSession(ServerWebExchange exchange,
                                                                                OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            // No authorization request to process, so no parameters to capture for this
            // flow.
            return Mono.empty();
        }

        Map<String, String> capturedParamsMap = new HashMap<>();
        for (String paramName : parametersToCapture) {
            String paramValue = exchange.getRequest().getQueryParams().getFirst(paramName);
            if (paramValue != null && !paramValue.isEmpty()) {
                capturedParamsMap.put(paramName, paramValue);
                logger.debug("Captured parameter '{}' with value '{}'", paramName, paramValue);
            }
        }

        if (!capturedParamsMap.isEmpty()) {
            // The 'state' parameter is crucial for correlating the request with the
            // callback
            String state = authorizationRequest.getState();
            if (state == null || state.isEmpty()) {
                logger.warn(
                        "OAuth2AuthorizationRequest state is null or empty. Cannot reliably store captured parameters for this flow.");
                return Mono.just(authorizationRequest); // Proceed without storing if
                // state is missing
            }

            String sessionKey = CONCAT.formatted(CAPTURED_PARAMETERS_SESSION_KEY_PREFIX, state);
            logger.info("Storing captured parameters in session with key '{}': {}", sessionKey, capturedParamsMap);

            return exchange.getSession().map(webSession -> {
                webSession.getAttributes().put(sessionKey, capturedParamsMap);
                return authorizationRequest;
            });
        }

        // No parameters were captured, or no relevant parameters found
        return Mono.just(authorizationRequest);
    }

}
