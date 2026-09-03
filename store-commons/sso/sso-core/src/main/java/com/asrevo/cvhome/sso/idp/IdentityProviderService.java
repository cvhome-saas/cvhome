package com.asrevo.cvhome.sso.idp;

import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.domain.IdpType;
import com.asrevo.cvhome.sso.dto.IdentityProviderDto;
import com.asrevo.cvhome.sso.dto.IdentityProviderRequest;
import com.asrevo.cvhome.sso.dto.IdpTestResult;
import com.asrevo.cvhome.sso.dto.PublicIdpDto;
import com.asrevo.cvhome.sso.repo.IdentityProviderRepository;
import com.asrevo.cvhome.uaa.errors.IdpAliasTakenException;
import com.asrevo.cvhome.uaa.errors.IdpConfigInvalidException;
import com.asrevo.cvhome.uaa.errors.IdpDiscoveryFailedException;
import com.asrevo.cvhome.uaa.errors.IdpNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * The providers, administered. Every write evicts the alias from the registration cache, so the next sign-in sees
 * the change; the audit diff is the DTO, which never carries the secret.
 */
@Service
@Slf4j
public class IdentityProviderService {

    static final String OIDC_DISCOVERY = "/.well-known/openid-configuration";

    private static final String TRAILING_SLASHES = "/+$";

    private final IdentityProviderRepository providers;

    private final IdentityProviderMapper mapper;

    private final ClientRegistrationFactory factory;

    private final DynamicClientRegistrationRepository registrations;

    private final AuditService audit;

    private final Clock clock;

    private final String issuer;

    private final RestClient http;

    public IdentityProviderService(IdentityProviderRepository providers, IdentityProviderMapper mapper,
                                   ClientRegistrationFactory factory, DynamicClientRegistrationRepository registrations,
                                   AuditService audit, Clock clock,
                                   @Qualifier("uaaIssuer") String issuer,
                                   @Qualifier("defaultRestClientBuilder") RestClient.Builder httpBuilder) {
        this.providers = providers;
        this.mapper = mapper;
        this.factory = factory;
        this.registrations = registrations;
        this.audit = audit;
        this.clock = clock;
        this.issuer = issuer;
        this.http = httpBuilder.build();
    }

    // ---------------------------------------------------------------- read

    @Transactional(readOnly = true)
    public List<IdentityProviderDto> list() {
        return providers.findAllByOrderBySortOrderAscDisplayNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public IdentityProviderDto get(UUID id) throws IdpNotFoundException {
        return toDto(find(id));
    }

    @Transactional(readOnly = true)
    public Optional<IdentityProvider> byAlias(String alias) {
        return providers.findByAlias(alias);
    }

    @Transactional(readOnly = true)
    public Optional<IdentityProvider> byId(UUID id) {
        return providers.findById(id);
    }

    /** The buttons: enabled and not hidden, in order. */
    @Transactional(readOnly = true)
    public List<PublicIdpDto> visibleForLogin() {
        return providers.findByEnabledTrueOrderBySortOrderAscDisplayNameAsc().stream()
                .filter(p -> !p.isHideOnLogin())
                .map(this::toPublic)
                .toList();
    }

    /**
     * Home-realm discovery: the enabled provider — hidden or not — whose email domains match the address, longest
     * suffix first, so {@code eng.example.com} beats {@code example.com}.
     */
    @Transactional(readOnly = true)
    public Optional<PublicIdpDto> discoverByEmail(String email) {
        if (email == null || !email.contains("@")) {
            return Optional.empty();
        }
        String domain = email.substring(email.lastIndexOf('@') + 1).trim().toLowerCase(Locale.ROOT);
        IdentityProvider best = null;
        int bestLength = -1;
        for (IdentityProvider p : providers.findByEnabledTrueOrderBySortOrderAscDisplayNameAsc()) {
            for (String rule : IdentityProviderMapper.split(p.getEmailDomains())) {
                boolean matches = domain.equals(rule) || domain.endsWith(String.format(".%s", rule));
                if (matches && rule.length() > bestLength) {
                    best = p;
                    bestLength = rule.length();
                }
            }
        }
        return Optional.ofNullable(best).map(this::toPublic);
    }

    // ---------------------------------------------------------------- write

    @Transactional
    public IdentityProviderDto create(IdentityProviderRequest req) throws IdpAliasTakenException, IdpConfigInvalidException {
        String alias = req.alias().trim().toLowerCase(Locale.ROOT);
        if (providers.existsByAlias(alias)) {
            throw IdpAliasTakenException.of(alias);
        }
        int order = (int) providers.count();
        IdentityProvider p = mapper.toNewEntity(req, order, clock.instant());
        validate(p);
        IdentityProviderDto created = toDto(providers.save(p));
        audit.record(AuditRecord.of(AuditEventType.IDP_CREATED).target(AuditTargetType.IDP, p.getId().toString(), alias)
                .change(null, created));
        return created;
    }

    @Transactional
    public IdentityProviderDto update(UUID id, IdentityProviderRequest req)
            throws IdpNotFoundException, IdpAliasTakenException, IdpConfigInvalidException {
        IdentityProvider p = find(id);
        IdentityProviderDto before = toDto(p);
        String alias = req.alias().trim().toLowerCase(Locale.ROOT);
        if (!alias.equals(p.getAlias()) && providers.existsByAlias(alias)) {
            throw IdpAliasTakenException.of(alias);
        }
        registrations.evict(p.getAlias());
        p.setAlias(alias);
        mapper.apply(p, req, clock.instant());
        validate(p);
        IdentityProviderDto after = toDto(providers.save(p));
        registrations.evict(alias);
        audit.record(AuditRecord.of(AuditEventType.IDP_UPDATED).target(AuditTargetType.IDP, id.toString(), alias)
                .change(before, after));
        return after;
    }

    @Transactional
    public void delete(UUID id) throws IdpNotFoundException {
        IdentityProvider p = find(id);
        IdentityProviderDto before = toDto(p);
        providers.delete(p);
        registrations.evict(p.getAlias());
        audit.record(AuditRecord.of(AuditEventType.IDP_DELETED).target(AuditTargetType.IDP, id.toString(), p.getAlias())
                .change(before, null));
    }

    @Transactional
    public IdentityProviderDto setEnabled(UUID id, boolean enabled) throws IdpNotFoundException {
        IdentityProvider p = find(id);
        IdentityProviderDto before = toDto(p);
        p.setEnabled(enabled);
        p.setUpdatedAt(clock.instant());
        registrations.evict(p.getAlias());
        IdentityProviderDto after = toDto(providers.save(p));
        audit.record(AuditRecord.of(AuditEventType.IDP_UPDATED).target(AuditTargetType.IDP, id.toString(), p.getAlias())
                .detail(enabled ? "enabled" : "disabled").change(before, after));
        return after;
    }

    /** The sign-in page's order: the aliases as given, everything else after them in its old order. */
    @Transactional
    public List<IdentityProviderDto> reorder(List<String> aliases) {
        List<IdentityProvider> all = providers.findAllByOrderBySortOrderAscDisplayNameAsc();
        int next = 0;
        for (String alias : aliases) {
            for (IdentityProvider p : all) {
                if (p.getAlias().equals(alias)) {
                    p.setSortOrder(next++);
                }
            }
        }
        for (IdentityProvider p : all) {
            if (!aliases.contains(p.getAlias())) {
                p.setSortOrder(next++);
            }
        }
        providers.saveAll(all);
        return list();
    }

    /**
     * Reaches out to the provider: the discovery document for an OIDC issuer, else the authorization endpoint. A
     * failure is typed as the provider's, with its status carried as an extension.
     */
    @Transactional(readOnly = true)
    public IdpTestResult test(UUID id) throws IdpNotFoundException, IdpDiscoveryFailedException, IdpConfigInvalidException {
        IdentityProvider p = find(id);
        factory.build(p);
        String url = testUrl(p);
        try {
            Map<?, ?> body = http.get().uri(URI.create(url)).retrieve().body(Map.class);
            Object issuerClaim = body == null ? null : body.get("issuer");
            String discovered = issuerClaim == null ? null : String.valueOf(issuerClaim);
            return new IdpTestResult(true, url, StringUtils.hasText(discovered) ? discovered : null, "answered");
        } catch (org.springframework.web.client.RestClientResponseException e) {
            throw IdpDiscoveryFailedException.of(p.getAlias(), url, e.getStatusCode().value(), e);
        } catch (org.springframework.web.client.RestClientException e) {
            // The authorization endpoint of an OAuth2 provider answers HTML, or a redirect; reachable is enough there.
            if (p.getType() == IdpType.OAUTH2 && e.getCause() == null) {
                return new IdpTestResult(true, url, null, "reachable");
            }
            throw IdpDiscoveryFailedException.of(p.getAlias(), url, 0, e);
        }
    }

    /** The discovery document for an OIDC issuer, else the authorization endpoint. */
    private static String testUrl(IdentityProvider p) throws IdpConfigInvalidException {
        String url = p.getType() == IdpType.OIDC && StringUtils.hasText(p.getIssuerUri())
                ? String.format("%s%s", p.getIssuerUri().replaceAll(TRAILING_SLASHES, ""), OIDC_DISCOVERY) : p.getAuthorizationUri();
        if (!StringUtils.hasText(url)) {
            throw IdpConfigInvalidException.of("authorizationUri", "Nothing to test: no issuer and no authorization endpoint.");
        }
        return url;
    }

    // ---------------------------------------------------------------- helpers

    private void validate(IdentityProvider p) throws IdpConfigInvalidException {
        factory.build(p);
    }

    private IdentityProvider find(UUID id) throws IdpNotFoundException {
        return providers.findById(id).orElseThrow(() -> IdpNotFoundException.of(id.toString()));
    }

    public IdentityProviderDto toDto(IdentityProvider p) {
        return mapper.toDto(p, redirectUri(p.getAlias()));
    }

    /** What to register at the provider: uaa's pinned issuer plus Spring's callback path for the alias. */
    public String redirectUri(String alias) {
        return String.format("%s/login/oauth2/code/%s", issuer.replaceAll(TRAILING_SLASHES, ""), alias);
    }

    private PublicIdpDto toPublic(IdentityProvider p) {
        return new PublicIdpDto(p.getAlias(), p.getDisplayName(), p.getPreset(), p.getType(),
                String.format("/oauth2/authorization/%s", p.getAlias()));
    }

}
