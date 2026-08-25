package com.asrevo.cvhome.content.facade;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.entity.FaqGroup;
import com.asrevo.cvhome.content.entity.PolicyVersion;
import com.asrevo.cvhome.content.entity.PostCategory;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.PolicyVersionStatus;
import com.asrevo.cvhome.content.model.banner.BannerArtwork;
import com.asrevo.cvhome.content.model.banner.BannerMeta;
import com.asrevo.cvhome.content.model.banner.BannerTarget;
import com.asrevo.cvhome.content.model.post.PostMeta;
import com.asrevo.cvhome.content.model.storefront.SitemapEntry;
import com.asrevo.cvhome.content.model.storefront.StorefrontBanner;
import com.asrevo.cvhome.content.model.storefront.StorefrontFaq;
import com.asrevo.cvhome.content.model.storefront.StorefrontLink;
import com.asrevo.cvhome.content.model.storefront.StorefrontPage;
import com.asrevo.cvhome.content.model.storefront.StorefrontPolicy;
import com.asrevo.cvhome.content.model.storefront.StorefrontPost;
import com.asrevo.cvhome.content.model.storefront.StorefrontPostList;
import com.asrevo.cvhome.content.model.storefront.StorefrontSite;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.service.FaqService;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.service.MenuService;
import com.asrevo.cvhome.content.service.PolicyService;
import com.asrevo.cvhome.content.service.PostCategoryService;
import com.asrevo.cvhome.content.service.RedirectService;
import com.asrevo.cvhome.content.service.binding.BannerBinding;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The read-only storefront surface. Two rules run through all of it: a draft is never served, and every
 * translated field comes from the asked locale when it exists and from the first complete one otherwise, with
 * {@code servedLocale} naming which one actually answered.
 */
class StorefrontFacadeTest {

    private static final String ARABIC_TITLE = "عن";

    private static final String META_TITLE_CODE = "meta-title";

    private static final String META_TITLE_KEY = "metaTitle";

    private static final String AGREEMENT_CODE = "agreement";

    private static final String EN = "en";

    private static final String NEWS_NAME = "News";

    private static final String NEWS_SLUG = "news";

    private static final String AR = "ar";

    private static final String ARABIC_NEWS = "أخبار";

    private static final String TERMS_TITLE = "Terms";

    private static final String TERMS_SLUG = "terms";

    private static final String HEADER_MESSAGE_CODE = "header-message";

    private static final String ABOUT_PATH = "/content/about";

    private static final String TERMS_PATH = "/policies/terms";

    private static final String SALE_TITLE = "Sale!";

    private static final String FREE_SHIPPING = "Free shipping";

    private static final String LEGACY_URL = "uber-uns";

    private static final String PARENT_SLUG = "company";

    private static final String PARENT_TITLE = "Company";

    private static final String SEO_TITLE = "About | Example";

    private static final String HELLO_TITLE = "Hello";

    private static final String SECOND_SLUG = "second";

    private static final String AUTHOR = "Ada";

    private static final String DESKTOP_URL = "https://cdn.test/5.png";

    private static final String SLUG_B = "b";

    private static final String SLUG_C = "c";

    private static final String HANDWRITTEN = "Handwritten";

    private static final String SLUG_A = "a";

    private static final String TITLE_A = "A";

    private static final String SALE_PATH = "/sale";

    private static final String MOBILE_URL = "https://cdn.test/6.png";

    private static final String QUESTION_SLUG = "q1";

    private static final String QUESTION = "How?";

    private static final String GENERAL_KEY = "general";

    private static final String GENERAL_NAME = "General";

    private static final String OLD_PATH = "/content/old";

    private static final String MOVED_PATH = "/content/new";

    private static final String ABOUT = "about";

    private static final String ABOUT_TITLE = "About";

    private static final String HELLO = "hello-world";

    private static final String BODY = "<p>body</p>";

    private ContentRepository contents;

    private MenuService menus;

    private MediaService media;

    private PolicyService policies;

    private PostCategoryService categories;

    private FaqService faq;

    private BannerBinding banners;

    private RedirectService redirects;

    private StorefrontFacade facade;

    @BeforeEach
    void setUp() {
        contents = mock(ContentRepository.class);
        menus = mock(MenuService.class);
        media = mock(MediaService.class);
        policies = mock(PolicyService.class);
        categories = mock(PostCategoryService.class);
        faq = mock(FaqService.class);
        banners = mock(BannerBinding.class);
        redirects = mock(RedirectService.class);
        facade = new StorefrontFacade(contents, menus, media, policies, categories, faq, banners, redirects,
                ContentFixtures.clock());
    }

    private static PostCategory category(Long id, String slug, Map<String, String> names) {
        PostCategory c = new PostCategory();
        c.setId(id);
        c.setSlug(slug);
        c.setNames(JsonCodec.write(names));
        return c;
    }

    private static FaqGroup faqGroup(Long id, String key, Map<String, String> names) {
        FaqGroup g = new FaqGroup();
        g.setId(id);
        g.setKey(key);
        g.setNames(JsonCodec.write(names));
        return g;
    }

    @Nested
    class LocalePicking {

        @Test
        void theAskedLocaleWinsWhenItIsThere() {
            Content c = ContentFixtures.published(1L, ContentType.PAGE, ABOUT, ABOUT_TITLE);
            c.getDescriptions().add(ContentFixtures.description(c, ContentFixtures.AR, ARABIC_TITLE, BODY));

            assertThat(StorefrontFacade.pick(c, ContentFixtures.AR)).get()
                    .extracting(ContentDescription::getLanguageCode).isEqualTo(ContentFixtures.AR);
        }

        @Test
        void anAbsentLocaleFallsBackToTheAlphabeticallyFirstOne() {
            Content c = ContentFixtures.published(1L, ContentType.PAGE, ABOUT, ABOUT_TITLE);
            c.getDescriptions().add(ContentFixtures.description(c, ContentFixtures.AR, ARABIC_TITLE, BODY));

            assertThat(StorefrontFacade.pick(c, new com.asrevo.cvhome.commons.domain.LanguageCode("fr"))).get()
                    .extracting(ContentDescription::getLanguageCode).isEqualTo(ContentFixtures.AR);
        }

        @Test
        void anItemWithNoLocaleAtAllPicksNothing() {
            assertThat(StorefrontFacade.pick(ContentFixtures.content(1L, ContentType.PAGE, ABOUT),
                    ContentFixtures.EN)).isEmpty();
        }

        @Test
        void aPolicyTranslationFallsBackTheSameWay() {
            var english = ContentFixtures.translation(ContentFixtures.EN, ABOUT_TITLE, BODY);
            var arabic = ContentFixtures.translation(ContentFixtures.AR, ARABIC_TITLE, BODY);

            assertThat(StorefrontFacade.pickTranslation(List.of(english, arabic), ContentFixtures.EN))
                    .contains(english);
            assertThat(StorefrontFacade.pickTranslation(List.of(english, arabic), null)).contains(arabic);
        }

    }

    @Nested
    class TextHelpers {

        @Test
        void plainStripsMarkupAndCollapsesWhitespace() {
            assertThat(StorefrontFacade.plain("<p>a  <b>b</b>\n c</p>")).isEqualTo("a b c");
            assertThat(StorefrontFacade.plain(null)).isNull();
        }

        @Test
        void anExcerptIsCutWithAnEllipsisOnlyWhenItIsTooLong() {
            assertThat(StorefrontFacade.excerpt(null)).isNull();
            assertThat(StorefrontFacade.excerpt("<p>short</p>")).isEqualTo("short");
            assertThat(StorefrontFacade.excerpt(String.format("<p>%s</p>", "x".repeat(400))))
                    .hasSize(198).endsWith("…");
        }

        @Test
        void aSnippetCodeBecomesACamelCaseKey() {
            assertThat(StorefrontFacade.camel(META_TITLE_CODE)).isEqualTo(META_TITLE_KEY);
            assertThat(StorefrontFacade.camel(AGREEMENT_CODE)).isEqualTo(AGREEMENT_CODE);
        }

        @Test
        void aLocalisedNameFallsBackToAnyOtherThenTheGivenDefault() {
            assertThat(StorefrontFacade.localised(Map.of(EN, NEWS_NAME), ContentFixtures.EN, NEWS_SLUG))
                    .isEqualTo(NEWS_NAME);
            assertThat(StorefrontFacade.localised(Map.of(AR, ARABIC_NEWS), ContentFixtures.EN, NEWS_SLUG))
                    .isEqualTo(ARABIC_NEWS);
            assertThat(StorefrontFacade.localised(Map.of(), null, NEWS_SLUG)).isEqualTo(NEWS_SLUG);
        }

    }

    @Nested
    class Site {

        @Test
        void theSiteCarriesSnippetsMenusFooterPagesAndPolicyLinks() {
            Content snippet = ContentFixtures.published(1L, ContentType.BOX, META_TITLE_CODE, "Example shop");
            Content ignored = ContentFixtures.published(2L, ContentType.BOX, AGREEMENT_CODE, TERMS_TITLE);
            Content footerPage = ContentFixtures.published(3L, ContentType.PAGE, ABOUT, ABOUT_TITLE);
            footerPage.setShowInFooter(true);
            Content policyHead = ContentFixtures.published(4L, ContentType.POLICY, TERMS_SLUG, TERMS_TITLE);
            policyHead.setPolicyType(PolicyType.TERMS);
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BOX))
                    .thenReturn(List.of(snippet, ignored));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BANNER)).thenReturn(List.of());
            when(contents.findByCodeAndType(HEADER_MESSAGE_CODE, ContentType.BOX, ContentFixtures.STORE))
                    .thenReturn(Optional.empty());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.PAGE)).thenReturn(List.of(footerPage));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY))
                    .thenReturn(List.of(policyHead));
            when(policies.live(policyHead)).thenReturn(Optional.of(new PolicyVersion()));
            when(menus.resolved(any(), any(), any(), any())).thenReturn(List.of());

            StorefrontSite site = facade.site(ContentFixtures.STORE, ContentFixtures.EN);

            assertThat(site.getServedLocale()).isEqualTo(EN);
            assertThat(site.getSnippets()).containsOnlyKeys(META_TITLE_KEY);
            assertThat(site.getSnippets()).containsEntry(META_TITLE_KEY, "body");
            assertThat(site.getMenus()).containsOnlyKeys("main", "footer");
            assertThat(site.getFooterPages()).extracting(StorefrontLink::getHref).containsExactly(ABOUT_PATH);
            assertThat(site.getPolicies()).extracting(StorefrontLink::getHref).containsExactly(TERMS_PATH);
            assertThat(site.getAnnouncement()).isNull();
        }

        @Test
        void aStripBannerBecomesTheAnnouncement() {
            Content banner = ContentFixtures.published(1L, ContentType.BANNER, "sale", SALE_TITLE);
            banner.setPlacement(BannerPlacement.STRIP);
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BOX)).thenReturn(List.of());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BANNER)).thenReturn(List.of(banner));
            when(banners.effective(banner)).thenReturn(true);
            when(media.urls(any(), anyList())).thenReturn(new java.util.HashMap<>());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.PAGE)).thenReturn(List.of());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of());
            when(menus.resolved(any(), any(), any(), any())).thenReturn(List.of());

            StorefrontSite site = facade.site(ContentFixtures.STORE, ContentFixtures.EN);

            assertThat(site.getAnnouncement()).isNotNull();
            assertThat(site.getAnnouncement().getTitle()).isEqualTo(SALE_TITLE);
        }

        @Test
        void withoutAStripBannerTheLegacyHeaderMessageIsUsed() {
            Content box = ContentFixtures.published(1L, ContentType.BOX, HEADER_MESSAGE_CODE, FREE_SHIPPING);
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BOX)).thenReturn(List.of());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BANNER)).thenReturn(List.of());
            when(contents.findByCodeAndType(HEADER_MESSAGE_CODE, ContentType.BOX, ContentFixtures.STORE))
                    .thenReturn(Optional.of(box));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.PAGE)).thenReturn(List.of());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of());
            when(menus.resolved(any(), any(), any(), any())).thenReturn(List.of());

            StorefrontSite site = facade.site(ContentFixtures.STORE, ContentFixtures.EN);

            assertThat(site.getAnnouncement().getPlacement()).isEqualTo(BannerPlacement.STRIP);
            assertThat(site.getAnnouncement().getTitle()).isEqualTo(FREE_SHIPPING);
        }

        @Test
        void withNoFooterPagesEveryServablePageIsListed() {
            Content page = ContentFixtures.published(1L, ContentType.PAGE, ABOUT, ABOUT_TITLE);
            Content draft = ContentFixtures.content(2L, ContentType.PAGE, "draft");
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BOX)).thenReturn(List.of());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BANNER)).thenReturn(List.of());
            when(contents.findByCodeAndType(HEADER_MESSAGE_CODE, ContentType.BOX, ContentFixtures.STORE))
                    .thenReturn(Optional.empty());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.PAGE))
                    .thenReturn(List.of(page, draft));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of());
            when(menus.resolved(any(), any(), any(), any())).thenReturn(List.of());

            assertThat(facade.site(ContentFixtures.STORE, null).getFooterPages())
                    .extracting(StorefrontLink::getTitle).containsExactly(ABOUT_TITLE);
        }

        @Test
        void aPolicyWithNoLiveVersionIsNotLinked() {
            Content policyHead = ContentFixtures.published(1L, ContentType.POLICY, TERMS_SLUG, TERMS_TITLE);
            policyHead.setPolicyType(PolicyType.TERMS);
            Content untyped = ContentFixtures.published(2L, ContentType.POLICY, "misc", "Misc");
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BOX)).thenReturn(List.of());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BANNER)).thenReturn(List.of());
            when(contents.findByCodeAndType(HEADER_MESSAGE_CODE, ContentType.BOX, ContentFixtures.STORE))
                    .thenReturn(Optional.empty());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.PAGE)).thenReturn(List.of());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY))
                    .thenReturn(List.of(policyHead, untyped));
            when(policies.live(policyHead)).thenReturn(Optional.empty());
            when(menus.resolved(any(), any(), any(), any())).thenReturn(List.of());

            assertThat(facade.site(ContentFixtures.STORE, ContentFixtures.EN).getPolicies()).isEmpty();
        }

    }

    @Nested
    class Pages {

        @Test
        void aDraftPageIsNotServedButIsVisibleInPreview() throws Exception {
            Content draft = ContentFixtures.content(1L, ContentType.PAGE, ABOUT);
            draft.getDescriptions().add(ContentFixtures.description(draft, ContentFixtures.EN, ABOUT_TITLE, BODY));
            when(contents.findByCodeAndType(ABOUT, ContentType.PAGE, ContentFixtures.STORE))
                    .thenReturn(Optional.of(draft));
            when(contents.findBySeUrl(any(), any(), any(), any())).thenReturn(Optional.empty());
            when(media.url(any(), any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> facade.page(ContentFixtures.STORE, ContentFixtures.EN, ABOUT, false))
                    .isInstanceOf(ContentNotFoundException.class);
            assertThat(facade.page(ContentFixtures.STORE, ContentFixtures.EN, ABOUT, true).getSlug())
                    .isEqualTo(ABOUT);
        }

        @Test
        void aPageWithoutAnyTranslationReadsAsMissing() {
            Content page = ContentFixtures.content(1L, ContentType.PAGE, ABOUT);
            page.setStatus(com.asrevo.cvhome.content.model.ContentStatus.PUBLISHED);
            page.setVisible(true);
            when(contents.findByCodeAndType(ABOUT, ContentType.PAGE, ContentFixtures.STORE))
                    .thenReturn(Optional.of(page));

            assertThatThrownBy(() -> facade.page(ContentFixtures.STORE, ContentFixtures.EN, ABOUT, false))
                    .isInstanceOf(ContentNotFoundException.class);
        }

        @Test
        void aPageIsAlsoFoundByItsLegacyFriendlyUrl() throws Exception {
            Content page = ContentFixtures.published(1L, ContentType.PAGE, ABOUT, ABOUT_TITLE);
            when(contents.findByCodeAndType(LEGACY_URL, ContentType.PAGE, ContentFixtures.STORE))
                    .thenReturn(Optional.empty());
            when(contents.findBySeUrl(ContentFixtures.STORE, ContentType.PAGE, LEGACY_URL, ContentFixtures.EN))
                    .thenReturn(Optional.of(page));
            when(media.url(any(), any())).thenReturn(Optional.empty());

            assertThat(facade.page(ContentFixtures.STORE, ContentFixtures.EN, LEGACY_URL, false).getSlug())
                    .isEqualTo(ABOUT);
        }

        @Test
        void aChildPageBreadcrumbsThroughItsServableParent() throws Exception {
            Content parent = ContentFixtures.published(1L, ContentType.PAGE, PARENT_SLUG, PARENT_TITLE);
            Content child = ContentFixtures.published(2L, ContentType.PAGE, ABOUT, ABOUT_TITLE);
            child.setParentId(1L);
            when(contents.findByCodeAndType(ABOUT, ContentType.PAGE, ContentFixtures.STORE))
                    .thenReturn(Optional.of(child));
            when(contents.findById(1L)).thenReturn(Optional.of(parent));
            when(media.url(any(), any())).thenReturn(Optional.empty());

            StorefrontPage out = facade.page(ContentFixtures.STORE, ContentFixtures.EN, ABOUT, false);

            assertThat(out.getBreadcrumbs()).extracting(StorefrontLink::getTitle)
                    .containsExactly(PARENT_TITLE, ABOUT_TITLE);
            assertThat(out.getServedLocale()).isEqualTo(EN);
            assertThat(out.getSeo().getMetaTitle()).isEqualTo(ABOUT_TITLE);
            assertThat(out.getSeo().isNoindex()).isFalse();
        }

        @Test
        void aDraftParentIsLeftOutOfTheBreadcrumbs() throws Exception {
            Content child = ContentFixtures.published(2L, ContentType.PAGE, ABOUT, ABOUT_TITLE);
            child.setParentId(1L);
            when(contents.findByCodeAndType(ABOUT, ContentType.PAGE, ContentFixtures.STORE))
                    .thenReturn(Optional.of(child));
            when(contents.findById(1L)).thenReturn(Optional.of(
                    ContentFixtures.content(1L, ContentType.PAGE, PARENT_SLUG)));
            when(media.url(any(), any())).thenReturn(Optional.empty());

            assertThat(facade.page(ContentFixtures.STORE, ContentFixtures.EN, ABOUT, false).getBreadcrumbs())
                    .hasSize(1);
        }

        @Test
        void anExplicitMetaTitleWinsOverTheHeading() throws Exception {
            Content page = ContentFixtures.published(1L, ContentType.PAGE, ABOUT, ABOUT_TITLE);
            page.getDescriptions().getFirst().setMetatagTitle(SEO_TITLE);
            page.setNoindex(true);
            when(contents.findByCodeAndType(ABOUT, ContentType.PAGE, ContentFixtures.STORE))
                    .thenReturn(Optional.of(page));
            when(media.url(any(), any())).thenReturn(Optional.empty());

            StorefrontPage out = facade.page(ContentFixtures.STORE, ContentFixtures.EN, ABOUT, false);

            assertThat(out.getSeo().getMetaTitle()).isEqualTo(SEO_TITLE);
            assertThat(out.getSeo().isNoindex()).isTrue();
        }

    }

    @Nested
    class Posts {

        private Content post(Long id, String slug, PostMeta meta) {
            Content c = ContentFixtures.published(id, ContentType.POST, slug, HELLO_TITLE);
            c.setMeta(JsonCodec.write(meta));
            return c;
        }

        @Test
        void theListIsNewestFirstAndPagedInMemory() {
            Content older = post(1L, HELLO, new PostMeta(null, List.of(), List.of(), null, false));
            older.setPublishAt(ContentFixtures.NOW.minusSeconds(7200));
            Content newer = post(2L, SECOND_SLUG, new PostMeta(null, List.of(), List.of(), null, false));
            Content undated = post(3L, "third", new PostMeta(null, List.of(), List.of(), null, false));
            undated.setPublishAt(null);
            when(categories.byIds(ContentFixtures.STORE)).thenReturn(Map.of());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POST))
                    .thenReturn(List.of(older, newer, undated));
            when(media.url(any(), any())).thenReturn(Optional.empty());

            StorefrontPostList list = facade.posts(ContentFixtures.STORE, ContentFixtures.EN, null, null,
                    PageRequest.of(0, 2));

            assertThat(list.getContent()).extracting(StorefrontPost::getSlug).containsExactly(SECOND_SLUG, HELLO);
            assertThat(list.getTotalElements()).isEqualTo(3);
            assertThat(list.getTotalPages()).isEqualTo(2);
        }

        @Test
        void anUnknownCategoryMatchesNothingRatherThanEverything() {
            when(categories.byIds(ContentFixtures.STORE))
                    .thenReturn(Map.of(1L, category(1L, NEWS_SLUG, Map.of(EN, NEWS_NAME))));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POST))
                    .thenReturn(List.of(post(1L, HELLO, new PostMeta(null, List.of(1L), List.of(), null, false))));

            assertThat(facade.posts(ContentFixtures.STORE, ContentFixtures.EN, "nope", null, PageRequest.of(0, 10))
                    .getContent()).isEmpty();
        }

        @Test
        void filteringByCategoryAndByTagBothNarrowTheList() {
            Content tagged = post(1L, HELLO, new PostMeta(null, List.of(1L), List.of(NEWS_NAME), null, false));
            Content other = post(2L, SECOND_SLUG, new PostMeta(null, List.of(), List.of(), null, false));
            when(categories.byIds(ContentFixtures.STORE))
                    .thenReturn(Map.of(1L, category(1L, NEWS_SLUG, Map.of(EN, NEWS_NAME))));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POST))
                    .thenReturn(List.of(tagged, other));
            when(media.url(any(), any())).thenReturn(Optional.empty());

            assertThat(facade.posts(ContentFixtures.STORE, ContentFixtures.EN, NEWS_SLUG, null,
                    PageRequest.of(0, 10)).getContent()).hasSize(1);
            assertThat(facade.posts(ContentFixtures.STORE, ContentFixtures.EN, null, NEWS_SLUG,
                    PageRequest.of(0, 10)).getContent()).hasSize(1);
        }

        @Test
        void aPostCarriesItsCategoriesTagsAndReadingTime() throws Exception {
            Content c = post(1L, HELLO, new PostMeta(5L, List.of(1L, 9L), List.of(NEWS_SLUG), AUTHOR, true));
            when(contents.findByCodeAndType(HELLO, ContentType.POST, ContentFixtures.STORE))
                    .thenReturn(Optional.of(c));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POST)).thenReturn(List.of(c));
            when(categories.byIds(ContentFixtures.STORE))
                    .thenReturn(Map.of(1L, category(1L, NEWS_SLUG, Map.of(EN, NEWS_NAME))));
            when(media.url(any(), any())).thenReturn(Optional.of(DESKTOP_URL));

            StorefrontPost out = facade.post(ContentFixtures.STORE, ContentFixtures.EN, HELLO, false);

            assertThat(out.getHeroImageUrl()).isEqualTo(DESKTOP_URL);
            assertThat(out.getCategories()).extracting(StorefrontLink::getHref).containsExactly("/blog?category=news");
            assertThat(out.getTags()).containsExactly(NEWS_SLUG);
            assertThat(out.getAuthorName()).isEqualTo(AUTHOR);
            assertThat(out.isFeatured()).isTrue();
            assertThat(out.getBody()).isEqualTo(BODY);
            assertThat(out.getRelated()).isEmpty();
        }

        @Test
        void relatedPostsShareACategoryOrATagAndStopAtThree() throws Exception {
            Content main = post(1L, HELLO, new PostMeta(null, List.of(1L), List.of(NEWS_SLUG), null, false));
            Content sameCategory = post(2L, SLUG_B, new PostMeta(null, List.of(1L), List.of(), null, false));
            Content sameTag = post(3L, SLUG_C, new PostMeta(null, List.of(), List.of(NEWS_SLUG), null, false));
            Content unrelated = post(4L, "d", new PostMeta(null, List.of(9L), List.of("other"), null, false));
            when(contents.findByCodeAndType(HELLO, ContentType.POST, ContentFixtures.STORE))
                    .thenReturn(Optional.of(main));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POST))
                    .thenReturn(List.of(main, sameCategory, sameTag, unrelated));
            when(categories.byIds(ContentFixtures.STORE)).thenReturn(Map.of());
            when(media.url(any(), any())).thenReturn(Optional.empty());

            assertThat(facade.post(ContentFixtures.STORE, ContentFixtures.EN, HELLO, false).getRelated())
                    .extracting(StorefrontPost::getSlug).containsExactly(SLUG_B, SLUG_C);
        }

        @Test
        void aDraftPostIsNotServed() {
            Content draft = ContentFixtures.content(1L, ContentType.POST, HELLO);
            when(contents.findByCodeAndType(HELLO, ContentType.POST, ContentFixtures.STORE))
                    .thenReturn(Optional.of(draft));

            assertThatThrownBy(() -> facade.post(ContentFixtures.STORE, ContentFixtures.EN, HELLO, false))
                    .isInstanceOf(ContentNotFoundException.class);
        }

        @Test
        void aStoredExcerptWinsOverTheGeneratedOne() {
            Content c = post(1L, HELLO, new PostMeta(null, List.of(), List.of(), null, false));
            c.getDescriptions().getFirst().setExcerpt(HANDWRITTEN);
            when(categories.byIds(ContentFixtures.STORE)).thenReturn(Map.of());
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POST)).thenReturn(List.of(c));
            when(media.url(any(), any())).thenReturn(Optional.empty());

            assertThat(facade.posts(ContentFixtures.STORE, ContentFixtures.EN, null, null, PageRequest.of(0, 10))
                    .getContent()).singleElement()
                    .satisfies(p -> assertThat(p.getExcerpt()).isEqualTo(HANDWRITTEN));
        }

        @Test
        void theCategoryIndexIsLocalised() {
            when(categories.byIds(ContentFixtures.STORE))
                    .thenReturn(Map.of(1L, category(1L, NEWS_SLUG, Map.of(AR, ARABIC_NEWS))));

            assertThat(facade.postCategories(ContentFixtures.STORE, ContentFixtures.EN))
                    .extracting(StorefrontLink::getTitle).containsExactly(ARABIC_NEWS);
        }

    }

    @Nested
    class Banners {

        @Test
        void bannersAreOrderedBySortThenNewestAndResolveTheirArtwork() {
            Content first = ContentFixtures.published(1L, ContentType.BANNER, SLUG_A, TITLE_A);
            first.setPlacement(BannerPlacement.CAROUSEL);
            first.setSortOrder(0);
            first.setMeta(JsonCodec.write(new BannerMeta(new BannerTarget(BannerTarget.Kind.URL, SALE_PATH),
                    new BannerArtwork(5L, 6L, null), null, false)));
            Content second = ContentFixtures.published(2L, ContentType.BANNER, SLUG_B, "B");
            second.setPlacement(BannerPlacement.CAROUSEL);
            second.setSortOrder(null);
            Content otherPlacement = ContentFixtures.published(3L, ContentType.BANNER, SLUG_C, "C");
            otherPlacement.setPlacement(BannerPlacement.HERO);
            Map<Long, String> urls = new java.util.HashMap<>();
            urls.put(5L, DESKTOP_URL);
            urls.put(6L, MOBILE_URL);
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BANNER))
                    .thenReturn(List.of(first, second, otherPlacement));
            when(banners.effective(any())).thenReturn(true);
            when(media.urls(any(), anyList())).thenReturn(urls);

            List<StorefrontBanner> out = facade.effectiveBanners(ContentFixtures.STORE, ContentFixtures.EN,
                    BannerPlacement.CAROUSEL);

            assertThat(out).extracting(StorefrontBanner::getId).containsExactly(1L, 2L);
            assertThat(out.getFirst().getPosition()).isZero();
            assertThat(out.getFirst().getDesktopUrl()).isEqualTo(DESKTOP_URL);
            assertThat(out.getFirst().getMobileUrl()).isEqualTo(MOBILE_URL);
            assertThat(out.getFirst().getTarget().value()).isEqualTo(SALE_PATH);
        }

        @Test
        void aBannerTheBindingCallsIneffectiveIsLeftOut() {
            Content banner = ContentFixtures.published(1L, ContentType.BANNER, SLUG_A, TITLE_A);
            banner.setPlacement(BannerPlacement.HERO);
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BANNER)).thenReturn(List.of(banner));
            when(banners.effective(banner)).thenReturn(false);
            when(media.urls(any(), anyList())).thenReturn(new java.util.HashMap<>());

            assertThat(facade.effectiveBanners(ContentFixtures.STORE, ContentFixtures.EN, null)).isEmpty();
        }

        @Test
        void aBannerWithNoTranslationAtAllIsSkipped() {
            Content banner = ContentFixtures.content(1L, ContentType.BANNER, SLUG_A);
            banner.setPlacement(BannerPlacement.HERO);
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.BANNER)).thenReturn(List.of(banner));
            when(banners.effective(banner)).thenReturn(true);
            when(media.urls(any(), anyList())).thenReturn(new java.util.HashMap<>());

            assertThat(facade.effectiveBanners(ContentFixtures.STORE, ContentFixtures.EN, null)).isEmpty();
        }

    }

    @Nested
    class Faq {

        @Test
        void groupsWithoutServableEntriesAreLeftOutAndTheRestGetStructuredData() {
            Content entry = ContentFixtures.published(1L, ContentType.FAQ, QUESTION_SLUG, QUESTION);
            entry.setParentId(1L);
            Content draft = ContentFixtures.content(2L, ContentType.FAQ, "q2");
            draft.setParentId(1L);
            Content orphan = ContentFixtures.published(3L, ContentType.FAQ, "q3", "Where?");
            orphan.setParentId(99L);
            when(faq.byIds(ContentFixtures.STORE)).thenReturn(new java.util.LinkedHashMap<>(Map.of(
                    1L, faqGroup(1L, GENERAL_KEY, Map.of(EN, GENERAL_NAME)))));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.FAQ))
                    .thenReturn(List.of(entry, draft, orphan));

            StorefrontFaq out = facade.faq(ContentFixtures.STORE, ContentFixtures.EN, null);

            assertThat(out.getServedLocale()).isEqualTo(EN);
            assertThat(out.getGroups()).singleElement().satisfies(g -> {
                assertThat(g.getName()).isEqualTo(GENERAL_NAME);
                assertThat(g.getEntries()).extracting(StorefrontFaq.Entry::getQuestion).containsExactly(QUESTION);
            });
            assertThat(out.getJsonLd()).contains("FAQPage").contains(QUESTION);
        }

        @Test
        void askingForOneGroupExcludesTheOthers() {
            Content entry = ContentFixtures.published(1L, ContentType.FAQ, QUESTION_SLUG, QUESTION);
            entry.setParentId(1L);
            when(faq.byIds(ContentFixtures.STORE)).thenReturn(new java.util.LinkedHashMap<>(Map.of(
                    1L, faqGroup(1L, GENERAL_KEY, Map.of(EN, GENERAL_NAME)))));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.FAQ)).thenReturn(List.of(entry));

            assertThat(facade.faq(ContentFixtures.STORE, ContentFixtures.EN, "shipping").getGroups()).isEmpty();
            assertThat(facade.faq(ContentFixtures.STORE, null, GENERAL_KEY).getGroups()).hasSize(1);
        }

    }

    @Nested
    class Policies {

        private Content head() {
            Content c = ContentFixtures.published(1L, ContentType.POLICY, TERMS_SLUG, TERMS_TITLE);
            c.setPolicyType(PolicyType.TERMS);
            return c;
        }

        private PolicyVersion version(int number) {
            PolicyVersion v = new PolicyVersion();
            v.setVersion(number);
            v.setStatus(PolicyVersionStatus.LIVE);
            v.setEffectiveFrom(ContentFixtures.NOW);
            v.setTranslations(JsonCodec.write(List.of(
                    ContentFixtures.translation(ContentFixtures.EN, TERMS_TITLE, BODY))));
            return v;
        }

        @Test
        void thePolicyIsServedFromItsLiveVersion() throws Exception {
            Content c = head();
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of(c));
            when(policies.live(c)).thenReturn(Optional.of(version(2)));

            StorefrontPolicy out = facade.policy(ContentFixtures.STORE, ContentFixtures.EN, PolicyType.TERMS, null);

            assertThat(out.getVersion()).isEqualTo(2);
            assertThat(out.getHeading()).isEqualTo(TERMS_TITLE);
            assertThat(out.getBody()).isEqualTo(BODY);
            assertThat(out.getServedLocale()).isEqualTo(EN);
            assertThat(out.isRequiresAcceptance()).isFalse();
        }

        @Test
        void anExplicitVersionNumberIsHonoured() throws Exception {
            Content c = head();
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of(c));
            when(policies.versionEntity(c, 1)).thenReturn(Optional.of(version(1)));

            assertThat(facade.policy(ContentFixtures.STORE, ContentFixtures.EN, PolicyType.TERMS, 1).getVersion())
                    .isEqualTo(1);
        }

        @Test
        void aPolicyWithoutAHeadOrWithoutAVersionReadsAsMissing() {
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of());

            assertThatThrownBy(() -> facade.policy(ContentFixtures.STORE, ContentFixtures.EN, PolicyType.TERMS,
                    null)).isInstanceOf(ContentNotFoundException.class);

            Content c = head();
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of(c));
            when(policies.live(c)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> facade.policy(ContentFixtures.STORE, ContentFixtures.EN, PolicyType.TERMS,
                    null)).isInstanceOf(ContentNotFoundException.class);
        }

        @Test
        void aVersionWithNoTextAtAllReadsAsMissing() {
            Content c = head();
            PolicyVersion empty = version(1);
            empty.setTranslations(null);
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of(c));
            when(policies.live(c)).thenReturn(Optional.of(empty));

            assertThatThrownBy(() -> facade.policy(ContentFixtures.STORE, ContentFixtures.EN, PolicyType.TERMS,
                    null)).isInstanceOf(ContentNotFoundException.class);
        }

    }

    @Nested
    class SitemapAndRedirects {

        @Test
        void theSitemapSkipsNoindexRowsAndPoliciesWithoutALiveVersion() {
            Content page = ContentFixtures.published(1L, ContentType.PAGE, ABOUT, ABOUT_TITLE);
            Content hidden = ContentFixtures.published(2L, ContentType.PAGE, "secret", "Secret");
            hidden.setNoindex(true);
            Content post = ContentFixtures.published(3L, ContentType.POST, HELLO, HELLO_TITLE);
            Content policyHead = ContentFixtures.published(4L, ContentType.POLICY, TERMS_SLUG, TERMS_TITLE);
            policyHead.setPolicyType(PolicyType.TERMS);
            Content faqEntry = ContentFixtures.published(5L, ContentType.FAQ, QUESTION_SLUG, QUESTION);
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.PAGE))
                    .thenReturn(List.of(page, hidden));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POST)).thenReturn(List.of(post));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.POLICY))
                    .thenReturn(List.of(policyHead));
            when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.FAQ)).thenReturn(List.of(faqEntry));
            when(policies.live(policyHead)).thenReturn(Optional.of(new PolicyVersion()));

            List<SitemapEntry> out = facade.sitemap(ContentFixtures.STORE, ContentFixtures.EN);

            assertThat(out).extracting(SitemapEntry::getLoc)
                    .containsExactly(ABOUT_PATH, "/blog/hello-world", TERMS_PATH, "/help");
        }

        @Test
        void aStoreWithNoFaqHasNoHelpEntry() {
            when(contents.findVisibleByType(any(), any())).thenReturn(List.of());

            assertThat(facade.sitemap(ContentFixtures.STORE, ContentFixtures.EN)).isEmpty();
        }

        @Test
        void aRedirectIsDelegatedToTheRedirectService() {
            when(redirects.resolve(ContentFixtures.STORE, OLD_PATH)).thenReturn(Optional.of(MOVED_PATH));

            assertThat(facade.redirect(ContentFixtures.STORE, OLD_PATH)).contains(MOVED_PATH);
        }

    }

}
