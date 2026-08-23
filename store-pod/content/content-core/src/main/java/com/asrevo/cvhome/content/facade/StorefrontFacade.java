package com.asrevo.cvhome.content.facade;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.entity.FaqGroup;
import com.asrevo.cvhome.content.entity.PolicyVersion;
import com.asrevo.cvhome.content.entity.PostCategory;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.MenuHandle;
import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.banner.BannerMeta;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.content.model.faq.FaqMeta;
import com.asrevo.cvhome.content.model.post.PostMeta;
import com.asrevo.cvhome.content.model.storefront.SitemapEntry;
import com.asrevo.cvhome.content.model.storefront.StorefrontBanner;
import com.asrevo.cvhome.content.model.storefront.StorefrontFaq;
import com.asrevo.cvhome.content.model.storefront.StorefrontLink;
import com.asrevo.cvhome.content.model.storefront.StorefrontMenuNode;
import com.asrevo.cvhome.content.model.storefront.StorefrontPage;
import com.asrevo.cvhome.content.model.storefront.StorefrontPolicy;
import com.asrevo.cvhome.content.model.storefront.StorefrontPost;
import com.asrevo.cvhome.content.model.storefront.StorefrontPostList;
import com.asrevo.cvhome.content.model.storefront.StorefrontSeo;
import com.asrevo.cvhome.content.model.storefront.StorefrontSite;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.service.FaqService;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.service.MenuService;
import com.asrevo.cvhome.content.service.PolicyService;
import com.asrevo.cvhome.content.service.PostCategoryService;
import com.asrevo.cvhome.content.service.RedirectService;
import com.asrevo.cvhome.content.service.binding.BannerBinding;
import com.asrevo.cvhome.content.service.binding.FaqBinding;
import com.asrevo.cvhome.content.service.binding.PostBinding;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.content.support.Strings;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * The public, read-only surface the storefront renders from. Never serves drafts. Every translated field comes
 * from the requested locale when that locale has a complete row, else from the first complete locale, and the
 * response names which locale actually served ({@code servedLocale}).
 */
@Component
@RequiredArgsConstructor
public class StorefrontFacade {

    private static final Set<String> SNIPPET_CODES = Set.of("meta-title", "meta-description");

    private static final String PAGE = "page";

    private static final String CATEGORY = "category";

    private static final String CATEGORY_HREF = "/blog?category=%s";

    private static final String CONTENT_PATH = "/content/%s";

    private static final String MONTHLY = "monthly";

    private static final String SPACE = " ";

    private static final java.util.regex.Pattern TAGS = java.util.regex.Pattern.compile("<[^>]+>");

    private static final java.util.regex.Pattern SPACES = java.util.regex.Pattern.compile("\\s+");

    private final ContentRepository contents;

    private final MenuService menus;

    private final MediaService media;

    private final PolicyService policies;

    private final PostCategoryService categories;

    private final FaqService faq;

    private final BannerBinding banners;

    private final RedirectService redirects;

    private final Clock clock;

    // ------------------------------------------------------------------------------------------------ site

    @Transactional
    public StorefrontSite site(StoreMerchantId store, LanguageCode language) {
        Instant now = clock.instant();
        StorefrontSite site = new StorefrontSite();
        site.setServedLocale(language == null ? null : language.code());

        Map<String, String> snippets = new LinkedHashMap<>();
        for (Content box : contents.findVisibleByType(store, ContentType.BOX)) {
            if (SNIPPET_CODES.contains(box.getCode())) {
                pick(box, language).ifPresent(d -> snippets.put(camel(box.getCode()), bodyOrTitle(d)));
            }
        }
        site.setSnippets(snippets);

        List<StorefrontBanner> strip = effectiveBanners(store, language, BannerPlacement.STRIP);
        if (!strip.isEmpty()) {
            site.setAnnouncement(strip.getFirst());
        } else {
            contents.findByCodeAndType("header-message", ContentType.BOX, store)
                    .filter(Content::isVisible)
                    .flatMap(box -> pick(box, language).map(d -> legacyAnnouncement(box, d)))
                    .ifPresent(site::setAnnouncement);
        }

        Map<String, List<StorefrontMenuNode>> menuMap = new LinkedHashMap<>();
        menuMap.put("main", menus.resolved(store, MenuHandle.MAIN, language, now));
        menuMap.put("footer", menus.resolved(store, MenuHandle.FOOTER, language, now));
        site.setMenus(menuMap);

        List<Content> pages = contents.findVisibleByType(store, ContentType.PAGE).stream()
                .filter(c -> c.servable(now)).toList();
        List<Content> footer = pages.stream().filter(Content::isShowInFooter).toList();
        site.setFooterPages((footer.isEmpty() ? pages : footer).stream()
                .map(c -> link(c, language, PAGE)).toList());

        List<StorefrontLink> policyLinks = new ArrayList<>();
        for (Content head : contents.findVisibleByType(store, ContentType.POLICY)) {
            if (!head.servable(now) || head.getPolicyType() == null || policies.live(head).isEmpty()) {
                continue;
            }
            pick(head, language).ifPresent(d -> policyLinks.add(new StorefrontLink(head.getCode(), title(d),
                    MenuService.href(com.asrevo.cvhome.content.model.MenuTargetKind.POLICY,
                            head.getPolicyType().name()), head.getPolicyType().name())));
        }
        site.setPolicies(policyLinks);
        return site;
    }

    // ------------------------------------------------------------------------------------------------ pages

    @Transactional(readOnly = true)
    public StorefrontPage page(StoreMerchantId store, LanguageCode language, String slug, boolean preview)
            throws ContentNotFoundException {
        Instant now = clock.instant();
        Content c = contents.findByCodeAndType(slug, ContentType.PAGE, store)
                .or(() -> contents.findBySeUrl(store, ContentType.PAGE, slug, language))
                .filter(x -> preview || x.servable(now))
                .orElseThrow(() -> ContentNotFoundException.byName(slug, store));
        ContentDescription d = pick(c, language).orElseThrow(() -> ContentNotFoundException.byName(slug, store));
        StorefrontPage p = new StorefrontPage();
        p.setId(c.getId());
        p.setSlug(c.getCode());
        p.setServedLocale(d.getLanguageCode().code());
        p.setTitle(title(d));
        p.setBody(d.getDescription());
        p.setTemplate(c.getTemplate());
        p.setSeo(seo(c, d, store));
        List<StorefrontLink> crumbs = new ArrayList<>();
        if (c.getParentId() != null) {
            contents.findById(c.getParentId()).filter(parent -> parent.servable(now))
                    .ifPresent(parent -> crumbs.add(link(parent, language, PAGE)));
        }
        crumbs.add(link(c, language, PAGE));
        p.setBreadcrumbs(crumbs);
        p.setUpdatedAt(c.getAuditSection() == null ? null : c.getAuditSection().getDateModified());
        return p;
    }

    // ------------------------------------------------------------------------------------------------ posts

    @Transactional(readOnly = true)
    public StorefrontPostList posts(StoreMerchantId store, LanguageCode language, String category, String tag,
                                    Pageable pageable) {
        Instant now = clock.instant();
        Map<Long, PostCategory> cats = categories.byIds(store);
        Long categoryId = category == null ? null : cats.values().stream()
                .filter(c -> c.getSlug().equals(category)).map(PostCategory::getId).findFirst().orElse(-1L);
        List<Content> all = contents.findVisibleByType(store, ContentType.POST).stream()
                .filter(c -> c.servable(now))
                .filter(c -> categoryId == null || PostBinding.meta(c).categoryIds().contains(categoryId))
                .filter(c -> tag == null || PostBinding.meta(c).tags().stream()
                        .anyMatch(t -> t.equalsIgnoreCase(tag)))
                .sorted(Comparator.comparing((Content c) -> c.getPublishAt() == null ? Instant.EPOCH
                        : c.getPublishAt()).reversed())
                .toList();
        int from = Math.min((int) pageable.getOffset(), all.size());
        int to = Math.min(from + pageable.getPageSize(), all.size());
        List<StorefrontPost> rows = new ArrayList<>();
        for (Content c : all.subList(from, to)) {
            pick(c, language).ifPresent(d -> rows.add(post(c, d, store, language, cats, false)));
        }
        StorefrontPostList list = new StorefrontPostList();
        list.setContent(rows);
        list.setTotalElements(all.size());
        list.setSize(rows.size());
        list.setRecordsFiltered(rows.size());
        list.setPageNumber(pageable.getPageNumber());
        list.setTotalPages(pageable.getPageSize() == 0 ? 1
                : (int) Math.ceil(all.size() / (double) pageable.getPageSize()));
        return list;
    }

    @Transactional(readOnly = true)
    public StorefrontPost post(StoreMerchantId store, LanguageCode language, String slug, boolean preview)
            throws ContentNotFoundException {
        Instant now = clock.instant();
        Content c = contents.findByCodeAndType(slug, ContentType.POST, store)
                .filter(x -> preview || x.servable(now))
                .orElseThrow(() -> ContentNotFoundException.byName(slug, store));
        ContentDescription d = pick(c, language).orElseThrow(() -> ContentNotFoundException.byName(slug, store));
        Map<Long, PostCategory> cats = categories.byIds(store);
        StorefrontPost p = post(c, d, store, language, cats, true);
        PostMeta meta = PostBinding.meta(c);
        // related: shared category first, then shared tag; published only; at most 3
        List<StorefrontPost> related = new ArrayList<>();
        for (Content other : contents.findVisibleByType(store, ContentType.POST)) {
            if (other.getId().equals(c.getId()) || !other.servable(now)) {
                continue;
            }
            PostMeta om = PostBinding.meta(other);
            boolean shared = om.categoryIds().stream().anyMatch(meta.categoryIds()::contains)
                    || om.tags().stream().anyMatch(meta.tags()::contains);
            if (shared) {
                pick(other, language).ifPresent(od -> related.add(post(other, od, store, language, cats, false)));
            }
            if (related.size() >= 3) {
                break;
            }
        }
        p.setRelated(related);
        return p;
    }

    @Transactional(readOnly = true)
    public List<StorefrontLink> postCategories(StoreMerchantId store, LanguageCode language) {
        List<StorefrontLink> out = new ArrayList<>();
        for (PostCategory c : categories.byIds(store).values()) {
            out.add(new StorefrontLink(c.getSlug(), localised(PostCategoryService.names(c), language, c.getSlug()),
                    String.format(CATEGORY_HREF, c.getSlug()), CATEGORY));
        }
        return out;
    }

    // ---------------------------------------------------------------------------------------------- banners

    @Transactional(readOnly = true)
    public List<StorefrontBanner> effectiveBanners(StoreMerchantId store, LanguageCode language,
                                                   BannerPlacement placement) {
        List<Content> live = contents.findVisibleByType(store, ContentType.BANNER).stream()
                .filter(c -> placement == null || c.getPlacement() == placement)
                .filter(banners::effective)
                .sorted(Comparator.comparing((Content c) -> c.getSortOrder() == null ? 0 : c.getSortOrder())
                        .thenComparing(c -> c.getPublishAt() == null ? Instant.EPOCH : c.getPublishAt(),
                                Comparator.reverseOrder()))
                .toList();
        List<Long> mediaIds = new ArrayList<>();
        for (Content c : live) {
            BannerMeta m = BannerBinding.meta(c);
            if (m.artwork() != null) {
                mediaIds.add(m.artwork().desktopMediaId());
                mediaIds.add(m.artwork().mobileMediaId());
            }
        }
        Map<Long, String> urls = media.urls(store, mediaIds);
        List<StorefrontBanner> out = new ArrayList<>();
        int position = 0;
        for (Content c : live) {
            Optional<ContentDescription> d = pick(c, language);
            if (d.isEmpty()) {
                continue;
            }
            BannerMeta m = BannerBinding.meta(c);
            StorefrontBanner b = new StorefrontBanner();
            b.setId(c.getId());
            b.setPlacement(c.getPlacement());
            b.setPosition(position++);
            b.setServedLocale(d.get().getLanguageCode().code());
            b.setTitle(title(d.get()));
            b.setSubtitle(d.get().getSubtitle());
            b.setBody(d.get().getDescription());
            b.setCtaLabel(d.get().getCtaLabel());
            b.setAltText(d.get().getAltText());
            b.setTarget(m.target());
            b.setTheme(m.theme());
            if (m.artwork() != null) {
                b.setDesktopUrl(urls.get(m.artwork().desktopMediaId()));
                b.setMobileUrl(urls.get(m.artwork().mobileMediaId()));
            }
            b.setStartsAt(c.getStartsAt());
            b.setEndsAt(c.getEndsAt());
            out.add(b);
        }
        return out;
    }

    // -------------------------------------------------------------------------------------------------- faq

    @Transactional
    public StorefrontFaq faq(StoreMerchantId store, LanguageCode language, String groupKey) {
        Instant now = clock.instant();
        Map<Long, FaqGroup> groups = faq.byIds(store);
        Map<Long, List<Content>> perGroup = entriesPerGroup(store, groups, now);
        List<StorefrontFaq.Group> list = new ArrayList<>();
        for (FaqGroup g : groups.values()) {
            List<Content> entries = perGroup.get(g.getId());
            boolean wanted = groupKey == null || groupKey.equals(g.getKey());
            if (!wanted || entries.isEmpty()) {
                continue;
            }
            StorefrontFaq.Group sg = new StorefrontFaq.Group();
            sg.setKey(g.getKey());
            sg.setName(localised(FaqService.names(g), language, g.getKey()));
            sg.setEntries(entries.stream().map(c -> faqEntry(c, language)).flatMap(Optional::stream).toList());
            list.add(sg);
        }
        StorefrontFaq out = new StorefrontFaq();
        out.setServedLocale(language == null ? null : language.code());
        out.setGroups(list);
        out.setJsonLd(faqJsonLd(list));
        return out;
    }

    private Map<Long, List<Content>> entriesPerGroup(StoreMerchantId store, Map<Long, FaqGroup> groups,
                                                     Instant now) {
        Map<Long, List<Content>> perGroup = new LinkedHashMap<>();
        groups.keySet().forEach(id -> perGroup.put(id, new ArrayList<>()));
        for (Content c : contents.findVisibleByType(store, ContentType.FAQ)) {
            if (c.servable(now) && c.getParentId() != null && perGroup.containsKey(c.getParentId())) {
                perGroup.get(c.getParentId()).add(c);
            }
        }
        Comparator<Content> order = Comparator.comparing((Content c) -> c.getSortOrder() == null ? 0
                : c.getSortOrder()).thenComparing(Content::getId);
        perGroup.values().forEach(list -> list.sort(order));
        return perGroup;
    }

    private static Optional<StorefrontFaq.Entry> faqEntry(Content c, LanguageCode language) {
        return pick(c, language).map(d -> {
            StorefrontFaq.Entry e = new StorefrontFaq.Entry();
            e.setId(c.getId());
            e.setSlug(c.getCode());
            e.setQuestion(title(d));
            e.setAnswer(d.getDescription());
            FaqMeta fm = FaqBinding.meta(c);
            e.setShowInCheckoutHelp(fm.showInCheckoutHelp());
            return e;
        });
    }

    /**
     * The {@code FAQPage} structured-data document for every entry served.
     */
    private static String faqJsonLd(List<StorefrontFaq.Group> groups) {
        List<String> questions = new ArrayList<>();
        for (StorefrontFaq.Group g : groups) {
            for (StorefrontFaq.Entry e : g.getEntries()) {
                questions.add(String.format(
                        "{\"@type\":\"Question\",\"name\":%s,\"acceptedAnswer\":{\"@type\":\"Answer\",\"text\":%s}}",
                        JsonCodec.write(e.getQuestion()), JsonCodec.write(plain(e.getAnswer()))));
            }
        }
        return String.format("{\"@context\":\"https://schema.org\",\"@type\":\"FAQPage\",\"mainEntity\":[%s]}",
                String.join(",", questions));
    }

    // ---------------------------------------------------------------------------------------------- policies

    @Transactional(readOnly = true)
    public StorefrontPolicy policy(StoreMerchantId store, LanguageCode language, PolicyType type, Integer version)
            throws ContentNotFoundException {
        Instant now = clock.instant();
        Content head = contents.findVisibleByType(store, ContentType.POLICY).stream()
                .filter(c -> c.getPolicyType() == type && c.servable(now)).findFirst()
                .orElseThrow(() -> ContentNotFoundException.byCode(type.name(), store));
        PolicyVersion v = (version == null ? policies.live(head) : policies.versionEntity(head, version))
                .orElseThrow(() -> ContentNotFoundException.byCode(type.name(), store));
        List<ContentTranslation> translations = PolicyService.translations(v);
        ContentTranslation t = pickTranslation(translations, language)
                .orElseThrow(() -> ContentNotFoundException.byCode(type.name(), store));
        StorefrontPolicy p = new StorefrontPolicy();
        p.setId(head.getId());
        p.setType(type);
        p.setSlug(head.getCode());
        p.setVersion(v.getVersion());
        p.setServedLocale(t.getLanguage().code());
        p.setHeading(t.getTitle());
        p.setBody(t.getBody());
        p.setEffectiveFrom(v.getEffectiveFrom());
        p.setRequiresAcceptance(com.asrevo.cvhome.content.service.binding.PolicyBinding.meta(head)
                .requiresAcceptance());
        return p;
    }

    // ---------------------------------------------------------------------------------------------- sitemap

    @Transactional(readOnly = true)
    public List<SitemapEntry> sitemap(StoreMerchantId store, LanguageCode language) {
        Instant now = clock.instant();
        List<SitemapEntry> out = new ArrayList<>();
        for (Content c : servable(store, ContentType.PAGE, now)) {
            if (!c.isNoindex()) {
                out.add(new SitemapEntry(String.format(CONTENT_PATH, c.getCode()), modified(c), MONTHLY, PAGE));
            }
        }
        for (Content c : servable(store, ContentType.POST, now)) {
            if (!c.isNoindex()) {
                out.add(new SitemapEntry(String.format("/blog/%s", c.getCode()), modified(c), "weekly", "post"));
            }
        }
        for (Content c : servable(store, ContentType.POLICY, now)) {
            if (c.getPolicyType() != null && policies.live(c).isPresent()) {
                out.add(new SitemapEntry(MenuService.href(com.asrevo.cvhome.content.model.MenuTargetKind.POLICY,
                        c.getPolicyType().name()), modified(c), "yearly", "policy"));
            }
        }
        if (!servable(store, ContentType.FAQ, now).isEmpty()) {
            out.add(new SitemapEntry("/help", now, MONTHLY, "faq"));
        }
        return out;
    }

    private List<Content> servable(StoreMerchantId store, ContentType type, Instant now) {
        return contents.findVisibleByType(store, type).stream().filter(c -> c.servable(now)).toList();
    }

    public Optional<String> redirect(StoreMerchantId store, String path) {
        return redirects.resolve(store, path);
    }

    // ---------------------------------------------------------------------------------------------- helpers

    private StorefrontPost post(Content c, ContentDescription d, StoreMerchantId store, LanguageCode language,
                                Map<Long, PostCategory> cats, boolean withBody) {
        PostMeta meta = PostBinding.meta(c);
        StorefrontPost p = new StorefrontPost();
        p.setId(c.getId());
        p.setSlug(c.getCode());
        p.setServedLocale(d.getLanguageCode().code());
        p.setTitle(title(d));
        p.setExcerpt(d.getExcerpt() != null ? d.getExcerpt() : excerpt(d.getDescription()));
        if (withBody) {
            p.setBody(d.getDescription());
            p.setSeo(seo(c, d, store));
        }
        p.setHeroImageUrl(media.url(store, meta.heroMediaId()).orElse(null));
        p.setPublishedAt(c.getPublishAt());
        p.setAuthorName(meta.authorName());
        p.setReadingMinutes(PostBinding.readingMinutes(c));
        p.setFeatured(meta.featured());
        p.setCategories(meta.categoryIds().stream().map(cats::get).filter(java.util.Objects::nonNull)
                .map(cat -> new StorefrontLink(cat.getSlug(),
                        localised(PostCategoryService.names(cat), language, cat.getSlug()),
                        String.format(CATEGORY_HREF, cat.getSlug()), CATEGORY))
                .toList());
        p.setTags(meta.tags());
        return p;
    }

    private StorefrontSeo seo(Content c, ContentDescription d, StoreMerchantId store) {
        StorefrontSeo seo = new StorefrontSeo();
        seo.setMetaTitle(d.getMetatagTitle() != null ? d.getMetatagTitle()
                : (d.getTitle() != null ? d.getTitle() : title(d)));
        seo.setMetaDescription(d.getMetatagDescription());
        seo.setKeywords(d.getMetatagKeywords());
        seo.setCanonicalUrl(c.getCanonicalUrl());
        seo.setNoindex(c.isNoindex());
        seo.setOgImageUrl(media.url(store, c.getOgMediaId()).orElse(null));
        return seo;
    }

    private static StorefrontBanner legacyAnnouncement(Content box, ContentDescription d) {
        StorefrontBanner b = new StorefrontBanner();
        b.setId(box.getId());
        b.setPlacement(BannerPlacement.STRIP);
        b.setPosition(0);
        b.setServedLocale(d.getLanguageCode().code());
        b.setTitle(title(d));
        b.setBody(d.getDescription());
        return b;
    }

    private static StorefrontLink link(Content c, LanguageCode language, String type) {
        String title = pick(c, language).map(StorefrontFacade::title).orElse(c.getCode());
        return new StorefrontLink(c.getCode(), title, String.format(CONTENT_PATH, c.getCode()), type);
    }

    /**
     * The row for {@code language} if present, else the first complete row.
     */
    static Optional<ContentDescription> pick(Content c, LanguageCode language) {
        Optional<ContentDescription> wanted = c.description(language);
        if (wanted.isPresent()) {
            return wanted;
        }
        return c.getDescriptions().stream()
                .sorted(Comparator.comparing(d -> d.getLanguageCode().code()))
                .findFirst();
    }

    static Optional<ContentTranslation> pickTranslation(List<ContentTranslation> all, LanguageCode language) {
        return all.stream().filter(t -> t.getLanguage() != null && t.getLanguage().equals(language)).findFirst()
                .or(() -> all.stream().sorted(Comparator.comparing(t -> t.getLanguage().code())).findFirst());
    }

    static String title(ContentDescription d) {
        return d.getName();
    }

    private static String bodyOrTitle(ContentDescription d) {
        return Strings.blank(d.getDescription()) ? title(d) : plain(d.getDescription());
    }

    static String localised(Map<String, String> names, LanguageCode language, String fallback) {
        String v = language == null ? null : names.get(language.code());
        if (v != null) {
            return v;
        }
        return names.values().stream().findFirst().orElse(fallback);
    }

    static String camel(String code) {
        String[] parts = code.split("-");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(parts[i].substring(0, 1).toUpperCase(Locale.ROOT)).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    static String plain(String html) {
        return html == null ? null : SPACES.matcher(TAGS.matcher(html).replaceAll(SPACE)).replaceAll(SPACE).trim();
    }

    static String excerpt(String html) {
        String text = plain(html);
        if (text == null) {
            return null;
        }
        return text.length() <= 200 ? text : String.format("%s…", text.substring(0, 197));
    }

    private static Instant modified(Content c) {
        return c.getAuditSection() != null && c.getAuditSection().getDateModified() != null
                ? c.getAuditSection().getDateModified() : c.getPublishAt();
    }


}
