package com.asrevo.cvhome.domain.controller;

import com.asrevo.cvhome.domain.domain.DomainReference;
import com.asrevo.cvhome.domain.service.DomainReferenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/domain-reference")
@AllArgsConstructor
@Slf4j
public class DomainReferenceController {

    private final DomainReferenceService domainReferenceService;
//    private final DomainReferenceAcmService domainReferenceAcmService;

    @PostMapping
    public Mono<DomainReference> save(@RequestBody DomainReference domainReference, @AuthenticationPrincipal Principal principal) {
        log.info("saving domain {} reference for user {}", domainReference.domain(), principal.getName());
        return domainReferenceService.save(domainReference, principal.getName());
    }

    @GetMapping(value = "get-domain-reference", params = "domain")
    @PreAuthorize("hasAnyAuthority('ROLE_MICROSERVICE')")
    public Mono<DomainReference> getDomainReference(@RequestParam("domain") String domain) {
        log.info("getting domain reference for domain {}", domain);
        return domainReferenceService.getDomainReference(domain);
    }
/*

    @GetMapping(value = "get-all-domain-references")
    public Flux<DomainReferenceOrder> getAllDomainReferences(@AuthenticationPrincipal Principal principal, @RequestParam(value = "domainType", required = false) DomainType domainType) {
        log.info("getting all domain reference for user {}", principal.getName());
        return domainReferenceAcmService.getAllDomainReferences(principal.getName(), domainType);
    }
*/
}
