package com.asrevo.cvhome.tenancy.org.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.errors.PodNotFoundException;
import com.asrevo.cvhome.tenancy.org.service.PodService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/pod")
@AllArgsConstructor
@Slf4j
public class PodController {

    private final PodService podService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN') or hasAuthority('SCOPE_STORE_CORE')")
    public Page<Pod> findAllPods(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, Pageable pageable) {
        return identity.isSuperAdmin() ? podService.listAllPods(pageable)
                : podService.listAllPods(identity.org(), pageable);
    }

    @GetMapping("list")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN') or hasAuthority('SCOPE_STORE_CORE')")
    public List<Pod> listPods(@OrgStorePrincipalInfo UserOrgStoreIdentity identity) {
        Pageable pageable = Pageable.unpaged();
        return identity.isSuperAdmin() ? podService.listAllPods(pageable).toList()
                : podService.listAllPods(identity.org(), pageable).toList();
    }

    /** Matches {@link #listPods}; it was the one read here with no annotation, exposing any pod's endpoint by id. */
    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN') or hasAuthority('SCOPE_STORE_CORE')")
    public Pod find(@PathVariable PodId id) {
        return podService.pod(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Pod create(@RequestBody Pod pod) {
        return podService.save(pod);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Pod update(@PathVariable PodId id, @RequestBody Pod pod) throws PodNotFoundException {
        return podService.update(id, pod);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void delete(@PathVariable PodId id) {
        podService.delete(id);
    }

}
