package com.asrevo.cvhome.tenancy.manager.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.ListOrgQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.errors.IllegalLifecycleTransitionException;
import com.asrevo.cvhome.tenancy.errors.OrgNotFoundException;
import com.asrevo.cvhome.tenancy.errors.OrgOwnerUnknownException;
import com.asrevo.cvhome.tenancy.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.tenancy.manager.service.InternalOrgService;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
import com.asrevo.cvhome.tenancy.manager.service.OrgLifecycleService;
import com.asrevo.cvhome.tenancy.manager.service.SignupService;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/org-manager")
@AllArgsConstructor
@Slf4j
public class OrgManagerApi {

    private final InternalOrgService internalOrgService;

    private final SignupService signupService;

    private final UserAccountService userAccountService;

    private final InternalStoreService internalStoreService;

    private final OrgLifecycleService orgLifecycleService;

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @GetMapping("find-all")

    public Page<ManagerOrgDto> findAllOrg(Pageable pageable) {
        log.info("findAllOrg {}", pageable);
        return internalOrgService.findAll(pageable);
    }

    /**
     * The same listing, narrowed by a search term and a status.
     *
     * <p>
     * A POST carrying a query body rather than parameters on {@link #findAllOrg}, matching
     * {@code StoreManagerApi.list} — the two are the same table twice over and their filters should not be shaped
     * differently for no reason. {@code find-all} stays as it was: it has callers, and a listing with no filter is
     * a real thing to want.
     * </p>
     *
     * <p>
     * The term spans the name and the contact email in one predicate. Almost every organization on the platform is
     * unnamed, so the console lists many of them by email — a box that searched only the name would fail to find
     * exactly the rows on screen.
     * </p>
     */
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @PostMapping("list")
    public Page<ManagerOrgDto> listOrgs(@RequestBody ListOrgQuery query, Pageable pageable) {
        return internalOrgService.findAll(query, pageable);
    }

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @GetMapping("find-one")

    public ManagerOrgDto findOne(@RequestParam ManagerOrgId id) throws OrgNotFoundException {
        return internalOrgService.findOne(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @PostMapping("create")

    public ReadableUser create(@RequestBody CreateOrgRequest request)
            throws UaaConflictException, UaaApiUnavailableException {
        return signupService.createOrgUser(request);
    }

    /**
     * Sets the password of the account that owns an organization.
     *
     * <p>
     * <strong>This had never once worked, in three layers at the same time.</strong> It passed
     * {@code id.toString()} — the <em>organization's</em> 24-character ObjectId — where uaa wants a user id, and
     * uaa's {@code AdminUserController.resetPassword} declares {@code @PathVariable UUID id}, so the request could
     * not even bind. Behind that, {@code ManagerOrgDto.ownerUserId} — the field that would have carried the right
     * id — was written by nothing and was null for every row on the platform. And seller-ui sent {@code password}
     * while {@code UserAccountServiceImpl.changePassword} reads {@code getChangePassword()}, so the value would
     * have been null even had it arrived.
     * </p>
     *
     * <p>
     * The owner is now resolved from the organization, which is what turns the two ids into one hop instead of a
     * type confusion. An organization with no recorded owner answers {@link OrgOwnerUnknownException} — a 422
     * naming the missing fact — rather than resetting nobody's password and reporting success.
     * </p>
     */
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @PostMapping("change-password")

    public void changePassword(@RequestParam ManagerOrgId id, @RequestBody UserPassword request)
            throws UaaUserNotFoundException, UaaApiUnavailableException, OrgNotFoundException,
            OrgOwnerUnknownException {
        String ownerUserId = internalOrgService.findOne(id).ownerUserId();
        if (ownerUserId == null || ownerUserId.isBlank()) {
            throw OrgOwnerUnknownException.of(id);
        }
        userAccountService.changePassword(ownerUserId, request);
    }

    /**
     * Lists any organization's stores by id, so it is super-admin only like the rest of this controller. It was the
     * one method here with no annotation, which let any authenticated principal enumerate any org's stores by
     * passing its id.
     */
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @PostMapping("rename")
    public ManagerOrgDto rename(@RequestParam ManagerOrgId id, @RequestParam String name,
                                Authentication authentication) throws OrgNotFoundException {
        return orgLifecycleService.rename(id, name, actorOf(authentication));
    }

    /**
     * Closes an organization and, with it, every store it owns.
     *
     * <p>
     * The stores are not written to. {@code InternalStoreService.requireOperable} reads the org's status as well
     * as the store's, so suspension takes effect everywhere at once instead of fanning out writes that drift when
     * one of them fails.
     * </p>
     */
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @PostMapping("suspend")
    public ManagerOrgDto suspend(@RequestParam ManagerOrgId id, @RequestParam(required = false) String reason,
                                 Authentication authentication)
            throws OrgNotFoundException, IllegalLifecycleTransitionException {
        return orgLifecycleService.suspend(id, actorOf(authentication),
                reason == null ? "suspended by operator" : reason);
    }

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @PostMapping("resume")
    public ManagerOrgDto resume(@RequestParam ManagerOrgId id, Authentication authentication)
            throws OrgNotFoundException, IllegalLifecycleTransitionException {
        return orgLifecycleService.resume(id, actorOf(authentication));
    }

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @PostMapping("close")
    public ManagerOrgDto close(@RequestParam ManagerOrgId id, Authentication authentication)
            throws OrgNotFoundException, IllegalLifecycleTransitionException {
        return orgLifecycleService.close(id, actorOf(authentication));
    }

    private static String actorOf(Authentication authentication) {
        return authentication == null ? "unknown" : authentication.getName();
    }

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @GetMapping("stores")
    public Page<ManagerStoreDto> findAllStores(@RequestParam ManagerOrgId id, Pageable pageable) {
        return internalStoreService.findAll(id, pageable);
    }

}
