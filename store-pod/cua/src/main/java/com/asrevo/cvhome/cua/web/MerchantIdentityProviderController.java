package com.asrevo.cvhome.cua.web;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.sso.dto.IdentityProviderDto;
import com.asrevo.cvhome.sso.dto.IdentityProviderRequest;
import com.asrevo.cvhome.sso.dto.IdpPresetDto;
import com.asrevo.cvhome.sso.dto.IdpTestResult;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.idp.IdpPreset;
import com.asrevo.cvhome.uaa.errors.IdpAliasTakenException;
import com.asrevo.cvhome.uaa.errors.IdpConfigInvalidException;
import com.asrevo.cvhome.uaa.errors.IdpDiscoveryFailedException;
import com.asrevo.cvhome.uaa.errors.IdpEndpointRefusedException;
import com.asrevo.cvhome.uaa.errors.IdpNotFoundException;
import com.asrevo.cvhome.uaa.errors.IdpTestThrottledException;

import lombok.RequiredArgsConstructor;

/**
 * A store's identity providers, as its own merchant administers them.
 *
 * <p>
 * The same service the platform console drives, reached from the store's side. Nothing here filters by store,
 * because nothing here can see another store's rows: every provider is a {@code @TenantId} row and the realm is
 * the one the edge resolved from the host. The {@code merchantStore} parameter is what the permission check
 * needs — it answers "may this operator administer this store", which is a different question from "whose rows
 * are these".
 * </p>
 *
 * <p>
 * This supersedes {@link SocialLoginConfigController}, which offers three presets with hard-coded endpoints. A
 * merchant reaching this one can point the server at a URL of their own, which is why it cannot ship without
 * {@code EgressGuard} — see {@code IdentityProviderService.validate}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/private/identity-providers")
@RequiredArgsConstructor
public class MerchantIdentityProviderController {

    private static final String MERCHANT = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUA.*')";

    private final IdentityProviderService providers;

    @PreAuthorize(MERCHANT)
    @GetMapping
    public List<IdentityProviderDto> list(StoreMerchantId merchantStore) {
        return providers.list();
    }

    /** The provider kinds a store may configure, with the fields each one needs. */
    @PreAuthorize(MERCHANT)
    @GetMapping("/presets")
    public List<IdpPresetDto> presets(StoreMerchantId merchantStore) {
        return IdpPreset.catalogue().stream().map(IdpPresetDto::of).toList();
    }

    @PreAuthorize(MERCHANT)
    @GetMapping("/{id}")
    public IdentityProviderDto get(StoreMerchantId merchantStore, @PathVariable UUID id) throws IdpNotFoundException {
        return providers.get(id);
    }

    @PreAuthorize(MERCHANT)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdentityProviderDto create(StoreMerchantId merchantStore, @Valid @RequestBody IdentityProviderRequest req)
            throws IdpAliasTakenException, IdpConfigInvalidException, IdpEndpointRefusedException {
        return providers.create(req);
    }

    @PreAuthorize(MERCHANT)
    @PutMapping("/{id}")
    public IdentityProviderDto update(StoreMerchantId merchantStore, @PathVariable UUID id,
                                      @Valid @RequestBody IdentityProviderRequest req)
            throws IdpNotFoundException, IdpAliasTakenException, IdpConfigInvalidException,
            IdpEndpointRefusedException {
        return providers.update(id, req);
    }

    @PreAuthorize(MERCHANT)
    @DeleteMapping("/{id}")
    public void delete(StoreMerchantId merchantStore, @PathVariable UUID id) throws IdpNotFoundException {
        providers.delete(id);
    }

    @PreAuthorize(MERCHANT)
    @PostMapping("/{id}/enable")
    public IdentityProviderDto enable(StoreMerchantId merchantStore, @PathVariable UUID id)
            throws IdpNotFoundException {
        return providers.setEnabled(id, true);
    }

    @PreAuthorize(MERCHANT)
    @PostMapping("/{id}/disable")
    public IdentityProviderDto disable(StoreMerchantId merchantStore, @PathVariable UUID id)
            throws IdpNotFoundException {
        return providers.setEnabled(id, false);
    }

    /**
     * Reaches the provider: discovery for OIDC, the authorization endpoint otherwise.
     *
     * <p>
     * This is the one call that makes the server fetch a URL on demand, so it is checked against the egress
     * policy again here and rationed per store. Both refusals are the store's own doing and say so.
     * </p>
     */
    @PreAuthorize(MERCHANT)
    @PostMapping("/{id}/test")
    public IdpTestResult test(StoreMerchantId merchantStore, @PathVariable UUID id)
            throws IdpNotFoundException, IdpDiscoveryFailedException, IdpConfigInvalidException,
            IdpEndpointRefusedException, IdpTestThrottledException {
        return providers.test(id);
    }

    /** The order the storefront's sign-in page lists them in. */
    @PreAuthorize(MERCHANT)
    @PutMapping("/order")
    public List<IdentityProviderDto> reorder(StoreMerchantId merchantStore, @RequestBody List<String> aliases) {
        return providers.reorder(aliases);
    }

}
