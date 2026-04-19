package com.asrevo.cvhome.uaa.web.admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.dto.ClientDetails;
import com.asrevo.cvhome.uaa.dto.ClientSummary;
import com.asrevo.cvhome.uaa.service.AdminClientService;
import com.asrevo.cvhome.uaa.service.ClientAuthMethod;
import com.asrevo.cvhome.uaa.service.OAuthGrantType;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat.REFERENCE;
import static org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat.SELF_CONTAINED;

@RestController
@RequestMapping("/api/v1/admin/clients")
@AllArgsConstructor
@Slf4j
public class AdminClientController {

    private final AdminClientService adminClientService;

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public ClientDetails create(@RequestBody ClientDetails req) {
        return adminClientService.save(req);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PutMapping("{id}")
    public ClientDetails update(@PathVariable String id, @RequestBody ClientDetails req) {
        return adminClientService.save(req);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PostMapping("{id}/reset-secret")
    public void resetSecret(@PathVariable String id, @RequestBody ResetSecretRequest req) {
        adminClientService.resetSecret(id, req.newSecret());
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping
    public Page<ClientSummary> list(@PageableDefault Pageable pageable) {
        return adminClientService.listClients(pageable);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping("{id}")
    public ClientDetails findOne(@PathVariable String id) {
        return adminClientService.findById(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        adminClientService.delete(id);
    }

    @GetMapping("/options")
    public Map<String, Object> getOptions() {
        return Map.of("clientAuthenticationMethods",
                Stream.of(ClientAuthMethod.values()).map(ClientAuthMethod::value).toList(), "authorizationGrantTypes",
                Stream.of(OAuthGrantType.values()).map(OAuthGrantType::value).toList(), "scopes",
                List.of("openid", "profile", "email", "api.read", "store_core", "store_pod", "super_admin"),
                "idTokenSignatureAlgorithm",
                Stream.of(SignatureAlgorithm.values()).map(SignatureAlgorithm::getName).toList(),
                "tokenEndpointAuthenticationSigningAlgorithm",
                Stream.of(SignatureAlgorithm.values()).map(SignatureAlgorithm::getName).toList(), "accessTokenFormat",
                List.of(SELF_CONTAINED.getValue(), REFERENCE.getValue()));
    }

    public record ResetSecretRequest(String newSecret) {
    }

}
