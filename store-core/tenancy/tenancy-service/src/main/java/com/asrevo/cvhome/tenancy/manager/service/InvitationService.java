package com.asrevo.cvhome.tenancy.manager.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.CreatedInvitationDto;
import com.asrevo.cvhome.tenancy.commons.dto.InvitationDto;
import com.asrevo.cvhome.tenancy.commons.dto.InvitationStatus;
import com.asrevo.cvhome.tenancy.errors.InvitationAlreadyExistsException;
import com.asrevo.cvhome.tenancy.errors.InvitationNotUsableException;
import com.asrevo.cvhome.tenancy.manager.entity.OrgInvitationEntity;
import com.asrevo.cvhome.tenancy.manager.repository.OrgInvitationRepository;
import com.asrevo.cvhome.tenancy.manager.repository.OrgMemberRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Invitations to join an organization.
 *
 * <p>
 * <strong>Nothing here sends an email, because this platform has no mail sender.</strong> Creating an invitation
 * returns a one-time token to the caller and stores only its hash; the console shows a link for an operator to
 * send by whatever means they already use. That is a deliberate shape, not a stopgap — it means the token has the
 * same handling as a password reset, and adding delivery later changes who transports the link, not how it is
 * stored or verified.
 * </p>
 *
 * <p>
 * The token is a 256-bit random value, compared by SHA-256 hash. It is never logged, never returned by any read,
 * and cannot be recovered from the database — losing it means issuing a new invitation.
 * </p>
 */
@Service
@Slf4j
public class InvitationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrgInvitationRepository invitationRepository;

    private final OrgMemberRepository memberRepository;

    private final TenancyAuditService auditService;

    private final Duration validity;

    public InvitationService(OrgInvitationRepository invitationRepository, OrgMemberRepository memberRepository,
                             TenancyAuditService auditService,
                             @Value("${com.asrevo.cvhome.tenancy.invitation.validity:P7D}") Duration validity) {
        this.invitationRepository = invitationRepository;
        this.memberRepository = memberRepository;
        this.auditService = auditService;
        this.validity = validity;
    }

    /**
     * @throws InvitationAlreadyExistsException that address already has a live invitation to this organization.
     *                                          A partial unique index enforces it too, so two concurrent invites
     *                                          cannot both mint a working token
     */
    @Transactional
    public CreatedInvitationDto invite(ManagerOrgId org, String email, String role, String actor)
            throws InvitationAlreadyExistsException {
        String normalised = normalise(email);
        if (invitationRepository.findByOrgIdAndEmailAndStatus(org, normalised, InvitationStatus.PENDING)
                .filter(OrgInvitationEntity::usable)
                .isPresent()) {
            throw InvitationAlreadyExistsException.of(normalised);
        }
        String token = newToken();
        OrgInvitationEntity saved = invitationRepository.save(OrgInvitationEntity.create(org, normalised, role,
                hash(token), Instant.now().plus(validity), actor));
        auditService.record(AuditEntityType.INVITATION, saved.getId(), "CREATE", null, InvitationStatus.PENDING,
                actor, String.format("invited %s as %s", normalised, role));
        // The token is deliberately absent from this line. It is a bearer credential; a log that carries it is a
        // log that grants membership to anyone who can read it.
        log.info("Invitation {} created for {} to org {}", saved.getId(), normalised, org);
        return new CreatedInvitationDto(toDto(saved), token);
    }

    /**
     * Accepts an invitation and adds the user to the organization.
     *
     * <p>
     * Idempotent in the way that matters: the membership insert tolerates a user who is already a member, so a
     * double-submitted accept does not fail. The invitation itself moves to ACCEPTED once and cannot be reused.
     * </p>
     *
     * @throws InvitationNotUsableException no such token, or it is spent, revoked or expired — one error for all
     *                                      four, so the endpoint cannot be used to probe which tokens existed
     */
    @Transactional
    public InvitationDto accept(String token, String userId) throws InvitationNotUsableException {
        OrgInvitationEntity invitation = invitationRepository.findByTokenHash(hash(token))
                .orElseThrow(InvitationNotUsableException::create);
        if (!invitation.usable()) {
            if (invitation.getStatus() == InvitationStatus.PENDING) {
                // Lazily relabelled: there is no job whose only purpose is to move expired rows, and the read has
                // to check expiry anyway.
                invitation.setStatus(InvitationStatus.EXPIRED);
                invitationRepository.save(invitation);
            }
            log.info("Invitation {} was offered but is {}", invitation.getId(), invitation.getStatus());
            throw InvitationNotUsableException.create();
        }
        memberRepository.add(invitation.getOrgId().getId().toString(), userId, invitation.getRole(),
                invitation.getCreatedBy());
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        invitation.setAcceptedBy(userId);
        OrgInvitationEntity saved = invitationRepository.save(invitation);
        auditService.record(AuditEntityType.MEMBER, invitation.getOrgId(), "JOIN", null, invitation.getRole(),
                userId, String.format("accepted invitation %s", invitation.getId()));
        return toDto(saved);
    }

    @Transactional
    public InvitationDto revoke(ManagerOrgId org, String invitationId, String actor)
            throws InvitationNotUsableException {
        OrgInvitationEntity invitation = invitationRepository.findByOrgId(org)
                .stream()
                .filter(it -> invitationId.equals(idOf(it)))
                .findFirst()
                .orElseThrow(InvitationNotUsableException::create);
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw InvitationNotUsableException.create();
        }
        InvitationStatus from = invitation.getStatus();
        invitation.setStatus(InvitationStatus.REVOKED);
        OrgInvitationEntity saved = invitationRepository.save(invitation);
        auditService.record(AuditEntityType.INVITATION, invitation.getId(), "REVOKE", from, InvitationStatus.REVOKED,
                actor, null);
        return toDto(saved);
    }

    /**
     * Issues a fresh token for an outstanding invitation, invalidating the previous one.
     *
     * <p>
     * Rotating rather than resending the same token is the point: "resend" usually means the first link went
     * astray, and a link that went astray should stop working.
     * </p>
     */
    @Transactional
    public CreatedInvitationDto resend(ManagerOrgId org, String email, String role, String actor)
            throws InvitationAlreadyExistsException {
        Optional<OrgInvitationEntity> existing =
                invitationRepository.findByOrgIdAndEmailAndStatus(org, normalise(email), InvitationStatus.PENDING);
        existing.ifPresent(it -> {
            it.setStatus(InvitationStatus.REVOKED);
            invitationRepository.save(it);
            auditService.record(AuditEntityType.INVITATION, it.getId(), "ROTATE", InvitationStatus.PENDING,
                    InvitationStatus.REVOKED, actor, "superseded by a resend");
        });
        return invite(org, email, role, actor);
    }

    public List<InvitationDto> list(ManagerOrgId org) {
        return invitationRepository.findByOrgId(org).stream().map(InvitationService::toDto).toList();
    }

    private static String normalise(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required of every JVM; if it is genuinely missing, failing loudly is the only honest move.
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /**
     * The bare id, not the value object's {@code toString()} — otherwise the invitation id reaches callers as
     * {@code ManagerStoreId[id=…]}, which is both ugly and impossible to pass back to {@code revoke}.
     */
    private static String idOf(OrgInvitationEntity entity) {
        return entity.getId() == null ? null : entity.getId().getId().toString();
    }

    private static InvitationDto toDto(OrgInvitationEntity entity) {
        return new InvitationDto(idOf(entity), entity.getOrgId(), entity.getEmail(),
                entity.getRole(), entity.getStatus(), entity.getExpiresAt(), entity.getCreatedAt(),
                entity.getCreatedBy());
    }

}
