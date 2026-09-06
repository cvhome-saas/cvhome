package com.asrevo.cvhome.sso.web.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.sso.client.ClientType;
import com.asrevo.cvhome.sso.dto.ClientDetails;
import com.asrevo.cvhome.sso.dto.ClientSearch;
import com.asrevo.cvhome.sso.dto.ClientStats;
import com.asrevo.cvhome.sso.dto.ClientSummary;
import com.asrevo.cvhome.sso.dto.CreatedClient;
import com.asrevo.cvhome.sso.dto.RotatedSecret;
import com.asrevo.cvhome.sso.security.PrincipalNames;
import com.asrevo.cvhome.sso.service.AdminClientService;
import com.asrevo.cvhome.sso.service.ClientAuthMethod;
import com.asrevo.cvhome.sso.service.OAuthGrantType;
import com.asrevo.cvhome.uaa.errors.ClientIdTakenException;
import com.asrevo.cvhome.uaa.errors.ClientNoPreviousSecretException;
import com.asrevo.cvhome.uaa.errors.ClientNotConfidentialException;
import com.asrevo.cvhome.uaa.errors.ClientNotFoundException;
import com.asrevo.cvhome.uaa.errors.ClientTokenTtlExceedsPolicyException;
import com.asrevo.cvhome.uaa.errors.InvalidRedirectUriException;

import lombok.AllArgsConstructor;

import static org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat.REFERENCE;
import static org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat.SELF_CONTAINED;

/**
 * The client registry. A secret appears in exactly two responses — {@link #create} and {@link #rotateSecret} — and
 * in neither can it be read again.
 */
@RestController
@RequestMapping("/api/v1/admin/clients")
@AllArgsConstructor
public class AdminClientController {

    private static final String ADMIN = "hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')";

    private final AdminClientService adminClientService;

    private final PrincipalNames principals;

    @PreAuthorize(ADMIN)
    @GetMapping
    public Page<ClientSummary> list(@RequestParam(required = false) String q, @RequestParam(required = false) Boolean enabled,
                                    @RequestParam(required = false) ClientType type, @PageableDefault Pageable pageable) {
        return adminClientService.listClients(new ClientSearch(q, enabled, type), pageable);
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/stats")
    public ClientStats stats() {
        return adminClientService.stats();
    }

    @PreAuthorize(ADMIN)
    @GetMapping("{id}")
    public ClientDetails findOne(@PathVariable String id) throws ClientNotFoundException {
        return adminClientService.findById(id);
    }

    /** 201 with the generated secret, once. */
    @PreAuthorize(ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedClient create(@RequestBody ClientDetails req)
            throws ClientIdTakenException, InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        return adminClientService.create(req);
    }

    @PreAuthorize(ADMIN)
    @PutMapping("{id}")
    public ClientDetails update(@PathVariable String id, @RequestBody ClientDetails req)
            throws ClientNotFoundException, InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        return adminClientService.update(id, req);
    }

    @PreAuthorize(ADMIN)
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        adminClientService.delete(id);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("{id}/enable")
    public ClientDetails enable(@PathVariable String id) throws ClientNotFoundException {
        return adminClientService.enable(id);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("{id}/disable")
    public ClientDetails disable(@PathVariable String id, Authentication authentication) throws ClientNotFoundException {
        return adminClientService.disable(id,
                authentication == null ? null : principals.display(authentication.getName()));
    }

    /** A new random secret with a grace window for the old one, answered once. */
    @PreAuthorize(ADMIN)
    @PostMapping("{id}/rotate-secret")
    public RotatedSecret rotateSecret(@PathVariable String id)
            throws ClientNotFoundException, ClientNotConfidentialException {
        return adminClientService.rotateSecret(id);
    }

    @PreAuthorize(ADMIN)
    @DeleteMapping("{id}/previous-secret")
    public void revokePreviousSecret(@PathVariable String id)
            throws ClientNotFoundException, ClientNoPreviousSecretException {
        adminClientService.revokePreviousSecret(id);
    }

    /** Sets a secret the caller chose, with no grace window: the alias the SDK calls. */
    @PreAuthorize(ADMIN)
    @PostMapping("{id}/reset-secret")
    public void resetSecret(@PathVariable String id, @RequestBody ResetSecretRequest req)
            throws ClientNotFoundException, ClientNotConfidentialException {
        adminClientService.resetSecret(id, req.newSecret());
    }

    /** Danger zone: every secret-holding client gets a new secret. The list is the only time they are shown. */
    @PreAuthorize(ADMIN)
    @PostMapping("/rotate-all")
    public List<RotatedSecret> rotateAll() throws ClientNotFoundException, ClientNotConfidentialException {
        return adminClientService.rotateAll();
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/options")
    public Map<String, Object> getOptions() {
        Map<String, Object> options = new HashMap<>(adminClientService.scopeCatalogue());
        options.put("clientAuthenticationMethods", Stream.of(ClientAuthMethod.values()).map(ClientAuthMethod::value).toList());
        // Token exchange is the impersonation grant, held by one seeded client; it is not something to hand out from a
        // form. It still reads back on that client's details, it just is not offered.
        options.put("authorizationGrantTypes", Stream.of(OAuthGrantType.values())
                .filter(grant -> grant != OAuthGrantType.TOKEN_EXCHANGE).map(OAuthGrantType::value).toList());
        options.put("idTokenSignatureAlgorithm", Stream.of(SignatureAlgorithm.values()).map(SignatureAlgorithm::getName).toList());
        options.put("tokenEndpointAuthenticationSigningAlgorithm",
                Stream.of(SignatureAlgorithm.values()).map(SignatureAlgorithm::getName).toList());
        options.put("accessTokenFormat", List.of(SELF_CONTAINED.getValue(), REFERENCE.getValue()));
        options.put("clientTypes", Stream.of(ClientType.values()).map(Enum::name).toList());
        return options;
    }

    public record ResetSecretRequest(String newSecret) {
    }

}
