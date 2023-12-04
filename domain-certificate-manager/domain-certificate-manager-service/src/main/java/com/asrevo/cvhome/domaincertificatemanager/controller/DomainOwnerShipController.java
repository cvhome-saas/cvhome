package com.asrevo.cvhome.domaincertificatemanager.controller;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainType;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.*;
import com.asrevo.cvhome.domaincertificatemanager.config.AutoOrderDomainsProperties;
import com.asrevo.cvhome.domaincertificatemanager.entity.OwnerEntity;
import com.asrevo.cvhome.domaincertificatemanager.service.DomainOwnerShipService;
import com.asrevo.cvhome.domaincertificatemanager.service.DomainService;
import com.asrevo.cvhome.domaincertificatemanager.service.OwnerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("api/v1/domain-ownership")
@AllArgsConstructor
@Slf4j
public class DomainOwnerShipController {
    private final DomainService domainService;
    private final DomainOwnerShipService domainOwnerShipService;
    private final OwnerService ownerService;
    private final AutoOrderDomainsProperties autoOrderDomainsProperties;

    @PostMapping("get-proving")
    public ProvingResponse getProving(@RequestBody Domain domain, @AuthenticationPrincipal Principal principal) {
        return new ProvingResponse(domain.getProvingDomain(), principal.getName());
    }

    @PostMapping("check-availability")
    public AvailabilityResponse checkAvailability(@RequestBody Domain domain) {
        return domainService.checkAvailability(domain);
    }

    @GetMapping("default-domain")
    public Domain defaultDomain() {
        return autoOrderDomainsProperties.getDefaultDomain();
    }

    @PostMapping("register")
    public RegisterDomainResponse register(@RequestBody RegisterDomainRequest request,
                                           @AuthenticationPrincipal JwtAuthenticationToken principal) {
        if (request.domainType() == DomainType.APPLICATION_RESERVED) {
            if (principal.getAuthorities().stream().noneMatch(it -> "ROLE_ADMIN".equals(it.getAuthority()))) {
                throw new RuntimeException("you don't have authorities to create " + request.domainType().name() + " domain");
            }
        }

        if (request.domain().isWildCard()) {
            if (principal.getAuthorities().stream().noneMatch(it -> "ROLE_ADMIN".equals(it.getAuthority()))) {
                throw new RuntimeException("you don't have authorities to create wildcard domain " + request.domain().domain());
            }
        }

        if (request.includeSubDomains()) {
            if (principal.getAuthorities().stream().noneMatch(it -> "ROLE_ADMIN".equals(it.getAuthority()))) {
                throw new RuntimeException("you don't have authorities to include sup domains " + request.domain().domain());
            }
        }

        IdentityId identityId = switch (request.domainType()) {
            case APPLICATION_RESERVED -> IdentityId.ofSys();
            default -> IdentityId.of(principal.getName());
        };
        OwnerEntity entity = ownerService.getOwnerOrCreate(identityId, principal);
        return domainOwnerShipService.register(request, entity.getId());
    }

    @PostMapping("get-reference")
    public DomainReferenceResponse getReference(@RequestBody Domain domain) {
        return domainService.getReference(domain);
    }

    @PostMapping("change-reference")
    public DomainChangeReferenceResponse changeReference(
            @RequestBody DomainChangeReferenceRequest changeReferenceRequest,
            @AuthenticationPrincipal Principal principal) {
        return domainService.changeDomainReference(changeReferenceRequest, new IdentityId(principal.getName()));
    }

    @GetMapping(value = "get-registered-domains")
    public List<RegisteredDomainResponse> getRegisteredDomains(
            @RequestParam(name = "domainType", required = false) DomainType domainType,
            @AuthenticationPrincipal Principal principal) {
        return domainService.findAllRegisteredDomains(IdentityId.of(principal.getName()), domainType);
    }
}
