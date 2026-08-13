package com.asrevo.cvhome.content.api.v2;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentVersionConflictException;
import com.asrevo.cvhome.content.errors.IllegalContentTransitionException;
import com.asrevo.cvhome.content.errors.PolicyNotFoundException;
import com.asrevo.cvhome.content.errors.PublishedPolicyImmutableException;
import com.asrevo.cvhome.content.model.policy.PolicyType;
import com.asrevo.cvhome.content.model.policy.PolicyView;
import com.asrevo.cvhome.content.model.policy.PolicyWriteRequest;
import com.asrevo.cvhome.content.service.PolicyService;

@RestController
@RequestMapping("/api/v2/private/content/policies")
public class PolicyApi {
    private final PolicyService service;

    public PolicyApi(PolicyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public PolicyView create(@Valid @RequestBody PolicyWriteRequest request, StoreMerchantId merchantStore,
                             LanguageCode language, Principal principal) throws ContentNotFoundException,
            PublishedPolicyImmutableException {
        return service.create(merchantStore, language, request, principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public List<PolicyView> list(@RequestParam PolicyType type, StoreMerchantId merchantStore,
                                 LanguageCode language) throws ContentNotFoundException {
        return service.list(merchantStore, type);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public PolicyView publish(@PathVariable Long id, @RequestHeader("If-Match") long version,
                              StoreMerchantId merchantStore, LanguageCode language, Principal principal)
            throws PolicyNotFoundException, ContentNotFoundException, ContentVersionConflictException,
            IllegalContentTransitionException, PublishedPolicyImmutableException {
        return service.publish(merchantStore, id, version, principal.getName());
    }
}
