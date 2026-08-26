package com.asrevo.cvhome.content.api;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.site.SiteBranding;

/**
 * The store's brand imagery, for a pod service that has to render the store's mark.
 *
 * <p>
 * The customer auth pages are the reason this exists. They are served by {@code cua}, not by the storefront, but
 * a shopper who is sent there to sign in has to recognise the shop they were sent from — so the login and
 * register pages carry the store's logo. That logo used to be a field on {@code ReadableMerchantStore} and was
 * read straight off the merchant record; it is the content service's now, along with the rest of appearance.
 * </p>
 *
 * <p>
 * {@code StoreMerchantId} and {@code LanguageCode} carry no annotation on purpose: argument resolvers serialise
 * them, so tenant and locale travel on every call automatically.
 * </p>
 */
@HttpExchange("/api/v1")
public interface ExternalBrandingService {

    /**
     * Every brand slot resolved to a URL. Slots the store has not filled come back {@code null} rather than
     * absent, so a caller renders its own fallback — a wordmark, usually — without a second call.
     */
    @GetExchange("/private/content/external/branding")
    SiteBranding branding(StoreMerchantId merchantStore, LanguageCode language);

}
