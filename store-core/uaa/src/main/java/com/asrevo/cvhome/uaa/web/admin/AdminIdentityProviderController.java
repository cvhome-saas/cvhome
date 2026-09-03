package com.asrevo.cvhome.uaa.web.admin;

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

import com.asrevo.cvhome.uaa.dto.IdentityProviderDto;
import com.asrevo.cvhome.uaa.dto.IdentityProviderRequest;
import com.asrevo.cvhome.uaa.dto.IdpPresetDto;
import com.asrevo.cvhome.uaa.dto.IdpTestResult;
import com.asrevo.cvhome.uaa.errors.IdpAliasTakenException;
import com.asrevo.cvhome.uaa.errors.IdpConfigInvalidException;
import com.asrevo.cvhome.uaa.errors.IdpDiscoveryFailedException;
import com.asrevo.cvhome.uaa.errors.IdpNotFoundException;
import com.asrevo.cvhome.uaa.idp.IdentityProviderService;
import com.asrevo.cvhome.uaa.idp.IdpPreset;

import lombok.RequiredArgsConstructor;

/** The identity providers: everything the console's screen does. Never a client secret in a response. */
@RestController
@RequestMapping("/api/v1/admin/identity-providers")
@RequiredArgsConstructor
public class AdminIdentityProviderController {

    private static final String ADMIN = "hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')";

    private final IdentityProviderService providers;

    @PreAuthorize(ADMIN)
    @GetMapping
    public List<IdentityProviderDto> list() {
        return providers.list();
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/presets")
    public List<IdpPresetDto> presets() {
        return IdpPreset.catalogue().stream().map(IdpPresetDto::of).toList();
    }

    @PreAuthorize(ADMIN)
    @GetMapping("{id}")
    public IdentityProviderDto get(@PathVariable UUID id) throws IdpNotFoundException {
        return providers.get(id);
    }

    @PreAuthorize(ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdentityProviderDto create(@Valid @RequestBody IdentityProviderRequest req)
            throws IdpAliasTakenException, IdpConfigInvalidException {
        return providers.create(req);
    }

    @PreAuthorize(ADMIN)
    @PutMapping("{id}")
    public IdentityProviderDto update(@PathVariable UUID id, @Valid @RequestBody IdentityProviderRequest req)
            throws IdpNotFoundException, IdpAliasTakenException, IdpConfigInvalidException {
        return providers.update(id, req);
    }

    @PreAuthorize(ADMIN)
    @DeleteMapping("{id}")
    public void delete(@PathVariable UUID id) throws IdpNotFoundException {
        providers.delete(id);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("{id}/enable")
    public IdentityProviderDto enable(@PathVariable UUID id) throws IdpNotFoundException {
        return providers.setEnabled(id, true);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("{id}/disable")
    public IdentityProviderDto disable(@PathVariable UUID id) throws IdpNotFoundException {
        return providers.setEnabled(id, false);
    }

    /** Reaches the provider: discovery for OIDC, the authorization endpoint otherwise. 502 carries the provider. */
    @PreAuthorize(ADMIN)
    @PostMapping("{id}/test")
    public IdpTestResult test(@PathVariable UUID id)
            throws IdpNotFoundException, IdpDiscoveryFailedException, IdpConfigInvalidException {
        return providers.test(id);
    }

    /** The sign-in page's order: a bare JSON array of aliases. */
    @PreAuthorize(ADMIN)
    @PutMapping("/order")
    public List<IdentityProviderDto> reorder(@RequestBody List<String> aliases) {
        return providers.reorder(aliases);
    }

}
