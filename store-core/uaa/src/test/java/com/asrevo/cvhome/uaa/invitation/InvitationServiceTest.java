package com.asrevo.cvhome.uaa.invitation;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.test.util.ReflectionTestUtils;

import com.asrevo.cvhome.uaa.audit.AuditActor;
import com.asrevo.cvhome.uaa.audit.AuditActorResolver;
import com.asrevo.cvhome.uaa.audit.AuditActorType;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.domain.Invitation;
import com.asrevo.cvhome.uaa.domain.InvitationStatus;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.dto.InviteUserRequest;
import com.asrevo.cvhome.uaa.dto.IssuedLink;
import com.asrevo.cvhome.uaa.dto.UserDto;
import com.asrevo.cvhome.uaa.errors.InvitationNotUsableException;
import com.asrevo.cvhome.uaa.errors.UserNotPendingException;
import com.asrevo.cvhome.uaa.events.InvitationIssuedEvent;
import com.asrevo.cvhome.uaa.password.PasswordService;
import com.asrevo.cvhome.uaa.repo.InvitationRepository;
import com.asrevo.cvhome.uaa.repo.UserRepository;
import com.asrevo.cvhome.uaa.service.AdminService;
import com.asrevo.cvhome.uaa.settings.RealmSettings;
import com.asrevo.cvhome.uaa.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tokens are hashed at rest, links carry the token once, the account records the event, and every refusal on the
 * public side is the same exception.
 */
class InvitationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");

    private static final String EMAIL = "new@example.com";

    private static final String PASSWORD = "Invited-Passw0rd-2026";

    private static final String TOKEN_PARAM = "token=";

    private static final String FIRST_NAME = "New";

    private static final String ACTOR = "super-admin";

    private static final String TOK = "tok";

    private static final String OLD = "old";

    private static final String DIS = "dis";

    private final UserRepository users = mock(UserRepository.class);

    private final InvitationRepository invitations = mock(InvitationRepository.class);

    private final AdminService admin = mock(AdminService.class);

    private final PasswordService passwords = mock(PasswordService.class);

    private final AuditService audit = mock(AuditService.class);

    private final AuditActorResolver actors = mock(AuditActorResolver.class);

    private final SettingsService settings = mock(SettingsService.class);

    private final InvitationService service = new InvitationService(users, invitations, admin, passwords, audit, actors,
            settings, new LinkBuilder(AuthorizationServerSettings.builder().issuer("http://uaa.test").build()),
            new LinksProperties(Duration.ofDays(7), Duration.ofHours(1), false), Clock.fixed(NOW, ZoneOffset.UTC));

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        user = User.create(EMAIL, EMAIL, FIRST_NAME, null);
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invitations.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actors.current()).thenReturn(new AuditActor(AuditActorType.USER, "id", ACTOR));
        RealmSettings realm = mock(RealmSettings.class);
        when(realm.defaultLocale()).thenReturn("en");
        when(settings.current()).thenReturn(realm);
        when(admin.getNonSuperAdmin(user.getId())).thenReturn(user);
        when(admin.toDto(any(User.class))).thenAnswer(invocation -> dto(invocation.getArgument(0)));
        when(admin.createAccount(any())).thenReturn(dto(user));
    }

    @Test
    void inviteStoresTheHashAndReturnsTheLinkOnce() throws Exception {
        IssuedLink issued = service.invite(new InviteUserRequest(null, EMAIL, FIRST_NAME, null, Set.of(), Map.of()));

        ArgumentCaptor<Invitation> saved = ArgumentCaptor.forClass(Invitation.class);
        verify(invitations).save(saved.capture());
        String token = issued.link().substring(issued.link().indexOf(TOKEN_PARAM) + TOKEN_PARAM.length());
        assertThat(saved.getValue().getTokenHash()).isEqualTo(OneTimeTokens.hash(token)).isNotEqualTo(token);
        assertThat(issued.link()).startsWith("http://uaa.test/accept-invitation?token=");
        assertThat(issued.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
        assertThat(issued.invitation().status()).isEqualTo(InvitationStatus.PENDING);
        // The mocked repository never drains the aggregate, so the created event is still there too.
        assertThat(events(user)).last().isInstanceOf(InvitationIssuedEvent.class)
                .extracting(event -> ((InvitationIssuedEvent) event).link()).isEqualTo(issued.link());
    }

    @Test
    void acceptSetsThePasswordVerifiesTheEmailAndSpendsTheInvitation() throws Exception {
        Invitation invitation = pending(NOW.plusSeconds(60));
        when(invitations.findByTokenHash(OneTimeTokens.hash(TOK))).thenReturn(Optional.of(invitation));

        assertThat(service.accept(TOK, PASSWORD).username()).isEqualTo(EMAIL);

        verify(passwords).setPassword(user, PASSWORD);
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation.getAcceptedAt()).isEqualTo(NOW);
    }

    @Test
    void anExpiredInvitationIsRelabelledAndRefused() throws Exception {
        Invitation invitation = pending(NOW.minusSeconds(1));
        when(invitations.findByTokenHash(OneTimeTokens.hash(OLD))).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.accept(OLD, PASSWORD)).isInstanceOf(InvitationNotUsableException.class);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
        verify(passwords, never()).setPassword(any(), any());
    }

    @Test
    void aDisabledAccountCannotAcceptAndLearnsNothing() {
        user.setEnabled(false);
        when(invitations.findByTokenHash(OneTimeTokens.hash(DIS))).thenReturn(Optional.of(pending(NOW.plusSeconds(60))));

        assertThatThrownBy(() -> service.accept(DIS, PASSWORD)).isInstanceOf(InvitationNotUsableException.class);
    }

    @Test
    void resendRotatesThePendingInvitation() throws Exception {
        Invitation previous = pending(NOW.plusSeconds(60));
        when(invitations.findByUserIdAndStatus(user.getId(), InvitationStatus.PENDING)).thenReturn(Optional.of(previous));

        IssuedLink issued = service.resend(user.getId());

        assertThat(previous.getStatus()).isEqualTo(InvitationStatus.REVOKED);
        assertThat(issued.invitation().id()).isNotEqualTo(previous.getId());
    }

    @Test
    void resendRefusesAnAccountThatAlreadyHasAPassword() {
        user.setPasswordHash("{hash}x");

        assertThatThrownBy(() -> service.resend(user.getId())).isInstanceOf(UserNotPendingException.class);
    }

    private Invitation pending(Instant expiresAt) {
        return Invitation.issue(user, OneTimeTokens.hash("whatever"), NOW.minusSeconds(120), expiresAt, ACTOR);
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> events(User target) {
        return (Collection<Object>) ReflectionTestUtils.invokeMethod(target, "domainEvents");
    }

    private static UserDto dto(User u) {
        return new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName(), u.isEnabled(),
                u.status(NOW), u.isEmailVerified(), Set.of(), u.getMetadata(), null, null, null, null, 0, null, null);
    }

}
