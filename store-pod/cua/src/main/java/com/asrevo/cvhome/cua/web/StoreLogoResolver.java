package com.asrevo.cvhome.cua.web;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.ExternalBrandingService;
import com.asrevo.cvhome.content.model.site.SiteBranding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The store's logo for the auth pages, from the content service.
 *
 * <p>
 * It used to be {@code ReadableMerchantStore.logo}, read off the record these pages already had. Appearance
 * moved to the content service and the field went with it, which took the auth pages down: the templates went on
 * reading {@code store.logo}, Thymeleaf threw {@code EL1008E} at render time, Spring dispatched the failure to
 * {@code /error}, and {@code /error} sent the shopper back to {@code /login} — which threw again. Clicking
 * "Login" on the storefront reloaded forever.
 * </p>
 *
 * <p>
 * <strong>Never throws.</strong> A logo is decoration on a page whose job is to take a password, so a content
 * outage must not stop a shopper signing in. Anything that goes wrong is logged and answered as "no logo", and
 * the templates fall back to the store's name — the same thing they do for a store that never uploaded one.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreLogoResolver {

    private final ExternalBrandingService branding;

    /** The store's logo URL, or {@code null} — for a store without one, or a content service that did not answer. */
    public String logoUrl(StoreMerchantId store, LanguageCode language) {
        try {
            SiteBranding resolved = branding.branding(store, language);
            return resolved == null || resolved.logo() == null ? null : resolved.logo().url();
        } catch (Exception failure) {
            log.warn("Could not read branding for store {}; the auth page falls back to its name", store, failure);
            return null;
        }
    }

}
