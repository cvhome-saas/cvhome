package com.asrevo.cvhome.uaa.idp;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.audit.AuditTargetType;
import com.asrevo.cvhome.uaa.domain.IdentityProvider;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.domain.UserIdentity;
import com.asrevo.cvhome.uaa.dto.UserIdentityDto;
import com.asrevo.cvhome.uaa.errors.IdentityNotFoundException;
import com.asrevo.cvhome.uaa.errors.LastCredentialException;
import com.asrevo.cvhome.uaa.repo.IdentityProviderRepository;
import com.asrevo.cvhome.uaa.repo.UserIdentityRepository;

import lombok.RequiredArgsConstructor;

/** An account's linked identities, for its owner and for an administrator. */
@Service
@RequiredArgsConstructor
public class UserIdentityService {

    private final UserIdentityRepository identities;

    private final IdentityProviderRepository providers;

    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<UserIdentityDto> list(UUID userId) {
        Map<UUID, IdentityProvider> byId = providers.findAll().stream()
                .collect(Collectors.toMap(IdentityProvider::getId, Function.identity()));
        return identities.findByUserIdOrderByLinkedAtAsc(userId).stream().map(identity -> {
            IdentityProvider p = byId.get(identity.getProviderId());
            return new UserIdentityDto(identity.getId(), p == null ? null : p.getAlias(), p == null ? null : p.getDisplayName(),
                    identity.getSubject(), identity.getEmail(), identity.getLinkedAt(), identity.getLastLoginAt());
        }).toList();
    }

    /**
     * Removes a link. Refused when it is the account's only credential: no password and no other identity leaves
     * nothing to sign in with, and an administrator should set a password first.
     */
    @Transactional
    public void unlink(User user, UUID identityId) throws IdentityNotFoundException, LastCredentialException {
        UserIdentity identity = identities.findById(identityId)
                .filter(i -> i.getUserId().equals(user.getId()))
                .orElseThrow(() -> IdentityNotFoundException.of(identityId));
        if (user.getPasswordHash() == null && identities.countByUserId(user.getId()) <= 1) {
            throw LastCredentialException.create();
        }
        identities.delete(identity);
        String alias = providers.findById(identity.getProviderId()).map(IdentityProvider::getAlias).orElse("?");
        audit.record(AuditRecord.of(AuditEventType.IDENTITY_UNLINKED).user(user.getId(), user.getUsername())
                .target(AuditTargetType.IDP, identity.getProviderId().toString(), alias)
                .detail(String.format("subject %s", identity.getSubject())));
    }

}
