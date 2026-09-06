package com.asrevo.cvhome.gateway.impersonation;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.gateway.errors.ImpersonationAlreadyActiveException;
import com.asrevo.cvhome.gateway.errors.ImpersonationRefusedException;
import com.asrevo.cvhome.gateway.errors.ImpersonationStoreNotTargetsException;
import com.asrevo.cvhome.gateway.errors.ImpersonationUnavailableException;
import com.asrevo.cvhome.gateway.errors.SessionRequiredException;
import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;
import com.asrevo.cvhome.s2s.jwt.UaaJwtGrantedAuthoritiesConverter;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

/**
 * Acting as a merchant: the swap inside the gateway session, and its undoing.
 *
 * <p>
 * The console never holds a token — the gateway does — so "act as" is a server-side swap of two things: the
 * authorized client {@code tokenRelay()} reads, and the {@link SecurityContext} the console's {@code auth/me} reads.
 * Swapping only the first would relay the merchant's token under the operator's rail. The operator's originals are
 * stashed in the session as an {@link ImpersonationSession}, so ending it needs no login, and the exchanged token
 * is revoked on the way out so the audit trail records the end.
 * </p>
 *
 * <p>
 * Authorized clients are keyed by <em>principal name</em>, in the service {@code tokenRelay()}'s manager reads —
 * which is why the impersonated principal is named after target and operator together: named after the merchant
 * alone, an impersonation would overwrite the merchant's own client if they happened to be signed in too.
 * </p>
 *
 * <p>
 * Three calls, in order, none of them committed until the last: uaa's exchange (which applies every rule that
 * needs both principals), a probe of tenancy <em>as the impersonated principal</em> (the store check uaa cannot
 * make for an org admin, and the same call the console makes to enter a store, so a suspended store refuses here
 * too), and only then the swap. A probe that fails revokes the token it was made with.
 * </p>
 */
@Service
@Slf4j
public class ImpersonationService {

    /** The gateway's login registration — the one {@code tokenRelay()} reads. */
    static final String UAA_REGISTRATION = "uaa";

    /** The registration holding the impersonation client's credentials and uaa's token endpoint. */
    static final String IMPERSONATION_REGISTRATION = "console-impersonation";

    /** The impersonated principal's name attribute — see the class comment on why it is a composite. */
    static final String PRINCIPAL_KEY = "cvhome_principal";

    static final String PREFERRED_USERNAME = "preferred_username";

    static final String TOKEN_EXCHANGE = "urn:ietf:params:oauth:grant-type:token-exchange";

    static final String ACCESS_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";

    static final String ROUTER = "/api/v1/router/store-pod-by-store-id";

    private static final String SECURITY_CONTEXT =
            WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME;

    private static final String ACCESS_TOKEN = "access_token";

    private static final String TENANCY = "tenancy";

    private static final String TOKEN = "token";

    private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {
    };

    private final ReactiveOAuth2AuthorizedClientManager clientManager;

    private final ReactiveOAuth2AuthorizedClientService clients;

    private final ReactiveClientRegistrationRepository registrations;

    private final WebClient uaa;

    private final WebClient tenancy;

    private final ServiceUrlBuilder urls;

    private final Clock clock;

    public ImpersonationService(ReactiveOAuth2AuthorizedClientManager clientManager,
                                ReactiveOAuth2AuthorizedClientService clients,
                                ReactiveClientRegistrationRepository registrations,
                                @Qualifier("impersonationUaaClient") WebClient uaa,
                                @Qualifier("impersonationTenancyClient") WebClient tenancy,
                                @Qualifier("impersonationUrls") ServiceUrlBuilder urls, Clock clock) {
        this.clientManager = clientManager;
        this.clients = clients;
        this.registrations = registrations;
        this.uaa = uaa;
        this.tenancy = tenancy;
        this.urls = urls;
        this.clock = clock;
    }

    /** What this session is acting as, or empty. */
    public Mono<ImpersonationView> current(ServerWebExchange exchange) {
        return exchange.getSession().flatMap(session -> Mono.justOrEmpty(stashed(session)).map(ImpersonationSession::view));
    }

    public Mono<ImpersonationView> start(ServerWebExchange exchange, OAuth2AuthenticationToken operator,
                                         StartImpersonation request) {
        return exchange.getSession().flatMap(session -> {
            ImpersonationSession active = stashed(session);
            if (active != null) {
                return Mono.error(ImpersonationAlreadyActiveException.actingAs(active.view().actingAs()));
            }
            OAuth2AuthorizeRequest fresh = OAuth2AuthorizeRequest.withClientRegistrationId(UAA_REGISTRATION)
                    .principal(operator)
                    .attribute(ServerWebExchange.class.getName(), exchange)
                    .build();
            return clientManager.authorize(fresh)
                    .switchIfEmpty(Mono.error(SessionRequiredException::of))
                    .flatMap(operatorClient -> exchange(operatorClient.getAccessToken().getTokenValue(), request)
                            .flatMap(issued -> probeStore(issued, request.storeId()).thenReturn(issued))
                            .map(issued -> swap(exchange, session, operator, operatorClient, issued, request)));
        }).flatMap(swap -> clients.saveAuthorizedClient(swap.client(), swap.session().impersonated())
                .thenReturn(swap.session().view()));
    }

    /** Ends the impersonation, if any: the operator is themselves again and the exchanged token is revoked. */
    public Mono<Void> end(ServerWebExchange exchange) {
        return originalOf(exchange).then();
    }

    /**
     * The operator's own authentication, restoring it if the session was acting as somebody — for logout, which
     * must end <em>the operator's</em> uaa session and needs their ID token to do it.
     */
    public Mono<Authentication> originalOf(ServerWebExchange exchange) {
        return exchange.getSession().flatMap(session -> {
            ImpersonationSession active = stashed(session);
            if (active == null) {
                return Mono.empty();
            }
            return restore(session, active, "ended").thenReturn(active.original().getAuthentication());
        });
    }

    /** Hands the session back to the operator once the ceiling has passed — never to the merchant. */
    public Mono<Void> expireIfDue(ServerWebExchange exchange) {
        return exchange.getSession().flatMap(session -> {
            ImpersonationSession active = stashed(session);
            if (active == null || clock.instant().isBefore(active.view().expiresAt())) {
                return Mono.empty();
            }
            return restore(session, active, "expired");
        });
    }

    private static ImpersonationSession stashed(WebSession session) {
        return session.getAttribute(ImpersonationSession.ATTRIBUTE);
    }

    private Mono<Issued> exchange(String operatorToken, StartImpersonation request) {
        return registrations.findByRegistrationId(IMPERSONATION_REGISTRATION).flatMap(client -> {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", TOKEN_EXCHANGE);
            form.add("subject_token", operatorToken);
            form.add("subject_token_type", ACCESS_TOKEN_TYPE);
            form.add("requested_token_type", ACCESS_TOKEN_TYPE);
            form.add("requested_subject", request.userId());
            form.add("impersonation_store", request.storeId());
            form.add("impersonation_mode", request.mode());
            form.add("reason", request.reason());
            return uaa.post().uri(client.getProviderDetails().getTokenUri())
                    .headers(headers -> headers.setBasicAuth(client.getClientId(), client.getClientSecret()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .exchangeToMono(response -> response.bodyToMono(JSON)
                            .flatMap(body -> response.statusCode().is2xxSuccessful() ? issued(body) : refused(body)))
                    .onErrorMap(e -> !(e instanceof com.asrevo.cvhome.errors.BaseException),
                            e -> ImpersonationUnavailableException.of(e, UAA_REGISTRATION));
        });
    }

    private static Mono<Issued> issued(Map<String, Object> body) {
        try {
            String token = String.valueOf(body.get(ACCESS_TOKEN));
            Map<String, Object> claims = SignedJWT.parse(token).getPayload().toJSONObject();
            return Mono.just(new Issued(token, claims, String.valueOf(body.get("act_mode")),
                    String.valueOf(body.get("acting_as"))));
        } catch (ParseException malformed) {
            return Mono.error(ImpersonationUnavailableException.of(malformed, UAA_REGISTRATION));
        }
    }

    private static Mono<Issued> refused(Map<String, Object> body) {
        Object error = body.get("error");
        Object description = body.get("error_description");
        return Mono.error(ImpersonationRefusedException.of(error == null ? null : error.toString(),
                description == null ? null : description.toString()));
    }

    /**
     * Tenancy, asked as the impersonated principal, whether this store is one it acts in. The same endpoint the
     * console calls to enter a store, so what refuses there refuses here — another organization's store, a
     * suspended one — and a refusal takes the token it was made with down with it.
     */
    private Mono<Void> probeStore(Issued issued, String storeId) {
        String url = UriComponentsBuilder.fromUriString(urls.getServiceUrl(TENANCY)).path(ROUTER)
                .queryParam("store", storeId).build().toUriString();
        return tenancy.get().uri(url)
                .headers(headers -> headers.setBearerAuth(issued.token()))
                .exchangeToMono(response -> response.statusCode().is2xxSuccessful()
                        ? response.releaseBody()
                        : response.releaseBody().then(revoke(issued.token()))
                                .then(Mono.error(ImpersonationStoreNotTargetsException.of(storeId,
                                        response.statusCode().value()))))
                .onErrorMap(e -> !(e instanceof com.asrevo.cvhome.errors.BaseException),
                        e -> ImpersonationUnavailableException.of(e, TENANCY));
    }

    private Swap swap(ServerWebExchange exchange, WebSession session, OAuth2AuthenticationToken operator,
                      OAuth2AuthorizedClient operatorClient, Issued issued, StartImpersonation request) {
        Set<GrantedAuthority> authorities = UaaJwtGrantedAuthoritiesConverter.getGrantedAuthorities(issued.claims());
        Map<String, Object> attributes = new LinkedHashMap<>(issued.claims());
        attributes.put(PREFERRED_USERNAME, issued.actingAs());
        attributes.put(PRINCIPAL_KEY, String.format("%s/%s", issued.subject(), operator.getName()));
        DefaultOAuth2User merchant = new DefaultOAuth2User(authorities, attributes, PRINCIPAL_KEY);
        OAuth2AuthenticationToken impersonated = new OAuth2AuthenticationToken(merchant, authorities, UAA_REGISTRATION);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, issued.token(),
                issued.issuedAt(), issued.expiresAt());
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(operatorClient.getClientRegistration(),
                impersonated.getName(), accessToken);
        SecurityContext original = session.getAttribute(SECURITY_CONTEXT);
        if (original == null) {
            original = new SecurityContextImpl(operator);
        }
        ImpersonationView view = new ImpersonationView(issued.actingAs(), issued.subject(), request.storeId(),
                issued.mode(), request.reason(), issued.expiresAt());
        ImpersonationSession stash = new ImpersonationSession(original, operatorClient, impersonated, issued.token(), view);
        session.getAttributes().put(ImpersonationSession.ATTRIBUTE, stash);
        session.getAttributes().put(SECURITY_CONTEXT, new SecurityContextImpl(impersonated));
        log.info("{} is acting as {} on store {} ({}) until {}", operator.getName(), issued.actingAs(),
                request.storeId(), issued.mode(), issued.expiresAt());
        return new Swap(stash, client);
    }

    private Mono<Void> restore(WebSession session, ImpersonationSession active, String how) {
        session.getAttributes().remove(ImpersonationSession.ATTRIBUTE);
        session.getAttributes().put(SECURITY_CONTEXT, active.original());
        log.info("{} stopped acting as {} ({})", active.original().getAuthentication().getName(),
                active.view().actingAs(), how);
        Authentication operator = active.original().getAuthentication();
        return clients.removeAuthorizedClient(UAA_REGISTRATION, active.impersonated().getName())
                .then(Mono.defer(() -> active.originalClient() == null ? Mono.empty()
                        : clients.saveAuthorizedClient(active.originalClient(), operator)))
                .then(revoke(active.token()));
    }

    /**
     * Best effort, and logged rather than failed: the session swap and the token's own expiry are what end an
     * impersonation. Revocation is what writes the "ended" audit row, and a missed one is a gap in the trail worth
     * a warning, not a reason to leave the operator stuck as somebody else.
     */
    private Mono<Void> revoke(String token) {
        return registrations.findByRegistrationId(IMPERSONATION_REGISTRATION).flatMap(client -> {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add(TOKEN, token);
            return uaa.post().uri(revocationUri(client))
                    .headers(headers -> headers.setBasicAuth(client.getClientId(), client.getClientSecret()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .exchangeToMono(ClientResponse::releaseBody);
        }).onErrorResume(e -> {
            log.warn("Could not revoke an impersonated token at uaa; the audit trail has no 'ended' row for it", e);
            return Mono.empty();
        });
    }

    /** Spring's provider details know the token endpoint and not the revocation one; they are siblings in uaa. */
    static String revocationUri(ClientRegistration client) {
        String tokenUri = client.getProviderDetails().getTokenUri();
        String path = UriComponentsBuilder.fromUriString(tokenUri).build().getPath();
        return UriComponentsBuilder.fromUriString(tokenUri)
                .replacePath(path == null ? "/oauth2/revoke" : path.replaceFirst("/token$", "/revoke"))
                .build().toUriString();
    }

    /** What uaa answered with, decoded. The token came straight from uaa over the client channel; no re-verification. */
    record Issued(String token, Map<String, Object> claims, String mode, String actingAs) {

        String subject() {
            return String.valueOf(claims.get("sub"));
        }

        Instant issuedAt() {
            return instant("iat");
        }

        Instant expiresAt() {
            return instant("exp");
        }

        private Instant instant(String claim) {
            Object value = claims.get(claim);
            return value instanceof Number seconds ? Instant.ofEpochSecond(seconds.longValue()) : null;
        }

    }

    private record Swap(ImpersonationSession session, OAuth2AuthorizedClient client) {
    }

}
