package com.asrevo.cvhome.certificatemanager.controllor;

import com.asrevo.cvhome.certificatemanager.domain.DomainReference;
import com.asrevo.cvhome.certificatemanager.service.DomainReferenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("api/domain-reference")
@AllArgsConstructor
@Slf4j
public class DomainReferenceController {

    private final DomainReferenceService domainReferenceService;

    @GetMapping(value = "get-domain-reference", params = "domain")
    public Optional<DomainReference> getDomainReference(@RequestParam("domain") String domain) {
        return domainReferenceService.getDomainReference(domain);
    }
}
