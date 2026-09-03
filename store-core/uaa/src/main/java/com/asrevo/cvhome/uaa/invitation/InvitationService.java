package com.asrevo.cvhome.uaa.invitation;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.audit.AuditActorResolver;
import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.audit.AuditTargetType;
import com.asrevo.cvhome.uaa.domain.Invitation;
import com.asrevo.cvhome.uaa.domain.InvitationStatus;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.dto.AcceptedLink;
import com.asrevo.cvhome.uaa.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.dto.InvitationDto;
import com.asrevo.cvhome.uaa.dto.InviteUserRequest;
import com.asrevo.cvhome.uaa.dto.IssuedLink;
import com.asrevo.cvhome.uaa.dto.LinkPreview;
import com.asrevo.cvhome.uaa.dto.UserDto;
import com.asrevo.cvhome.uaa.errors.EmailTakenException;
import com.asrevo.cvhome.uaa.errors.InvitationNotUsableException;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.errors.RoleNotAssignableException;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.errors.UserNotFoundException;
import com.asrevo.cvhome.uaa.errors.UserNotPendingException;
import com.asrevo.cvhome.uaa.errors.UsernameTakenException;
import com.asrevo.cvhome.uaa.password.PasswordService;
import com.asrevo.cvhome.uaa.repo.InvitationRepository;
import com.asrevo.cvhome.uaa.repo.UserRepository;
import com.asrevo.cvhome.uaa.service.AdminService;
import com.asrevo.cvhome.uaa.settings.SettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Invitations: a pending account plus a one-time link for its first password.
 *
 * <p>
 * <strong>Nothing here sends anything.</strong> Issuing a link stores the token's hash, returns the link once in the
 * response, and registers an {@code InvitationIssuedEvent} on the account, which the outbox delivers to whatever
 * consumer is subscribed — today the log, later the platform's delivery service. The administrator's request never
 * waits on a transport.
 * </p>
 *
 * <p>
 * Accepting is public and rate limited. Every failure — unknown, expired, revoked, spent, or an account that was
 * disabled in the meantime — is the same 404, so the endpoint cannot be used to learn which tokens existed.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    static final String LOGIN_URL = "/login";

    static final String RESEND = "superseded by a resend";

    private final UserRepository users;

    private final InvitationRepository invitations;

    private final AdminService admin;

    private final PasswordService passwords;

    private final AuditService audit;

    private final AuditActorResolver actors;

    private final SettingsService settings;

    private final LinkBuilder links;

    private final LinksProperties properties;

    private final Clock clock;

    /** Creates the account pending (no password), grants the roles, and issues its first link. */
    @Transactional
    public IssuedLink invite(InviteUserRequest req)
            throws UsernameTakenException, EmailTakenException, RoleNotFoundException, RoleNotAssignableException,
            UserNotFoundException, SuperAdminImmutableException {
        String username = req.username() == null || req.username().isBlank() ? req.email() : req.username();
        UserDto created = admin.createAccount(new CreateUserRequest(username, req.email(), req.firstName(), req.lastName(),
                null, req.roles(), req.metadata()));
        User user = users.findById(created.id()).orElseThrow(() -> UserNotFoundException.of(created.id()));
        return issue(user);
    }

    /**
     * Rotates a pending account's invitation: the previous link stops working, a new one is issued. "Resend" usually
     * means the first link went astray, and a link that went astray should stop working.
     */
    @Transactional
    public IssuedLink resend(UUID userId) throws UserNotFoundException, SuperAdminImmutableException, UserNotPendingException {
        User user = admin.getNonSuperAdmin(userId);
        if (!user.isPending()) {
            throw UserNotPendingException.of(userId);
        }
        pendingOf(userId).ifPresent(previous -> revoke(previous, user, RESEND));
        return issue(user);
    }

    /** Withdraws the pending invitation. The account stays, pending, until it is deleted or invited again. */
    @Transactional
    public void revoke(UUID userId) throws UserNotFoundException, SuperAdminImmutableException, InvitationNotUsableException {
        User user = admin.getNonSuperAdmin(userId);
        Invitation pending = pendingOf(userId).orElseThrow(InvitationNotUsableException::create);
        revoke(pending, user, null);
    }

    @Transactional(readOnly = true)
    public Page<InvitationDto> list(InvitationStatus status, Pageable pageable) {
        Page<Invitation> page = status == null ? invitations.findAllByOrderByCreatedAtDesc(pageable)
                : invitations.findByStatusOrderByCreatedAtDesc(status, pageable);
        Set<UUID> ids = page.getContent().stream().map(Invitation::getUserId).collect(Collectors.toSet());
        Map<UUID, String> usernames = users.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, _) -> a));
        return page.map(invitation -> toDto(invitation, usernames.getOrDefault(invitation.getUserId(), null)));
    }

    /** What the accept page shows before a password is typed. */
    @Transactional
    public LinkPreview preview(String token) throws InvitationNotUsableException {
        Invitation invitation = usable(token);
        User user = users.findById(invitation.getUserId()).orElseThrow(InvitationNotUsableException::create);
        return new LinkPreview(LinkPreview.INVITATION, user.getUsername(), user.getEmail(), user.getFirstName(),
                invitation.getExpiresAt(), LinkPreview.PasswordRules.of(settings.current().password()));
    }

    /**
     * Sets the first password and activates the account. The email is marked verified: the link reached the address
     * the invitation was sent to, and that is what verification means.
     */
    @Transactional
    public AcceptedLink accept(String token, String password)
            throws InvitationNotUsableException, PasswordPolicyViolationException, PasswordReusedException,
            PasswordCompromisedException {
        Invitation invitation = usable(token);
        User user = users.findById(invitation.getUserId())
                .filter(User::isEnabled)
                .orElseThrow(InvitationNotUsableException::create);
        passwords.setPassword(user, password);
        user.setEmailVerified(true);
        users.save(user);
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(clock.instant());
        invitations.save(invitation);
        audit.record(AuditRecord.of(AuditEventType.INVITATION_ACCEPTED).user(user.getId(), user.getUsername())
                .detail(invitation.getId().toString()));
        audit.record(AuditRecord.of(AuditEventType.USER_ACTIVATED).user(user.getId(), user.getUsername()));
        log.info("Invitation {} accepted by {}", invitation.getId(), user.getUsername());
        return new AcceptedLink(user.getUsername(), LOGIN_URL);
    }

    private IssuedLink issue(User user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.invitationValidity());
        String token = OneTimeTokens.newToken();
        String actor = actors.current().name();
        Invitation invitation = invitations.save(Invitation.issue(user, OneTimeTokens.hash(token), now, expiresAt, actor));
        String link = links.invitation(token);
        user.invitationIssued(link, expiresAt, settings.current().defaultLocale());
        users.save(user);
        audit.record(AuditRecord.of(AuditEventType.INVITATION_CREATED)
                .target(AuditTargetType.INVITATION, invitation.getId().toString(), user.getUsername())
                .detail(String.format("expires %s", expiresAt)));
        // The token is deliberately absent from this line: it is a bearer credential.
        log.info("Invitation {} issued for {}", invitation.getId(), user.getUsername());
        return new IssuedLink(admin.toDto(user), toDto(invitation, user.getUsername()), link, expiresAt);
    }

    private void revoke(Invitation invitation, User user, String detail) {
        invitation.setStatus(InvitationStatus.REVOKED);
        invitations.save(invitation);
        audit.record(AuditRecord.of(AuditEventType.INVITATION_REVOKED)
                .target(AuditTargetType.INVITATION, invitation.getId().toString(), user.getUsername())
                .detail(detail));
    }

    private Optional<Invitation> pendingOf(UUID userId) {
        return invitations.findByUserIdAndStatus(userId, InvitationStatus.PENDING);
    }

    /**
     * The invitation behind a token, if it can still be used. An expired one is relabelled on the way — there is no
     * job whose only purpose is to move expired rows, and this read has to check expiry anyway.
     */
    private Invitation usable(String token) throws InvitationNotUsableException {
        Invitation invitation = invitations.findByTokenHash(OneTimeTokens.hash(token))
                .orElseThrow(InvitationNotUsableException::create);
        Instant now = clock.instant();
        if (invitation.usable(now)) {
            return invitation;
        }
        if (invitation.getStatus() == InvitationStatus.PENDING) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitations.save(invitation);
        }
        log.info("Invitation {} was offered but is {}", invitation.getId(), invitation.getStatus());
        throw InvitationNotUsableException.create();
    }

    static InvitationDto toDto(Invitation invitation, String username) {
        return new InvitationDto(invitation.getId(), invitation.getUserId(), username, invitation.getEmail(),
                invitation.getStatus(), invitation.getExpiresAt(), invitation.getCreatedAt(), invitation.getCreatedBy(),
                invitation.getAcceptedAt());
    }

    /** For callers that only need to render one row. */
    static Function<Invitation, InvitationDto> withUsername(String username) {
        return invitation -> toDto(invitation, username);
    }

}
