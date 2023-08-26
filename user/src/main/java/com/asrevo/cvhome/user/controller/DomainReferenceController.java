package com.asrevo.cvhome.user.controller;

import com.asrevo.cvhome.user.domain.DomainReference;
import com.asrevo.cvhome.user.domain.DomainType;
import com.asrevo.cvhome.user.service.DomainReferenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/domain-reference")
@AllArgsConstructor
@Slf4j
public class DomainReferenceController {

    private final DomainReferenceService domainReferenceService;

    @PostMapping
    public Mono<DomainReference> save(@RequestBody DomainReference domainReference, @AuthenticationPrincipal Principal principal) {
        Mono<SecurityContext> context = ReactiveSecurityContextHolder.getContext();
        log.info("saving domain {} reference for user {}", domainReference.domain(), principal.getName());
        return domainReferenceService.save(domainReference, principal.getName());
    }

    @GetMapping(value = "get-domain-reference", params = "domain")
    @PreAuthorize("hasAnyAuthority('ROLE_MICROSERVICE')")
    public Mono<DomainReference> getDomainReference(@RequestParam("domain") String domain) {
        log.info("getting domain reference for domain {}", domain);
        return domainReferenceService.getDomainReference(domain);
    }

    @GetMapping(value = "get-all-domain-references")
    public Flux<DomainReference> getAllDomainReferences(@AuthenticationPrincipal Principal principal, @RequestParam(value = "domainType", required = false) DomainType domainType) {
        log.info("getting all domain reference for user {}", principal.getName());
        return domainReferenceService.getAllDomainReferences(principal.getName(), domainType);
    }
}
