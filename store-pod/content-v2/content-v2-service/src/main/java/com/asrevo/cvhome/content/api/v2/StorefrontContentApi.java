package com.asrevo.cvhome.content.api.v2;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.PolicyNotFoundException;
import com.asrevo.cvhome.content.model.ContentSummary;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.banner.BannerPlacement;
import com.asrevo.cvhome.content.model.banner.BannerView;
import com.asrevo.cvhome.content.model.menu.MenuView;
import com.asrevo.cvhome.content.model.page.PageView;
import com.asrevo.cvhome.content.model.policy.PolicyType;
import com.asrevo.cvhome.content.model.policy.PolicyView;
import com.asrevo.cvhome.content.model.post.PostView;
import com.asrevo.cvhome.content.service.StorefrontContentService;

@RestController
@RequestMapping("/api/v2/storefront")
public class StorefrontContentApi {
    private static final String CACHE_CONTROL = "public, s-maxage=300, stale-while-revalidate=60";
    private final StorefrontContentService service;

    public StorefrontContentApi(StorefrontContentService service) {
        this.service = service;
    }

    @GetMapping("/pages/{slug}")
    public ResponseEntity<PageView> page(@PathVariable String slug, StoreMerchantId merchantStore,
                                         LanguageCode language, @RequestHeader(value = "If-None-Match",
            required = false) String ifNoneMatch) throws ContentNotFoundException {
        return cached(service.page(merchantStore, language, slug), ifNoneMatch);
    }

    @GetMapping("/posts/{slug}")
    public ResponseEntity<PostView> post(@PathVariable String slug, StoreMerchantId merchantStore,
                                         LanguageCode language, @RequestHeader(value = "If-None-Match",
            required = false) String ifNoneMatch) throws ContentNotFoundException {
        return cached(service.post(merchantStore, language, slug), ifNoneMatch);
    }

    @GetMapping("/banners")
    public ResponseEntity<List<BannerView>> banners(@RequestParam BannerPlacement placement,
                                                     @RequestParam(required = false) String country,
                                                     @RequestParam(defaultValue = "false") boolean authenticated,
                                                     StoreMerchantId merchantStore, LanguageCode language,
                                                     @RequestHeader(value = "If-None-Match", required = false)
                                                     String ifNoneMatch) throws ContentNotFoundException {
        return cached(service.banners(merchantStore, language, placement, country, authenticated), ifNoneMatch);
    }

    @GetMapping("/faq/{groupId}")
    public ResponseEntity<Map<String, Object>> faq(@PathVariable Long groupId, StoreMerchantId merchantStore,
                                                    LanguageCode language,
                                                    @RequestHeader(value = "If-None-Match", required = false)
                                                    String ifNoneMatch) throws ContentNotFoundException {
        return cached(service.faqJsonLd(merchantStore, groupId), ifNoneMatch);
    }

    @GetMapping("/menus/{handle}")
    public ResponseEntity<MenuView> menu(@PathVariable String handle, StoreMerchantId merchantStore,
                                         LanguageCode language, @RequestHeader(value = "If-None-Match",
            required = false) String ifNoneMatch) throws ContentNotFoundException {
        return cached(service.menu(merchantStore, language, handle), ifNoneMatch);
    }

    @GetMapping("/policies/{type}")
    public ResponseEntity<PolicyView> policy(@PathVariable PolicyType type, StoreMerchantId merchantStore,
                                             LanguageCode language, @RequestHeader(value = "If-None-Match",
            required = false) String ifNoneMatch) throws PolicyNotFoundException, ContentNotFoundException {
        return cached(service.policy(merchantStore, type), ifNoneMatch);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ContentView>> search(@RequestParam String query,
                                                     @RequestParam(defaultValue = "25") int limit,
                                                     StoreMerchantId merchantStore, LanguageCode language,
                                                     @RequestHeader(value = "If-None-Match", required = false)
                                                     String ifNoneMatch) {
        return cached(service.search(merchantStore, language, query, limit), ifNoneMatch);
    }

    @GetMapping("/summary")
    public ResponseEntity<ContentSummary> summary(StoreMerchantId merchantStore, LanguageCode language,
                                                   @RequestHeader(value = "If-None-Match", required = false)
                                                   String ifNoneMatch) {
        return cached(service.summary(merchantStore), ifNoneMatch);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap(StoreMerchantId merchantStore, LanguageCode language,
                                           @RequestHeader(value = "If-None-Match", required = false)
                                           String ifNoneMatch) {
        return cached(service.sitemap(merchantStore, language), ifNoneMatch);
    }

    private static <T> ResponseEntity<T> cached(T body, String ifNoneMatch) {
        String etag = "\"%s\"".formatted(Integer.toUnsignedString(body.hashCode(), 16));
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag)
                    .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL).build();
        }
        return ResponseEntity.ok().eTag(etag).header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL).body(body);
    }
}
