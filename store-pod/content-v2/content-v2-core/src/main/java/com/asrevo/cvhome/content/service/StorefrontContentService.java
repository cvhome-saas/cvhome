package com.asrevo.cvhome.content.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.PolicyNotFoundException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentSummary;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.banner.BannerPlacement;
import com.asrevo.cvhome.content.model.banner.BannerView;
import com.asrevo.cvhome.content.model.faq.FaqView;
import com.asrevo.cvhome.content.model.menu.MenuView;
import com.asrevo.cvhome.content.model.page.PageView;
import com.asrevo.cvhome.content.model.policy.PolicyType;
import com.asrevo.cvhome.content.model.policy.PolicyView;
import com.asrevo.cvhome.content.model.post.PostView;

@Service
public class StorefrontContentService {
    private static final String JSON_LD_TYPE = "@type";
    private final ContentV2Service contentService;
    private final PageService pageService;
    private final PostService postService;
    private final BannerService bannerService;
    private final FaqService faqService;
    private final MenuService menuService;
    private final PolicyService policyService;

    public StorefrontContentService(ContentV2Service contentService, PageService pageService,
                                    PostService postService, BannerService bannerService, FaqService faqService,
                                    MenuService menuService, PolicyService policyService) {
        this.contentService = contentService;
        this.pageService = pageService;
        this.postService = postService;
        this.bannerService = bannerService;
        this.faqService = faqService;
        this.menuService = menuService;
        this.policyService = policyService;
    }

    @Transactional(readOnly = true)
    public PageView page(StoreMerchantId store, LanguageCode language, String slug)
            throws ContentNotFoundException {
        ContentV2Service.ResolvedRoute route = contentService.findPublishedRouteWithFallback(
                store, language, ContentType.PAGE, slug);
        return pageService.find(store, route.content().id(), language, route.resolvedLanguage(), route.fallback());
    }

    @Transactional(readOnly = true)
    public PostView post(StoreMerchantId store, LanguageCode language, String slug)
            throws ContentNotFoundException {
        ContentView route = contentService.findPublishedRoute(store, language, ContentType.POST, slug);
        return postService.find(store, route.id());
    }

    @Transactional(readOnly = true)
    public List<BannerView> banners(StoreMerchantId store, LanguageCode language, BannerPlacement placement,
                                    String countryCode, boolean authenticated) throws ContentNotFoundException {
        return bannerService.effective(store, language, placement, countryCode, authenticated);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> faqJsonLd(StoreMerchantId store, Long groupId) throws ContentNotFoundException {
        List<Map<String, Object>> entities = faqService.list(store, groupId).stream()
                .filter(it -> it.content().status() == ContentStatus.PUBLISHED)
                .map(StorefrontContentService::faqEntity).toList();
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("@context", "https://schema.org");
        document.put(JSON_LD_TYPE, "FAQPage");
        document.put("mainEntity", entities);
        return Map.copyOf(document);
    }

    @Transactional(readOnly = true)
    public MenuView menu(StoreMerchantId store, LanguageCode language, String handle)
            throws ContentNotFoundException {
        return menuService.findPublished(store, language, handle);
    }

    @Transactional(readOnly = true)
    public PolicyView policy(StoreMerchantId store, PolicyType type) throws PolicyNotFoundException,
            ContentNotFoundException {
        return policyService.active(store, type);
    }

    @Transactional(readOnly = true)
    public List<ContentView> search(StoreMerchantId store, LanguageCode language, String query, int limit) {
        return contentService.searchPublished(store, language, query, limit);
    }

    @Transactional(readOnly = true)
    public ContentSummary summary(StoreMerchantId store) {
        return contentService.publishedSummary(store);
    }

    @Transactional(readOnly = true)
    public String sitemap(StoreMerchantId store, LanguageCode language) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        contentService.listPublished(store).stream()
                .filter(it -> it.type() == ContentType.PAGE || it.type() == ContentType.POST)
                .map(it -> translation(it, language))
                .filter(java.util.Objects::nonNull)
                .filter(it -> it.slug() != null && !it.slug().isBlank())
                .forEach(it -> xml.append("<url><loc>").append(escapeXml(it.slug())).append("</loc></url>"));
        return xml.append("</urlset>").toString();
    }

    private static Map<String, Object> faqEntity(FaqView faq) {
        ContentView.TranslationView translation = faq.content().translations().getFirst();
        return Map.of(JSON_LD_TYPE, "Question", "name", translation.name(), "acceptedAnswer",
                Map.of(JSON_LD_TYPE, "Answer", "text", java.util.Objects.requireNonNullElse(
                        translation.description(), "")));
    }

    private static ContentView.TranslationView translation(ContentView content, LanguageCode language) {
        return content.translations().stream().filter(it -> it.language().equals(language)).findFirst()
                .orElseGet(() -> content.translations().stream().findFirst().orElse(null));
    }

    private static String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
