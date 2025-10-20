package com.asrevo.cvhome.gateway.config;

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
import reactor.core.publisher.Mono;

public class CapturingServerOAuth2AuthorizationRequestResolver
        implements ServerOAuth2AuthorizationRequestResolver {

    private static final Logger logger =
            LoggerFactory.getLogger(CapturingServerOAuth2AuthorizationRequestResolver.class);
    private final ServerOAuth2AuthorizationRequestResolver delegate;

    // Define the query parameters you want to capture from the URL
    // Make this configurable via application properties if needed
    private final List<String> parametersToCapture = List.of("redirectTo");

    // Session key prefix to store the captured parameters
    public static final String CAPTURED_PARAMETERS_SESSION_KEY_PREFIX =
            "CAPTURED_OAUTH2_LOGIN_PARAMS_FOR_STATE_";

    public CapturingServerOAuth2AuthorizationRequestResolver(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        // You can customize the base URI if needed, but for most Spring Cloud Gateway setups,
        // the default resolver handles various ways login can be initiated.
        this.delegate =
                new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
        return this.delegate
                .resolve(exchange)
                .flatMap(
                        authorizationRequest -> {
                            if (authorizationRequest == null) {
                                return Mono.empty();
                            }
                            return modifyAuthorizationRequestUriWithForwardedParam(
                                            exchange, authorizationRequest)
                                    .flatMap(
                                            modifiedRequest ->
                                                    captureParametersAndStoreInSession(
                                                            exchange, modifiedRequest));
                        });
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(
            ServerWebExchange exchange, String clientRegistrationId) {
        return this.delegate
                .resolve(exchange, clientRegistrationId)
                .flatMap(
                        authorizationRequest -> {
                            if (authorizationRequest == null) {
                                return Mono.empty();
                            }
                            return modifyAuthorizationRequestUriWithForwardedParam(
                                            exchange, authorizationRequest)
                                    .flatMap(
                                            modifiedRequest ->
                                                    captureParametersAndStoreInSession(
                                                            exchange, modifiedRequest));
                        });
    }

    private Mono<OAuth2AuthorizationRequest> modifyAuthorizationRequestUriWithForwardedParam(
            ServerWebExchange exchange, OAuth2AuthorizationRequest authorizationRequest) {
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        UriComponentsBuilder componentsBuilder =
                UriComponentsBuilder.fromUriString(
                        authorizationRequest.getAuthorizationRequestUri());
        UriComponents originalUriComponents =
                UriComponentsBuilder.fromUriString(
                                authorizationRequest.getAuthorizationRequestUri())
                        .build();
        MultiValueMap<String, String> originalQueryParams = originalUriComponents.getQueryParams();

        queryParams.entrySet().stream()
                .filter(
                        it ->
                                Objects.nonNull(it.getKey())
                                        && Objects.nonNull(it.getValue())
                                        && !it.getValue().isEmpty()
                                        && !originalQueryParams.containsKey(it.getKey()))
                .forEach(
                        (param) ->
                                componentsBuilder.queryParam(
                                        param.getKey(), param.getValue().getFirst()));

        String newRequestUri = componentsBuilder.build(true).toUriString();

        return Mono.just(
                OAuth2AuthorizationRequest.from(authorizationRequest)
                        .authorizationRequestUri(newRequestUri)
                        .build());
    }

    private Mono<OAuth2AuthorizationRequest> captureParametersAndStoreInSession(
            ServerWebExchange exchange, OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            // No authorization request to process, so no parameters to capture for this flow.
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
            // The 'state' parameter is crucial for correlating the request with the callback
            String state = authorizationRequest.getState();
            if (state == null || state.isEmpty()) {
                logger.warn(
                        "OAuth2AuthorizationRequest state is null or empty. Cannot reliably store"
                                + " captured parameters for this flow.");
                return Mono.just(
                        authorizationRequest); // Proceed without storing if state is missing
            }

            String sessionKey = CAPTURED_PARAMETERS_SESSION_KEY_PREFIX + state;
            logger.info(
                    "Storing captured parameters in session with key '{}': {}",
                    sessionKey,
                    capturedParamsMap);

            return exchange.getSession()
                    .map(
                            webSession -> {
                                webSession.getAttributes().put(sessionKey, capturedParamsMap);
                                return authorizationRequest;
                            });
        }

        // No parameters were captured, or no relevant parameters found
        return Mono.just(authorizationRequest);
    }
}
