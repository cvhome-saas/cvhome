package com.asrevo.cvhome.content.reads;

import java.time.Clock;
import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.spring.StoreScopedKeyGenerator;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.facade.StorefrontFacade;
import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.MenuHandle;
import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.layout.PageKind;
import com.asrevo.cvhome.content.model.storefront.SitemapEntry;
import com.asrevo.cvhome.content.model.storefront.StorefrontBanner;
import com.asrevo.cvhome.content.model.storefront.StorefrontFaq;
import com.asrevo.cvhome.content.model.storefront.StorefrontLayout;
import com.asrevo.cvhome.content.model.storefront.StorefrontLink;
import com.asrevo.cvhome.content.model.storefront.StorefrontMenuNode;
import com.asrevo.cvhome.content.model.storefront.StorefrontPage;
import com.asrevo.cvhome.content.model.storefront.StorefrontPolicy;
import com.asrevo.cvhome.content.model.storefront.StorefrontPost;
import com.asrevo.cvhome.content.model.storefront.StorefrontPostList;
import com.asrevo.cvhome.content.model.storefront.StorefrontSite;
import com.asrevo.cvhome.content.service.MenuService;

import lombok.RequiredArgsConstructor;

/**
 * The storefront's published reads, each held per store, language and arguments ({@link ContentRegions}); an
 * editor's committed write drops their store's entries at once on the task that took it, and another task lags by
 * the region's time-to-live. Every read here is the published shape: a preview, a draft or a policy at a named
 * version reads the facade live.
 */
@Component
@CacheConfig(keyGenerator = StoreScopedKeyGenerator.BEAN)
@RequiredArgsConstructor
public class StorefrontReads {

    private final StorefrontFacade storefront;

    private final MenuService menus;

    private final Clock clock;

    @Cacheable(ContentRegions.Names.SITE)
    public StorefrontSite site(StoreMerchantId store, LanguageCode language) {
        return storefront.site(store, language);
    }

    @Cacheable(ContentRegions.Names.LAYOUT)
    public StorefrontLayout layout(StoreMerchantId store, LanguageCode language, PageKind page) {
        return storefront.layout(store, language, page, false);
    }

    @Cacheable(ContentRegions.Names.MENU)
    public List<StorefrontMenuNode> menu(StoreMerchantId store, LanguageCode language, MenuHandle handle) {
        return menus.resolved(store, handle, language, clock.instant());
    }

    @Cacheable(ContentRegions.Names.PAGE)
    public StorefrontPage page(StoreMerchantId store, LanguageCode language, String slug)
            throws ContentNotFoundException {
        return storefront.page(store, language, slug, false);
    }

    @Cacheable(ContentRegions.Names.POST)
    public StorefrontPost post(StoreMerchantId store, LanguageCode language, String slug)
            throws ContentNotFoundException {
        return storefront.post(store, language, slug, false);
    }

    @Cacheable(ContentRegions.Names.POSTS)
    public StorefrontPostList posts(StoreMerchantId store, LanguageCode language, PostsFilter filter,
                                    Pageable pageable) {
        return storefront.posts(store, language, filter.category(), filter.tag(), pageable);
    }

    @Cacheable(ContentRegions.Names.POST_CATEGORIES)
    public List<StorefrontLink> postCategories(StoreMerchantId store, LanguageCode language) {
        return storefront.postCategories(store, language);
    }

    /** The placement is optional; absent, every banner. */
    @Cacheable(ContentRegions.Names.BANNERS)
    public List<StorefrontBanner> banners(StoreMerchantId store, LanguageCode language, BannerPlacement placement) {
        return storefront.effectiveBanners(store, language, placement);
    }

    /** The group is optional; absent, every group. */
    @Cacheable(ContentRegions.Names.FAQ)
    public StorefrontFaq faq(StoreMerchantId store, LanguageCode language, String group) {
        return storefront.faq(store, language, group);
    }

    /** The current version of a policy; a named version reads the facade live. */
    @Cacheable(ContentRegions.Names.POLICY)
    public StorefrontPolicy policy(StoreMerchantId store, LanguageCode language, PolicyType type)
            throws ContentNotFoundException {
        return storefront.policy(store, language, type, null);
    }

    @Cacheable(ContentRegions.Names.SITEMAP)
    public List<SitemapEntry> sitemap(StoreMerchantId store, LanguageCode language) {
        return storefront.sitemap(store, language);
    }
}
