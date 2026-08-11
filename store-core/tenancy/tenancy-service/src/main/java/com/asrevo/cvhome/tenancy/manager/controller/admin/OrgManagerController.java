package com.asrevo.cvhome.tenancy.manager.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.tenancy.manager.service.InternalOrgService;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
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
public class OrgManagerController {

    private final InternalOrgService internalOrgService;

    private final SignupService signupService;

    private final UserAccountService userAccountService;

    private final InternalStoreService internalStoreService;

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

    @GetMapping("stores")

    public Page<ManagerStoreDto> findAllStores(@RequestParam ManagerOrgId id, Pageable pageable) {
        return internalStoreService.findAll(id, pageable);
    }

}
