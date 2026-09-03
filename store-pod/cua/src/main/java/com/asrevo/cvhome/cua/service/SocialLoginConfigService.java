package com.asrevo.cvhome.cua.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.cua.web.dto.PersistableSocialLoginConfig;
import com.asrevo.cvhome.cua.web.dto.ReadableSocialLogin;
import com.asrevo.cvhome.cua.web.dto.ReadableSocialLoginConfig;
import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.dto.IdentityProviderDto;
import com.asrevo.cvhome.sso.dto.IdentityProviderRequest;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.idp.IdpPreset;
import com.asrevo.cvhome.uaa.errors.IdpAliasTakenException;
import com.asrevo.cvhome.uaa.errors.IdpConfigInvalidException;
import com.asrevo.cvhome.uaa.errors.IdpNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The merchant console's social-login screen, over the shared identity-provider store.
 *
 * <p>
 * <strong>Transitional, and deliberately narrow.</strong> cua used to keep its own {@code social_login_configs}
 * table with three providers and two fields each. That data now lives in {@code identity_providers} alongside
 * everything the shared server can broker — presets, account-linking policy, just-in-time provisioning, attribute
 * mapping — and this class is the small translation that lets the existing console screen go on working while the
 * richer screen is built. When the console addresses the identity-provider API directly, this goes.
 * </p>
 *
 * <p>
 * Nothing here names a store. The realm is resolved from the request, and every query and write below is scoped to
 * it by Hibernate's tenant filter — which is what makes one merchant's Google application invisible to another.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SocialLoginConfigService {

    /** What the console offers, and what cua brokered before the two servers became one. */
    private static final List<IdpPreset> SOCIAL = List.of(IdpPreset.GOOGLE, IdpPreset.FACEBOOK, IdpPreset.GITHUB);

    private final IdentityProviderService providers;

    /**
     * The buttons a storefront's login page renders, for the store the request arrived for.
     *
     * <p>
     * The registration id is the bare alias now, not {@code {store}.{provider}}. It no longer has to carry the
     * store because the alias is unique within its realm and the realm comes from the host — and the callback URL
     * a merchant registers with Google is still per-store, because it is built on their own domain.
     * </p>
     */
    public List<ReadableSocialLogin> enabledLogins() {
        return providers.visibleForLogin().stream()
                .map(p -> new ReadableSocialLogin(p.alias(), p.displayName(), p.alias()))
                .toList();
    }

    /**
     * One row per provider the console offers, whether or not this store has configured it.
     *
     * <p>
     * The app id comes back; the app secret does not. An OAuth2 client id travels in the authorization URL and is
     * public by construction, and a merchant has to be able to see which application their store is wired to. The
     * secret is the credential, and an API that returns it undoes the encryption it is stored under.
     * </p>
     */
    public List<ReadableSocialLoginConfig> getConfigs() {
        return SOCIAL.stream().map(this::readable).toList();
    }

    @Transactional
    public void saveConfigs(List<PersistableSocialLoginConfig> configs)
            throws IdpAliasTakenException, IdpConfigInvalidException, IdpNotFoundException {
        for (PersistableSocialLoginConfig config : configs) {
            IdpPreset preset = IdpPreset.valueOf(config.providerId().toUpperCase());
            Optional<IdentityProvider> existing = stored(preset);
            IdentityProviderRequest request = request(preset, config);
            if (existing.isPresent()) {
                providers.update(existing.get().getId(), request);
            } else {
                providers.create(request);
            }
        }
    }

    /** The providers the console screen knows how to draw. */
    public List<String> supportedProviders() {
        return SOCIAL.stream().map(Enum::name).toList();
    }

    /**
     * One provider, and never an exception for the others' sake.
     *
     * <p>
     * Reading the app id decrypts it, and a row this deployment cannot decrypt — written under a key that has
     * since changed, or seeded by hand — would otherwise take the whole screen down with a 500, leaving the
     * merchant unable to see or repair any of their providers. It reports as unconfigured instead, which is what
     * an unreadable credential effectively is.
     * </p>
     */
    private ReadableSocialLoginConfig readable(IdpPreset preset) {
        Optional<IdentityProvider> provider = stored(preset);
        if (provider.isEmpty()) {
            return ReadableSocialLoginConfig.of(preset.name(), preset.displayName(), null, false, false);
        }
        IdentityProvider p = provider.get();
        try {
            IdentityProviderDto dto = providers.toDto(p);
            return ReadableSocialLoginConfig.of(preset.name(), preset.displayName(), dto.clientId(),
                    dto.hasClientSecret(), p.isEnabled());
        } catch (RuntimeException unreadable) {
            log.warn("Identity provider {} cannot be read back and is reported as unconfigured: {}", preset,
                    unreadable.getMessage());
            return ReadableSocialLoginConfig.of(preset.name(), preset.displayName(), null, false, false);
        }
    }

    private Optional<IdentityProvider> stored(IdpPreset preset) {
        return providers.byAlias(alias(preset));
    }

    private IdentityProviderRequest request(IdpPreset preset, PersistableSocialLoginConfig config) {
        return new IdentityProviderRequest(alias(preset), preset.displayName(), preset, false, config.appId(),
                config.appSecret(), null, null, null, null, null, null, null, null, null,
                // A shopper who signs in with Google and already has a password account for the same address is
                // the same person to a store; CONFIRM asks for that password once rather than assuming it.
                AccountLinking.CONFIRM, true, null, true, null);
    }

    /** The alias is the Spring registrationId, and it is unique per realm — every store may have a "google". */
    private String alias(IdpPreset preset) {
        return preset.name().toLowerCase();
    }

}
