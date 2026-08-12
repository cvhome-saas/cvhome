package com.asrevo.cvhome.tenancy.manager.service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.commons.dto.StoreStatus;
import com.asrevo.cvhome.tenancy.errors.IllegalLifecycleTransitionException;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Suspending, resuming, archiving and deleting a store.
 *
 * <p>
 * The legal moves are one table rather than scattered {@code if}s, so adding a status cannot quietly make it
 * reachable from everywhere. Deleting is soft: billing holds a subscription against the store id and the pod
 * registry holds a placement, so removing the row would orphan both and erase the history of a store that
 * existed.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreLifecycleService {

    /**
     * Where each status may go. DELETED is terminal, and ARCHIVED can only be undone by reactivating — there is
     * deliberately no path from DELETED back to anything.
     */
    private static final String STATUS = "STATUS";

    private static final Map<StoreStatus, Set<StoreStatus>> ALLOWED = Map.of(
            StoreStatus.ACTIVE, EnumSet.of(StoreStatus.SUSPENDED, StoreStatus.ARCHIVED, StoreStatus.DELETED),
            StoreStatus.SUSPENDED, EnumSet.of(StoreStatus.ACTIVE, StoreStatus.ARCHIVED, StoreStatus.DELETED),
            StoreStatus.ARCHIVED, EnumSet.of(StoreStatus.ACTIVE, StoreStatus.DELETED),
            StoreStatus.DELETED, EnumSet.noneOf(StoreStatus.class));

    private final InternalStoreService internalStoreService;

    private final TenancyAuditService auditService;

    @Transactional
    public ManagerStoreDto suspend(UserOrgStoreIdentity identity, StoreMerchantId store, String actor, String reason)
            throws StoreNotFoundException, IllegalLifecycleTransitionException {
        return move(identity, store, StoreStatus.SUSPENDED, actor, reason);
    }

    @Transactional
    public ManagerStoreDto resume(UserOrgStoreIdentity identity, StoreMerchantId store, String actor)
            throws StoreNotFoundException, IllegalLifecycleTransitionException {
        return move(identity, store, StoreStatus.ACTIVE, actor, "resumed by operator");
    }

    @Transactional
    public ManagerStoreDto archive(UserOrgStoreIdentity identity, StoreMerchantId store, String actor)
            throws StoreNotFoundException, IllegalLifecycleTransitionException {
        return move(identity, store, StoreStatus.ARCHIVED, actor, "archived by owner");
    }

    /**
     * Soft delete. The row and its id survive; only the status changes.
     */
    @Transactional
    public ManagerStoreDto delete(UserOrgStoreIdentity identity, StoreMerchantId store, String actor)
            throws StoreNotFoundException, IllegalLifecycleTransitionException {
        return move(identity, store, StoreStatus.DELETED, actor, "deleted by owner");
    }

    private ManagerStoreDto move(UserOrgStoreIdentity identity, StoreMerchantId store, StoreStatus to, String actor,
                                 String detail)
            throws StoreNotFoundException, IllegalLifecycleTransitionException {
        ManagerStoreDto current = internalStoreService.findStore(identity, store);
        StoreStatus from = Objects.requireNonNullElse(current.status(), StoreStatus.ACTIVE);
        if (from == to) {
            // Asking for the state it is already in is not an error — it is what a double-click looks like — but
            // it is still recorded, because "who tried" is the question asked afterwards.
            auditService.record(AuditEntityType.STORE, store, STATUS, from, to, actor,
                    String.format("no-op: %s", detail));
            return current;
        }
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw IllegalLifecycleTransitionException.of(store, from, to);
        }
        ManagerStoreDto updated = internalStoreService.updateStatus(store, to);
        auditService.record(AuditEntityType.STORE, store, STATUS, from, to, actor, detail);
        log.info("Store {} moved {} -> {} by {}", store, from, to, actor);
        return updated;
    }

}
