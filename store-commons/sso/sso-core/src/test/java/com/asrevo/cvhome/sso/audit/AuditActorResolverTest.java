package com.asrevo.cvhome.sso.audit;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class AuditActorResolverTest {

    private static final String SUB = "sub";

    private static final String ORG1_ADMIN = "org1-admin";

    private static final String UID = "u-1";

    private static final String ADMIN_SDK = "admin-sdk";

    private static final String SUPER_ADMIN = "super-admin";

    private final AuditActorResolver resolver = new AuditActorResolver();

    private static Jwt jwt(Map<String, Object> claims) {
        return new Jwt("t", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "RS256"), claims);
    }

    @Test
    void aUserTokenIsAUserAndAClientTokenAClient() {
        AuditActor user = resolver.resolve(new JwtAuthenticationToken(jwt(Map.of(SUB, ORG1_ADMIN, "uid", UID))));
        AuditActor client = resolver.resolve(new JwtAuthenticationToken(jwt(Map.of(SUB, ADMIN_SDK))));

        assertThat(user).isEqualTo(new AuditActor(AuditActorType.USER, UID, ORG1_ADMIN));
        assertThat(client).isEqualTo(new AuditActor(AuditActorType.CLIENT, ADMIN_SDK, ADMIN_SDK));
    }

    @Test
    void aSessionIsAUserAndNothingIsAnonymous() {
        AuditActor session = resolver.resolve(UsernamePasswordAuthenticationToken.authenticated(SUPER_ADMIN, null, List.of()));
        AuditActor nobody = resolver.resolve(new AnonymousAuthenticationToken("k", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThat(session.type()).isEqualTo(AuditActorType.USER);
        assertThat(session.name()).isEqualTo(SUPER_ADMIN);
        assertThat(nobody).isEqualTo(AuditActor.ANONYMOUS);
        assertThat(resolver.resolve(null)).isEqualTo(AuditActor.ANONYMOUS);
    }

}
