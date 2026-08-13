package com.asrevo.cvhome.merchant.api.v1;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.service.AskTlsService;
import com.asrevo.cvhome.merchant.service.LookupDomainHeadersService;
import com.asrevo.cvhome.merchant.services.merchant.MerchantRoutingService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class RouterController {

    private final MerchantRoutingService merchantRoutingService;

    private final AskTlsService askTlsService;

    private final LookupDomainHeadersService lookupDomainHeadersService;

    @GetMapping("public/ask-for-tls")

    public ResponseEntity<Object> ask(Domain domain) {
        log.info("tls ask: {}", domain);
        if (askTlsService.ask(domain)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("public/lookup-by-domain")

    public Map<String, String> getLookupHeadersByDomain(Domain domain) {
        log.info("header lookup: {}", domain);
        return lookupDomainHeadersService.lookupHeaders(domain);
    }

    @GetMapping("private/allocates")

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.MERCHANT.*')")
    public Set<ManagerStoreDomain> allocatedDomains(StoreMerchantId merchantStore)
            throws MerchantStoreNotFoundException {
        return merchantRoutingService.domains(merchantStore);
    }

    @PostMapping("private/allocate")

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.MERCHANT.*')")
    public void allocate(StoreMerchantId merchantStore, Domain domain) throws MerchantStoreNotFoundException {
        merchantRoutingService.addDomain(merchantStore, domain);
    }

    @DeleteMapping("private/remove")

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.MERCHANT.*')")
    public void remove(StoreMerchantId merchantStore, Domain domain) throws MerchantStoreNotFoundException {
        merchantRoutingService.removeDomain(merchantStore, domain);
    }

}
