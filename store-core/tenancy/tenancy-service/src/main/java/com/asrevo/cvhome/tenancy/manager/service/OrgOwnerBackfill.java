package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.tenancy.manager.entity.ManagerOrgEntity;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fills in {@code manager_org.owner_user_id} for the organizations created before anything wrote it.
 *
 * <p>
 * The column and {@code ManagerOrgDto.ownerUserId} both shipped with the lifecycle work and had no writer, so every
 * organization on the platform has a null owner. {@code SignupServiceImpl} writes it from now on; this resolves the
 * history, once, by asking uaa which accounts carry {@code metadata[org] = <id>} — the same filter the console's
 * per-organization Users tab uses, and the only join between the two services that exists.
 * </p>
 *
 * <p>
 * <strong>It is deliberately timid.</strong> It runs after the context is ready rather than during startup, so a
 * uaa outage delays nothing; it only ever writes rows that are null, so running it twice is a no-op; and every
 * failure is logged and skipped rather than propagated, because an organization whose owner cannot be resolved is a
 * row that answers {@code OrgOwnerUnknownException} on a password reset — an honest 422 — and not a reason to keep
 * tenancy from starting.
 * </p>
 *
 * <p>
 * Where uaa reports several accounts for one organization, the {@code ORG_ADMIN} is preferred and the rest are
 * ignored. That is a guess, and it is the same guess {@code SignupServiceImpl} makes when it creates the first
 * administrator with exactly that role. An organization with two org admins is resolved to whichever uaa lists
 * first, and the log line names the ambiguity so it can be corrected by hand.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrgOwnerBackfill {

    /** The role {@code SignupServiceImpl} gives an organization's first administrator. */
    private static final String OWNER_ROLE = "ORG_ADMIN";

    /** uaa's metadata key for the organization, as {@code UserAccountServiceImpl.ORG_KEY} writes it. */
    private static final String ORG_KEY = "org";

    private final ManagerOrgRepository orgRepository;

    private final InternalOrgService internalOrgService;

    private final UserAccountService userAccountService;

    @EventListener(ApplicationReadyEvent.class)
    public void backfill() {
        List<ManagerOrgEntity> pending = orgRepository.findWithoutOwner();
        if (pending.isEmpty()) {
            return;
        }
        log.info("Resolving owners for {} organization(s) with no recorded owner", pending.size());
        int resolved = 0;
        for (ManagerOrgEntity org : pending) {
            resolved += resolve(org) ? 1 : 0;
        }
        log.info("Owner backfill resolved {} of {} organization(s)", resolved, pending.size());
    }

    private boolean resolve(ManagerOrgEntity org) {
        String id = String.valueOf(org.getId().id());
        try {
            Optional<ReadableUser> owner = owner(id);
            if (owner.isEmpty()) {
                log.warn("Org {} has no uaa account carrying metadata[org]={}; leaving its owner unrecorded", id, id);
                return false;
            }
            internalOrgService.recordOwner(org.getId(), owner.get().getId());
            return true;
        } catch (UaaApiUnavailableException e) {
            log.warn("uaa could not be reached while resolving the owner of org {}; it stays unrecorded", id, e);
            return false;
        } catch (Exception e) {
            // Deliberately broad: one unresolvable organization must not stop the other nineteen.
            log.warn("Could not resolve the owner of org {}", id, e);
            return false;
        }
    }

    /**
     * The organization's owner among the accounts uaa reports for it.
     *
     * <p>
     * Sorted so an {@code ORG_ADMIN} wins over a store-scoped account, then by username so the choice is stable
     * across runs rather than dependent on uaa's page order.
     * </p>
     */
    private Optional<ReadableUser> owner(String orgId) throws UaaApiUnavailableException {
        List<ReadableUser> candidates = userAccountService.list(Map.of(ORG_KEY, orgId), 0, 100).getContent();
        if (candidates.size() > 1) {
            log.info("Org {} has {} uaa accounts; preferring the {}", orgId, candidates.size(), OWNER_ROLE);
        }
        return candidates.stream()
                .min(Comparator.comparing((ReadableUser user) -> user.getRoles().contains(OWNER_ROLE) ? 0 : 1)
                        .thenComparing(user -> String.valueOf(user.getUserName())));
    }

}
