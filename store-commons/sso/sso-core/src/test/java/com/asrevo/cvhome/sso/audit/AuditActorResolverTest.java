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

import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.security.PrincipalNames;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditActorResolverTest {

    private static final String SUB = "sub";

    private static final String ORG1_ADMIN = "org1-admin";

    private static final String UID_CLAIM = "uid";

    private static final String ADMIN_SDK = "admin-sdk";

    private static final String SUPER_ADMIN = "super-admin";

    private final UserRepository users = mock(UserRepository.class);

    private final AuditActorResolver resolver = new AuditActorResolver(new PrincipalNames(users));

    private static User account(String username) {
        User account = new User();
        account.setId(java.util.UUID.randomUUID());
        account.setUsername(username);
        return account;
    }

    private static Jwt jwt(Map<String, Object> claims) {
        return new Jwt("t", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "RS256"), claims);
    }

    @Test
    void aUserTokenIsAUserAndAClientTokenAClient() {
        // A user token's subject is the account id too, so the name comes from the account, not from the claim.
        User account = account(ORG1_ADMIN);
        when(users.findById(account.getId())).thenReturn(java.util.Optional.of(account));
        String uid = account.getId().toString();

        AuditActor user = resolver.resolve(new JwtAuthenticationToken(jwt(Map.of(SUB, uid, UID_CLAIM, uid))));
        AuditActor client = resolver.resolve(new JwtAuthenticationToken(jwt(Map.of(SUB, ADMIN_SDK))));

        assertThat(user).isEqualTo(new AuditActor(AuditActorType.USER, uid, ORG1_ADMIN));
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

    /**
     * The principal name a session actually carries is the account id. An audit row read by a person has to name
     * the account, so the id is looked up; a name that is not an id — a client, or a login that failed before
     * anyone was identified — stays as it is.
     */
    @Test
    void anAccountIdIsShownAsItsUsername() {
        User account = account(SUPER_ADMIN);
        when(users.findById(account.getId())).thenReturn(java.util.Optional.of(account));

        AuditActor actor = resolver.resolve(UsernamePasswordAuthenticationToken
                .authenticated(account.getId().toString(), null, List.of()));

        assertThat(actor).isEqualTo(new AuditActor(AuditActorType.USER, account.getId().toString(), SUPER_ADMIN));
    }

}
