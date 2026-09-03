package com.asrevo.cvhome.sso.invitation;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.sso.audit.AuditActorResolver;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.PasswordResetToken;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.dto.AcceptedLink;
import com.asrevo.cvhome.sso.dto.IssuedLink;
import com.asrevo.cvhome.sso.dto.LinkPreview;
import com.asrevo.cvhome.sso.password.PasswordService;
import com.asrevo.cvhome.sso.repo.PasswordResetTokenRepository;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.service.AdminService;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.sso.token.TokenRevocationService;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.errors.ResetTokenNotUsableException;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.errors.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Administrator-issued password-reset links.
 *
 * <p>
 * There is no "forgot password" self-service: uaa cannot send mail, and a public endpoint that issued links for any
 * email address would be an enumeration oracle. An administrator issues the link, sees it once, and the outbox
 * hands it to the delivery consumer. Issuing a new link retires every earlier one for the account.
 * </p>
 *
 * <p>
 * Using the link sets the password through the policy and ends every session and token the account holds — a reset
 * is what happens when the old password may be in the wrong hands.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    static final String RESET_LINK = "RESET_LINK";

    private final UserRepository users;

    private final PasswordResetTokenRepository tokens;

    private final AdminService admin;

    private final PasswordService passwords;

    private final AuditService audit;

    private final AuditActorResolver actors;

    private final SettingsService settings;

    private final LinkBuilder links;

    private final LinksProperties properties;

    private final SessionAdminService sessions;

    private final TokenRevocationService authorizations;

    private final Clock clock;

    @Transactional
    public IssuedLink createLink(UUID userId, boolean revokeSessions) throws UserNotFoundException, SuperAdminImmutableException {
        User user = admin.getNonSuperAdmin(userId);
        Instant now = clock.instant();
        tokens.findByUserIdAndUsedAtIsNullAndRevokedAtIsNull(userId).forEach(previous -> {
            previous.setRevokedAt(now);
            tokens.save(previous);
        });
        Instant expiresAt = now.plus(properties.resetValidity());
        String token = OneTimeTokens.newToken();
        PasswordResetToken issued = tokens.save(PasswordResetToken.issue(user, OneTimeTokens.hash(token), now, expiresAt,
                actors.current().name()));
        String link = links.passwordReset(token);
        user.resetLinkIssued(link, expiresAt, settings.current().defaultLocale());
        users.save(user);
        if (revokeSessions) {
            revokeEverything(user);
        }
        audit.record(AuditRecord.of(AuditEventType.USER_PASSWORD_RESET_LINK_ISSUED).user(user.getId(), user.getUsername())
                .detail(String.format("%s, expires %s%s", issued.getId(), expiresAt, revokeSessions ? ", sessions revoked" : "")));
        log.info("Password reset link {} issued for {}", issued.getId(), user.getUsername());
        return new IssuedLink(admin.toDto(user), null, link, expiresAt);
    }

    @Transactional(readOnly = true)
    public LinkPreview preview(String token) throws ResetTokenNotUsableException {
        PasswordResetToken reset = usable(token);
        User user = users.findById(reset.getUserId()).orElseThrow(ResetTokenNotUsableException::create);
        return new LinkPreview(LinkPreview.PASSWORD_RESET, user.getUsername(), user.getEmail(), user.getFirstName(),
                reset.getExpiresAt(), LinkPreview.PasswordRules.of(settings.current().password()));
    }

    @Transactional
    public AcceptedLink accept(String token, String password)
            throws ResetTokenNotUsableException, PasswordPolicyViolationException, PasswordReusedException,
            PasswordCompromisedException {
        PasswordResetToken reset = usable(token);
        User user = users.findById(reset.getUserId())
                .filter(User::isEnabled)
                .orElseThrow(ResetTokenNotUsableException::create);
        passwords.setPassword(user, password);
        users.save(user);
        reset.setUsedAt(clock.instant());
        tokens.save(reset);
        revokeEverything(user);
        audit.record(AuditRecord.of(AuditEventType.USER_PASSWORD_RESET).user(user.getId(), user.getUsername()).reason(RESET_LINK));
        log.info("Password reset link {} used by {}", reset.getId(), user.getUsername());
        return new AcceptedLink(user.getUsername(), InvitationService.LOGIN_URL);
    }

    private void revokeEverything(User user) {
        sessions.revokeAll(user, null);
        authorizations.revokeAllForUser(user);
    }

    private PasswordResetToken usable(String token) throws ResetTokenNotUsableException {
        PasswordResetToken reset = tokens.findByTokenHash(OneTimeTokens.hash(token))
                .orElseThrow(ResetTokenNotUsableException::create);
        if (!reset.usable(clock.instant())) {
            log.info("Password reset link {} was offered but is spent, revoked or expired", reset.getId());
            throw ResetTokenNotUsableException.create();
        }
        return reset;
    }

}
