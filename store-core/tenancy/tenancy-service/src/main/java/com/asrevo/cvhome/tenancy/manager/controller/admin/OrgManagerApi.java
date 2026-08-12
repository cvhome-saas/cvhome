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
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.errors.IllegalLifecycleTransitionException;
import com.asrevo.cvhome.tenancy.errors.OrgNotFoundException;
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

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @GetMapping("find-one")

    public ManagerOrgDto findOne(@RequestParam ManagerOrgId id) {
        return internalOrgService.findOne(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @PostMapping("create")

    public ReadableUser create(@RequestBody CreateOrgRequest request)
            throws UaaConflictException, UaaApiUnavailableException {
        return signupService.createOrgUser(request);
    }

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    @PostMapping("change-password")

    public void changePassword(@RequestParam ManagerOrgId id, @RequestBody UserPassword request)
            throws UaaUserNotFoundException, UaaApiUnavailableException {
        userAccountService.changePassword(id.toString(), request);
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
