package com.asrevo.cvhome.content.facade;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.StoreScopedKeyGenerator;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.MenuHandle;
import com.asrevo.cvhome.content.model.layout.PageKind;
import com.asrevo.cvhome.content.model.storefront.StorefrontLayout;
import com.asrevo.cvhome.content.model.storefront.StorefrontMenuNode;
import com.asrevo.cvhome.content.model.storefront.StorefrontSite;
import com.asrevo.cvhome.content.service.MenuService;

import lombok.RequiredArgsConstructor;

/**
 * The storefront's three hottest reads — the site, a page's published layout and a menu — held for {@link #TTL}.
 *
 * <p>
 * Every storefront render asks for them, and every shopper of a store gets the same answer: in the 2026-09-14 load test
 * the site read was 12 statements (a policy version per policy, a content row per menu target) in a read-write
 * transaction, the layout and menus one read-write transaction each, on every page view. The responses already declare
 * themselves cacheable for 60 s; ten seconds here stays well inside that.
 * </p>
 *
 * <p>
 * An editor's change drops their store's entries from all three caches when it commits (content-service's
 * {@code CacheConfig}), so the task that took the change serves it at once; another task serves it within
 * {@link #TTL}. A preview never comes
 * through here: drafts are read live.
 * </p>
 */
@Component
@CacheConfig(keyGenerator = StoreScopedKeyGenerator.BEAN)
@RequiredArgsConstructor
public class CachedStorefront {

    public static final String SITE = "STOREFRONT_SITE";

    public static final String LAYOUT = "STOREFRONT_LAYOUT";

    public static final String MENU = "STOREFRONT_MENU";

    public static final List<String> CACHES = List.of(SITE, LAYOUT, MENU);

    public static final Duration TTL = Duration.ofSeconds(10);

    private final StorefrontFacade storefront;

    private final MenuService menus;

    private final Clock clock;

    @Cacheable(SITE)
    public StorefrontSite site(StoreMerchantId store, LanguageCode language) {
        return storefront.site(store, language);
    }

    /** The published layout; a draft is read with {@link StorefrontFacade#layout} directly. */
    @Cacheable(LAYOUT)
    public StorefrontLayout layout(StoreMerchantId store, LanguageCode language, PageKind page) {
        return storefront.layout(store, language, page, false);
    }

    @Cacheable(MENU)
    public List<StorefrontMenuNode> menu(StoreMerchantId store, MenuHandle handle, LanguageCode language) {
        return menus.resolved(store, handle, language, clock.instant());
    }
}
