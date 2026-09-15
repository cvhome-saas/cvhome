package com.asrevo.cvhome.sso.aot;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import com.asrevo.cvhome.sso.idp.PendingLink;
import com.asrevo.cvhome.sso.security.BrokeredPrincipal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Everything uaa and cua persist about an authentication can be written and read back by a native image — as a
 * Java-serialized session attribute and as an authorization row's JSON.
 *
 * <p>
 * The contents are built the way the flows build them — a password sign-in, a brokered social sign-in, the request
 * saved across it, a pending account link — and serialized through a stream that records every class it writes. A
 * class written but not registered is a native sign-in that answers 500, so the test fails on it: including after a
 * Spring Security upgrade adds a type to the graph, which is how {@code FactorGrantedAuthority} arrived.
 * </p>
 */
class AuthenticationStateRuntimeHintsTest {

    private static final String SUBJECT = "someone";

    private static final String GOOGLE = "google";

    private static final String EMAIL = "a@b.c";

    private static final String CSRF_VALUE = "t";

    /** Collects the class of every object written, the way Java serialization will meet them. */
    private static final class RecordingStream extends ObjectOutputStream {

        private final Set<Class<?>> written = new LinkedHashSet<>();

        RecordingStream() throws IOException {
            super(OutputStream.nullOutputStream());
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) {
            for (Class<?> type = obj.getClass(); type != null && Serializable.class.isAssignableFrom(type);
                 type = type.getSuperclass()) {
                written.add(type);
            }
            return obj;
        }

    }

    private static List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY));
    }

    private static SecurityContextImpl passwordSignIn() {
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.authenticated(
                User.withUsername(SUBJECT).password("{noop}x").authorities(authorities()).build(), null, authorities());
        token.setDetails(new WebAuthenticationDetails("127.0.0.1", "session-1"));
        return new SecurityContextImpl(token);
    }

    private static SecurityContextImpl brokeredSignIn() {
        OidcIdToken idToken = new OidcIdToken("id-token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", SUBJECT));
        BrokeredPrincipal principal = new BrokeredPrincipal(SUBJECT, GOOGLE, Map.of("email", EMAIL), idToken, null);
        return new SecurityContextImpl(new OAuth2AuthenticationToken(principal, authorities(), GOOGLE));
    }

    private static Object savedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
        request.setQueryString("response_type=code&client_id=web-app");
        request.setCookies(new Cookie("XSRF-TOKEN", CSRF_VALUE));
        new HttpSessionRequestCache().saveRequest(request, new MockHttpServletResponse());
        return request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
    }

    private static OAuth2AuthorizationRequest brokeredAuthorizationRequest() {
        return OAuth2AuthorizationRequest.authorizationCode().authorizationUri("https://idp.example.com/authorize")
                .clientId("client").redirectUri("https://uaa.example.com/login/oauth2/code/google")
                .scopes(Set.of("openid", "profile")).state("state").attributes(Map.of("registration_id", GOOGLE))
                .build();
    }

    @Test
    void everyClassASessionWritesIsRegistered() throws IOException {
        RuntimeHints hints = new RuntimeHints();
        new AuthenticationStateRuntimeHints().registerHints(hints, getClass().getClassLoader());

        RecordingStream stream = new RecordingStream();
        for (Object attribute : List.of(passwordSignIn(), brokeredSignIn(), savedRequest(), brokeredAuthorizationRequest(),
                new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", CSRF_VALUE),
                new PendingLink(UUID.randomUUID(), GOOGLE, "Google", SUBJECT, EMAIL, UUID.randomUUID(), SUBJECT))) {
            stream.writeObject(attribute);
        }

        assertThat(stream.written).isNotEmpty()
                .allSatisfy(type -> assertThat(RuntimeHintsPredicates.serialization().onType(type))
                        .as("%s is written to a session but not registered for serialization", type.getName())
                        .accepts(hints));
    }

    @Test
    void jacksonCanBuildTheAuthorityAnAuthorizationRowCarriesThroughItsMixin() throws ClassNotFoundException {
        RuntimeHints hints = new RuntimeHints();
        new AuthenticationStateRuntimeHints().registerHints(hints, getClass().getClassLoader());

        // What the native uaa could not read back out of oauth2_authorization.
        assertThat(RuntimeHintsPredicates.reflection().onType(FactorGrantedAuthority.class)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
                .onType(Class.forName("org.springframework.security.jackson.FactorGrantedAuthorityMixin"))
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(AuthenticationStateRuntimeHints.class::isInstance);
    }

}
