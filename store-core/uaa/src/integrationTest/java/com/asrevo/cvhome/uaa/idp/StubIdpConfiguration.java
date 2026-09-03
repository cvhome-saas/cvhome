package com.asrevo.cvhome.uaa.idp;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The stub provider's endpoints, on the same port as uaa, outside every real chain. Authorize answers a code at once
 * (there is no login page to drive); token mints an id token for whoever {@link StubIdp} says; jwks and userinfo do
 * what their names say. The issuer is whatever host the request arrived on, which the test registers.
 */
@TestConfiguration(proxyBeanMethods = false)
public class StubIdpConfiguration {

    /** The most recent nonce the authorize endpoint saw: the token request carries no state to look it up by. */
    static final java.util.concurrent.atomic.AtomicReference<String> LAST = new java.util.concurrent.atomic.AtomicReference<>();

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain stubIdpSecurity(HttpSecurity http) throws Exception {
        http.securityMatcher(String.format("%s/**", StubIdp.PATH))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Registered by being a member class of an imported configuration — no {@code @Bean} method, or the same
     * controller is registered twice and the mapping is ambiguous. It needs the {@code @Controller} stereotype:
     * Spring 7 no longer treats a bare type-level {@code @RequestMapping} as a handler, and without it every request
     * here falls through to the SPA's catch-all router and answers index.html.
     */
    @RestController
    @RequestMapping(StubIdp.PATH)
    public static class StubIdpController {

        private static final String STATE = "state";

        @GetMapping("/authorize")
        public ResponseEntity<Void> authorize(@RequestParam("redirect_uri") String redirectUri, @RequestParam String state,
                                              @RequestParam(required = false) String nonce) {
            StubIdp.rememberNonce(state, nonce);
            LAST.set(nonce);
            // encode(), not build(true): the container hands the parameters back decoded, and the state is base64url
            // with padding, which "already encoded" would reject.
            URI back = UriComponentsBuilder.fromUriString(redirectUri).queryParam("code", StubIdp.CODE)
                    .queryParam(STATE, state).encode().build().toUri();
            return ResponseEntity.status(HttpStatus.FOUND).location(back).build();
        }

        /** {@code client_id} is optional: with client_secret_basic it is in the Authorization header, not the form. */
        @PostMapping("/token")
        public Map<String, Object> token(@RequestParam(value = "client_id", required = false) String clientId,
                                         @RequestParam String code, HttpServletRequest request) {
            if (!StubIdp.CODE.equals(code)) {
                throw new IllegalArgumentException("wrong code");
            }
            String issuer = issuer(request);
            // The nonce travelled with the state; the state is not on the token request, so use the last one seen.
            String nonce = StubIdp.nonceFor(request.getParameter(STATE));
            return Map.of("access_token", StubIdp.ACCESS_TOKEN, "token_type", "Bearer", "expires_in", 300,
                    "id_token", StubIdp.idToken(issuer, clientId == null ? StubIdp.CLIENT_ID : clientId,
                            nonce == null ? LAST.get() : nonce));
        }

        @GetMapping("/jwks")
        public Map<String, Object> jwks() {
            return StubIdp.jwks().toJSONObject();
        }

        @GetMapping("/userinfo")
        public Map<String, Object> userinfo() {
            return StubIdp.claims();
        }

        static String issuer(HttpServletRequest request) {
            return String.format("%s://%s:%d%s", request.getScheme(), request.getServerName(), request.getServerPort(),
                    StubIdp.PATH);
        }

    }

}
