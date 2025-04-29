package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.dto.StoreDomainList;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class RouterController {
    public final InternalStoreService internalStoreService;

    @GetMapping("public/ask-for-tls")
    @ConditionalOnApiStatus
    public ResponseEntity<Object> ask(Domain domain) {
        return Optional.ofNullable(internalStoreService.getReferenceByDomain(domain))
                .map(it -> ResponseEntity.ok().build())
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("store-id-by-domain")
    @ConditionalOnApiStatus
    public ManagerStoreId getStoreIdByDomain(@RequestParam Domain domain) {
        return internalStoreService.getReferenceByDomain(domain);
    }

    @GetMapping("store-pod-by-store-id")
    @ConditionalOnApiStatus
    public Pod getStorePodByStoreId(@RequestParam ManagerStoreId store) {
        return internalStoreService.getStorePod(store);
    }

    @GetMapping("allocates")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.DOMAIN.LIST')")
    @ConditionalOnApiStatus
    public Mono<StoreDomainList> allocatedDomains(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store) {
        return Mono.just(internalStoreService.domains(store));
    }

    @PostMapping("allocate")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.DOMAIN.CREATE')")
    @ConditionalOnApiStatus
    public Mono<Void> allocate(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, Domain domain) {
        internalStoreService.addDomain(store, domain);
        return Mono.empty();
    }

    @DeleteMapping("remove")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.DOMAIN.DELETE')")
    @ConditionalOnApiStatus
    public Mono<Void> remove(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, Domain domain) {
        internalStoreService.removeDomain(store, domain);
        return Mono.empty();
    }

}
