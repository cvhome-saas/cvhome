package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.service.PodSelection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/pod")
@AllArgsConstructor
@Slf4j
public class PodController {
    private final PodSelection podSelection;

    @GetMapping("list")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')")
    @ConditionalOnApiStatus
    public Mono<List<Pod>> findAllPods(@OrgStorePrincipalInfo UserOrgStoreIdentity identity) {
        if (identity.isSuperAdmin()) {
            return Mono.just(podSelection.listAllPods());
        } else {
            return Mono.just(podSelection.listPrivatePods(identity.org()));
        }
    }
}
