package com.asrevo.cvhome.content.api.v1;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.PreviewTokens;
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
 * The public, cache-friendly read surface the storefront renders from. No authentication; drafts only with a valid
 * preview token. Every response is short-lived cacheable so Caddy/CDN and the storefront's own fetch cache can hold
 * it.
 */
@RestController
@RequestMapping("/api/v1/storefront")
@RequiredArgsConstructor
public class StorefrontApi {

    private static final CacheControl CACHE = CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic()
            .staleWhileRevalidate(60, TimeUnit.SECONDS);

    private final StorefrontFacade storefront;

    private final MenuService menus;

    private final PreviewTokens previews;

    private final java.time.Clock clock;

    private static <T> ResponseEntity<T> cached(T body) {
        return ResponseEntity.ok().cacheControl(CACHE).body(body);
    }

    @GetMapping("site")
    public ResponseEntity<StorefrontSite> site(StoreMerchantId merchantStore, LanguageCode language) {
        return cached(storefront.site(merchantStore, language));
    }

    @GetMapping("pages/{slug}")
    public ResponseEntity<StorefrontPage> page(StoreMerchantId merchantStore, LanguageCode language,
                                               @PathVariable String slug,
                                               @RequestParam(required = false) String preview)
            throws ContentNotFoundException {
        boolean draft = previews.valid(preview, merchantStore, slug);
        StorefrontPage page = storefront.page(merchantStore, language, slug, draft);
        return draft ? ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(page) : cached(page);
    }

    @GetMapping("posts")
    public ResponseEntity<StorefrontPostList> posts(StoreMerchantId merchantStore, LanguageCode language,
                                                    @RequestParam(required = false) String category,
                                                    @RequestParam(required = false) String tag, Pageable pageable) {
        return cached(storefront.posts(merchantStore, language, category, tag, pageable));
    }

    @GetMapping("posts/{slug}")
    public ResponseEntity<StorefrontPost> post(StoreMerchantId merchantStore, LanguageCode language,
                                               @PathVariable String slug,
                                               @RequestParam(required = false) String preview)
            throws ContentNotFoundException {
        boolean draft = previews.valid(preview, merchantStore, slug);
        StorefrontPost post = storefront.post(merchantStore, language, slug, draft);
        return draft ? ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(post) : cached(post);
    }

    @GetMapping("post-categories")
    public ResponseEntity<List<StorefrontLink>> postCategories(StoreMerchantId merchantStore,
                                                               LanguageCode language) {
        return cached(storefront.postCategories(merchantStore, language));
    }

    @GetMapping("banners")
    public ResponseEntity<List<StorefrontBanner>> banners(StoreMerchantId merchantStore, LanguageCode language,
                                                          @RequestParam(required = false) BannerPlacement placement) {
        return cached(storefront.effectiveBanners(merchantStore, language, placement));
    }

    /**
     * The page's layout document, render-ready. Serves the published copy (or the starter default before any
     * publish); with a valid preview token it serves the draft instead, uncached, which is what the builder's
     * canvas iframe renders.
     */
    @GetMapping("layout/{page}")
    public ResponseEntity<StorefrontLayout> layout(
            StoreMerchantId merchantStore, LanguageCode language, @PathVariable PageKind page,
            @RequestParam(required = false) String preview) {
        boolean draft = previews.valid(preview, merchantStore, LayoutApi.previewSlug(page));
        var layout = storefront.layout(merchantStore, language, page, draft);
        return draft ? ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(layout) : cached(layout);
    }

    @GetMapping("faq")
    public ResponseEntity<StorefrontFaq> faq(StoreMerchantId merchantStore, LanguageCode language,
                                             @RequestParam(required = false) String group) {
        return cached(storefront.faq(merchantStore, language, group));
    }

    @GetMapping("menus/{handle}")
    public ResponseEntity<List<StorefrontMenuNode>> menu(StoreMerchantId merchantStore, LanguageCode language,
                                                         @PathVariable MenuHandle handle) {
        return cached(menus.resolved(merchantStore, handle, language, clock.instant()));
    }

    @GetMapping("policies/{type}")
    public ResponseEntity<StorefrontPolicy> policy(StoreMerchantId merchantStore, LanguageCode language,
                                                   @PathVariable PolicyType type,
                                                   @RequestParam(name = "v", required = false) Integer version)
            throws ContentNotFoundException {
        return cached(storefront.policy(merchantStore, language, type, version));
    }

    @GetMapping("sitemap")
    public ResponseEntity<List<SitemapEntry>> sitemap(StoreMerchantId merchantStore, LanguageCode language) {
        return cached(storefront.sitemap(merchantStore, language));
    }

    /**
     * {@code {to}} for a moved path, 404 otherwise.
     */
    @GetMapping("redirects")
    public ResponseEntity<Map<String, String>> redirect(StoreMerchantId merchantStore, LanguageCode language,
                                                        @RequestParam String path) {
        return storefront.redirect(merchantStore, path)
                .map(to -> cached(Map.of("from", path, "to", to)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
