package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.service.RouterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class RouterController {
    public final RouterService routerService;

    @GetMapping("public/ask-for-tls")
    public ResponseEntity<Object> ask(Domain domain) {
        return Optional.ofNullable(routerService.getReferenceByDomain(domain))
                .map(it -> ResponseEntity.ok().build())
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("allocation-by-domain")
    public ManagerStoreId getAllocationByDomain(@RequestParam Domain domain) {
        return routerService.getReferenceByDomain(domain);
    }

    @GetMapping("allocates")
    public List<Domain> allocates(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store) {
        return routerService.allocations(store);
    }

    @PostMapping("allocate")
    public void allocate(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, Domain domain) {
        routerService.create(domain,store);
    }

    @DeleteMapping("remove")
    public void remove(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, Domain domain) {
        routerService.remove(domain, store);
    }

}
