package com.asrevo.cvhome.cua.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.sso.domain.UserStatus;
import com.asrevo.cvhome.sso.dto.UserCounts;
import com.asrevo.cvhome.sso.dto.UserDto;
import com.asrevo.cvhome.sso.dto.UserSearch;
import com.asrevo.cvhome.sso.service.AdminService;
import com.asrevo.cvhome.sso.session.SessionSummary;
import com.asrevo.cvhome.uaa.errors.SessionNotFoundException;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.errors.UserNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * A store's shoppers, as its own merchant administers them.
 *
 * <p>
 * Deliberately narrower than the platform's user administration. A store's accounts arrive by registration or by
 * signing in with a provider, so there is no create and no invite here; roles are the deployment's configuration
 * ({@code default-roles}), so there is no role assignment; and a merchant cannot set a shopper's password —
 * resetting it is the shopper's own flow, from the storefront. What is left is what running a shop actually
 * needs: finding an account, seeing its state, unlocking it, ending its sessions, and removing it.
 * </p>
 *
 * <p>
 * Every row is a {@code @TenantId} row, so this cannot see another store's shoppers even by id: the realm is the
 * one the edge resolved from the host, and an id from elsewhere is simply not found. {@code merchantStore} is
 * what the permission check needs — whether this operator may administer this store.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/private/shoppers")
@RequiredArgsConstructor
public class MerchantShopperController {

    private static final String MERCHANT = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUA.*')";

    private final AdminService shoppers;

    @PreAuthorize(MERCHANT)
    @GetMapping
    public Page<UserDto> list(StoreMerchantId merchantStore, @RequestParam(required = false) String q,
                              @RequestParam(required = false) UserStatus status,
                              @PageableDefault(size = 20) Pageable pageable) {
        return shoppers.getUsers(new UserSearch(q, status, null, Map.of()), pageable);
    }

    @PreAuthorize(MERCHANT)
    @GetMapping("/counts")
    public UserCounts counts(StoreMerchantId merchantStore) {
        return shoppers.counts();
    }

    @PreAuthorize(MERCHANT)
    @GetMapping("/{id}")
    public UserDto get(StoreMerchantId merchantStore, @PathVariable UUID id) throws UserNotFoundException {
        return shoppers.getUser(id);
    }

    /** Ends every session and every token the account holds, as disabling always has. */
    @PreAuthorize(MERCHANT)
    @PostMapping("/{id}/disable")
    public void disable(StoreMerchantId merchantStore, @PathVariable UUID id)
            throws UserNotFoundException, SuperAdminImmutableException {
        shoppers.disableUser(id);
    }

    @PreAuthorize(MERCHANT)
    @PostMapping("/{id}/enable")
    public void enable(StoreMerchantId merchantStore, @PathVariable UUID id)
            throws UserNotFoundException, SuperAdminImmutableException {
        shoppers.enableUser(id);
    }

    /** Clears a lockout after too many wrong passwords. Idempotent. */
    @PreAuthorize(MERCHANT)
    @PostMapping("/{id}/unlock")
    public void unlock(StoreMerchantId merchantStore, @PathVariable UUID id)
            throws UserNotFoundException, SuperAdminImmutableException {
        shoppers.unlock(id);
    }

    @PreAuthorize(MERCHANT)
    @GetMapping("/{id}/sessions")
    public List<SessionSummary> sessions(StoreMerchantId merchantStore, @PathVariable UUID id)
            throws UserNotFoundException {
        return shoppers.listSessions(id);
    }

    @PreAuthorize(MERCHANT)
    @DeleteMapping("/{id}/sessions/{sessionId}")
    public void revokeSession(StoreMerchantId merchantStore, @PathVariable UUID id, @PathVariable String sessionId)
            throws UserNotFoundException, SessionNotFoundException {
        shoppers.revokeSession(id, sessionId);
    }

    @PreAuthorize(MERCHANT)
    @DeleteMapping("/{id}/sessions")
    public Map<String, Integer> revokeSessions(StoreMerchantId merchantStore, @PathVariable UUID id)
            throws UserNotFoundException {
        return Map.of("revoked", shoppers.revokeSessions(id));
    }

    /**
     * Removes the account outright.
     *
     * <p>
     * A merchant is the data controller for their shoppers and this platform is the processor, so an erasure
     * request has to be answerable — and until now there was no path that answered it. It is the account and its
     * credentials that go; orders and invoices are the store's own records and live in other services.
     * </p>
     */
    @PreAuthorize(MERCHANT)
    @DeleteMapping("/{id}")
    public void delete(StoreMerchantId merchantStore, @PathVariable UUID id)
            throws UserNotFoundException, SuperAdminImmutableException {
        shoppers.delete(id);
    }

}
