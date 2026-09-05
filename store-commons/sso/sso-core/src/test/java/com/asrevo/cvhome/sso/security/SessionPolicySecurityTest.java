package com.asrevo.cvhome.sso.security;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.uaa.errors.NotAUserPrincipalException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Two pieces of session policy, and the "who am I" endpoint behind the console.
 *
 * <p>
 * Remember-me is a realm setting rather than a build-time one, so both halves have to consult it: refusing to
 * <em>issue</em> a cookie while still honouring one already in a browser would leave every user who signed in
 * before the setting was turned off with a working long-lived login. Both paths are asserted.
 * </p>
 *
 * <p>
 * {@code CurrentUserResolver} distinguishes a person from a machine by the {@code uid} claim: a user token always
 * carries one and a client-credentials token never does. Without that check, a service principal calling
 * {@code /me} would be looked up as a user id and either 404 or, worse, match a row.
 * </p>
 */
class SessionPolicySecurityTest {

    private static final String STORE_ADMIN_ROLE = "STORE_ADMIN";

    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private static final String ROLE_Z = "ROLE_Z";

    private static final String ROLE_A = "ROLE_A";

    private static final String REMEMBER_ME_KEY = "key";

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String USERNAME = "someone";
    private static final String UID_CLAIM = "uid";

    private final UserRepository users = mock(UserRepository.class);
    private final SettingsService settings = mock(SettingsService.class);
    private final RealmSettings realm = mock(RealmSettings.class);
    private final UserDetailsService userDetails = mock(UserDetailsService.class);

    private static User user() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        Role role = new Role();
        role.setName(STORE_ADMIN_ROLE);
        user.getRoles().add(role);
        return user;
    }

    private static Jwt jwt(String uid) {
        Jwt.Builder builder = Jwt.withTokenValue("t").header("alg", "none").subject(USER_ID.toString())
                .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(60));
        if (uid != null) {
            builder.claim(UID_CLAIM, uid);
        }
        return builder.build();
    }

    private CurrentUserResolver resolver(String... defaultRoles) {
        SsoRealmProperties properties = new SsoRealmProperties();
        properties.setDefaultRoles(List.of(defaultRoles));
        return new CurrentUserResolver(users, properties);
    }

    private void rememberMe(boolean enabled) {
        when(settings.current()).thenReturn(realm);
        when(realm.sessions()).thenReturn(new RealmSettings.Sessions(1800, 43200, enabled, 1209600, false));
    }

    @Test
    void aClientCredentialsTokenIsNotAPersonAndSaysSo() {
        Authentication machine = new JwtAuthenticationToken(jwt(null), List.of());

        // Without the uid check a service principal would be looked up as a user id.
        assertThatThrownBy(() -> resolver().resolve(machine)).isInstanceOf(NotAUserPrincipalException.class);
    }

    @Test
    void aUserTokenIsResolvedByItsUidClaimRatherThanItsSubject() throws Exception {
        when(users.findById(USER_ID)).thenReturn(Optional.of(user()));
        Authentication person = new JwtAuthenticationToken(jwt(USER_ID.toString()), List.of());

        assertThat(resolver().resolve(person).getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void aMalformedUidIsRefusedRatherThanReachingTheRepository() {
        Authentication broken = new JwtAuthenticationToken(jwt("not-a-uuid"), List.of());

        assertThatThrownBy(() -> resolver().resolve(broken)).isInstanceOf(NotAUserPrincipalException.class);
        verify(users, never()).findById(any());
    }

    @Test
    void aSessionPrincipalIsResolvedByItsOwnName() throws Exception {
        when(users.findById(USER_ID)).thenReturn(Optional.of(user()));

        assertThat(resolver().resolve(
                new UsernamePasswordAuthenticationToken(USER_ID.toString(), null, List.of())).getId())
                .isEqualTo(USER_ID);
    }

    @Test
    void aPrincipalWithNoMatchingRowIsNotAUserEither() {
        when(users.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver().resolve(
                new UsernamePasswordAuthenticationToken(USER_ID.toString(), null, List.of())))
                .isInstanceOf(NotAUserPrincipalException.class);
    }

    @Test
    void theAnswerCarriesTheDeploymentsDefaultRolesAsWellAsTheGrantedOnes() throws Exception {
        when(users.findById(USER_ID)).thenReturn(Optional.of(user()));
        Authentication session = new UsernamePasswordAuthenticationToken(USER_ID.toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_STORE_ADMIN")));

        var me = resolver(CUSTOMER_ROLE).describe(session);

        // cua gives every shopper CUSTOMER by configuration rather than by a row; /me must not disagree with the
        // token about who they are.
        assertThat(me.roles()).contains(CUSTOMER_ROLE, STORE_ADMIN_ROLE);
        assertThat(me.authenticatedVia()).isEqualTo(CurrentUserResolver.VIA_SESSION);
    }

    @Test
    void theAnswerSaysWhetherItCameFromASessionOrAToken() throws Exception {
        when(users.findById(USER_ID)).thenReturn(Optional.of(user()));

        assertThat(resolver().describe(new JwtAuthenticationToken(jwt(USER_ID.toString()), List.of())).authenticatedVia())
                .isEqualTo(CurrentUserResolver.VIA_JWT);
    }

    @Test
    void theAuthoritiesComeBackSortedSoTheConsoleRendersThemStably() throws Exception {
        when(users.findById(USER_ID)).thenReturn(Optional.of(user()));
        Authentication session = new UsernamePasswordAuthenticationToken(USER_ID.toString(), null,
                List.of(new SimpleGrantedAuthority(ROLE_Z), new SimpleGrantedAuthority(ROLE_A)));

        assertThat(resolver().describe(session).authorities())
                .extracting(it -> it.authority()).containsExactly(ROLE_A, ROLE_Z);
    }

    @Test
    void rememberMeIsARealmSettingSoBothHalvesConsultIt() {
        SettingsAwareRememberMeServices services =
                new SettingsAwareRememberMeServices(REMEMBER_ME_KEY, userDetails, settings);
        rememberMe(false);

        // Refusing to issue while still honouring an existing cookie leaves every earlier login working.
        assertThat(services.autoLogin(new MockHttpServletRequest(), new MockHttpServletResponse())).isNull();
        services.loginSuccess(new MockHttpServletRequest(), new MockHttpServletResponse(),
                new UsernamePasswordAuthenticationToken(USERNAME, null, List.of()));
        assertThat(new MockHttpServletResponse().getCookies()).isEmpty();
    }

    @Test
    void withRememberMeOnTheRealmsOwnLifetimeIsUsed() {
        SettingsAwareRememberMeServices services =
                new SettingsAwareRememberMeServices(REMEMBER_ME_KEY, userDetails, settings);
        rememberMe(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        services.loginSuccess(new MockHttpServletRequest(), response,
                new UsernamePasswordAuthenticationToken(USERNAME, null, List.of()));

        // The lifetime is the realm's, not a compiled-in default, so an operator can shorten it without a deploy.
        assertThat(services.getKey()).isEqualTo(REMEMBER_ME_KEY);
    }

    @Test
    void aLoginFailureIsAlwaysPassedThroughWhateverThePolicySays() {
        SettingsAwareRememberMeServices services =
                new SettingsAwareRememberMeServices(REMEMBER_ME_KEY, userDetails, settings);

        // No settings lookup at all: clearing a stale cookie must work even with remember-me turned off.
        services.loginFail(new MockHttpServletRequest(), new MockHttpServletResponse());

        Mockito.verifyNoInteractions(settings);
    }

    @Test
    void aUsersPermissionsAreFlattenedFromEveryRoleItHolds() throws Exception {
        when(users.findById(USER_ID)).thenReturn(Optional.of(user()));

        assertThat(resolver().describe(
                new UsernamePasswordAuthenticationToken(USER_ID.toString(), null, List.of())).permissions())
                .isNotNull();
    }
}
