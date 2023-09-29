package com.asrevo.cvhome.domainownership.controller;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domainownership.commons.domain.DomainType;
import com.asrevo.cvhome.domainownership.commons.dto.*;
import com.asrevo.cvhome.domainownership.domain.OwnerEntity;
import com.asrevo.cvhome.domainownership.service.DomainOwnerShipService;
import com.asrevo.cvhome.domainownership.service.DomainService;
import com.asrevo.cvhome.domainownership.service.OwnerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


@RestController
@RequestMapping("api/v1/domain-ownership")
@AllArgsConstructor
@Slf4j
public class DomainOwnerShipController {
    private final DomainService domainService;
    private final DomainOwnerShipService domainOwnerShipService;
    private final OwnerService ownerService;

    @PostMapping("get-proving")
    public ProvingResponse getReference(@RequestBody Domain domain, @AuthenticationPrincipal Principal principal) {
        return new ProvingResponse(domain.getProvingDomain(), principal.getName());
    }

    @PostMapping("check-availability")
    public AvailabilityResponse checkAvailability(@RequestBody Domain domain) {
        return domainService.checkAvailability(domain);
    }

    @PostMapping("register")
    public RegisterDomainResponse register(@RequestBody RegisterDomainRequest request, @AuthenticationPrincipal JwtAuthenticationToken principal) {
        if (request.domainType() == DomainType.APPLICATION_RESERVED) {
            if (principal.getAuthorities().stream().noneMatch(it -> "ROLE_ADMIN".equals(it.getAuthority()))) {
                throw new RuntimeException("you don't have authorities to create " + request.domainType().name() + " domain");
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
}
