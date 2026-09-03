package com.asrevo.cvhome.sso.invitation;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import com.asrevo.cvhome.sso.audit.AuditActor;
import com.asrevo.cvhome.sso.audit.AuditActorResolver;
import com.asrevo.cvhome.sso.audit.AuditActorType;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.PasswordResetToken;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.dto.IssuedLink;
import com.asrevo.cvhome.sso.dto.UserDto;
import com.asrevo.cvhome.sso.password.PasswordService;
import com.asrevo.cvhome.sso.repo.PasswordResetTokenRepository;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.service.AdminService;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.sso.token.TokenRevocationService;
import com.asrevo.cvhome.uaa.errors.ResetTokenNotUsableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");

    private static final String USERNAME = "someone";

    private static final String PASSWORD = "Reset-Passw0rd-2026";

    private static final String TOK = "tok";

    private static final String SPENT = "spent";

    private static final String EXPIRED = "expired";

    private final UserRepository users = mock(UserRepository.class);

    private final PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);

    private final AdminService admin = mock(AdminService.class);

    private final PasswordService passwords = mock(PasswordService.class);

    private final SessionAdminService sessions = mock(SessionAdminService.class);

    private final TokenRevocationService authorizations = mock(TokenRevocationService.class);

    private final AuditActorResolver actors = mock(AuditActorResolver.class);

    private final SettingsService settings = mock(SettingsService.class);

    private final PasswordResetService service = new PasswordResetService(users, tokens, admin, passwords,
            mock(AuditService.class), actors, settings,
            new LinkBuilder(AuthorizationServerSettings.builder().issuer("http://uaa.test").build()),
            new LinksProperties(Duration.ofDays(7), Duration.ofHours(1), false), sessions, authorizations,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        user = User.create(USERNAME, "someone@example.com", null, null);
        user.setPasswordHash("{hash}old");
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokens.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actors.current()).thenReturn(new AuditActor(AuditActorType.USER, "id", "super-admin"));
        RealmSettings realm = mock(RealmSettings.class);
        when(realm.defaultLocale()).thenReturn("en");
        when(settings.current()).thenReturn(realm);
        when(admin.getNonSuperAdmin(user.getId())).thenReturn(user);
        when(admin.toDto(any(User.class))).thenAnswer(invocation -> dto(invocation.getArgument(0)));
    }

    @Test
    void aNewLinkRetiresEveryEarlierOneAndOnlyRevokesSessionsWhenAsked() throws Exception {
        PasswordResetToken earlier = PasswordResetToken.issue(user, "h", NOW.minusSeconds(60), NOW.plusSeconds(60), null);
        when(tokens.findByUserIdAndUsedAtIsNullAndRevokedAtIsNull(user.getId())).thenReturn(List.of(earlier));

        IssuedLink issued = service.createLink(user.getId(), false);

        assertThat(earlier.getRevokedAt()).isEqualTo(NOW);
        assertThat(issued.link()).startsWith("http://uaa.test/reset-password?token=");
        assertThat(issued.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
        assertThat(issued.invitation()).isNull();
        verify(sessions, never()).revokeAll(any(), any());

        service.createLink(user.getId(), true);

        verify(sessions).revokeAll(USERNAME, null);
        verify(authorizations).revokeAllForUser(USERNAME);
    }

    @Test
    void acceptSetsThePasswordSpendsTheTokenAndSignsTheAccountOutEverywhere() throws Exception {
        PasswordResetToken token = PasswordResetToken.issue(user, OneTimeTokens.hash(TOK), NOW, NOW.plusSeconds(60), null);
        when(tokens.findByTokenHash(OneTimeTokens.hash(TOK))).thenReturn(Optional.of(token));

        assertThat(service.accept(TOK, PASSWORD).username()).isEqualTo(USERNAME);

        verify(passwords).setPassword(user, PASSWORD);
        assertThat(token.getUsedAt()).isEqualTo(NOW);
        verify(sessions).revokeAll(USERNAME, null);
        verify(authorizations).revokeAllForUser(USERNAME);
    }

    @Test
    void aSpentOrExpiredTokenIsOneAndTheSameRefusal() {
        PasswordResetToken spent = PasswordResetToken.issue(user, OneTimeTokens.hash(SPENT), NOW, NOW.plusSeconds(60), null);
        spent.setUsedAt(NOW.minusSeconds(1));
        PasswordResetToken expired = PasswordResetToken.issue(user, OneTimeTokens.hash(EXPIRED), NOW, NOW.minusSeconds(1), null);
        when(tokens.findByTokenHash(OneTimeTokens.hash(SPENT))).thenReturn(Optional.of(spent));
        when(tokens.findByTokenHash(OneTimeTokens.hash(EXPIRED))).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.preview(SPENT)).isInstanceOf(ResetTokenNotUsableException.class);
        assertThatThrownBy(() -> service.preview(EXPIRED)).isInstanceOf(ResetTokenNotUsableException.class);
        assertThatThrownBy(() -> service.preview("unknown")).isInstanceOf(ResetTokenNotUsableException.class);
    }

    private static UserDto dto(User u) {
        return new UserDto(UUID.randomUUID(), u.getUsername(), u.getEmail(), null, null, true, u.status(NOW), false, Set.of(),
                u.getMetadata(), null, null, null, null, 0, null, null);
    }

}
