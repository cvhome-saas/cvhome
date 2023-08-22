package com.asrevo.cvhome.certificatemanager.controllor;

import com.asrevo.cvhome.certificatemanager.domain.DomainReference;
import com.asrevo.cvhome.certificatemanager.service.DomainReferenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/domain-reference")
@AllArgsConstructor
@Slf4j
public class DomainReferenceController {

    private final DomainReferenceService domainReferenceService;

    @GetMapping(value = "get-domain-reference", params = "domain")
    @PreAuthorize("hasAnyAuthority('ROLE_MICROSERVICE')")
    public Optional<DomainReference> getDomainReference(@RequestParam("domain") String domain) {
        log.info("getting domain reference for domain {}", domain);
        Optional<DomainReference> domainReference = domainReferenceService.getDomainReference(domain);
        if (domainReference.isEmpty()) {
            log.warn("did not get domain reference for domain {}", domain);
        }
        return domainReference;
    }
}
