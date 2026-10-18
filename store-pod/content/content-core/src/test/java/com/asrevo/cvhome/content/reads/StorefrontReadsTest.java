package com.asrevo.cvhome.content.reads;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.facade.StorefrontFacade;
import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.MenuHandle;
import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.layout.PageKind;
import com.asrevo.cvhome.content.service.MenuService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/** Every read hands its arguments to the published shape of the facade, and is declared on a content region. */
@ExtendWith(MockitoExtension.class)
class StorefrontReadsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final Instant NOW = Instant.parse("2026-09-15T00:00:00Z");

    private static final String SLUG = "about";

    private static final String NEWS = "news";

    private static final Pageable PAGE = PageRequest.of(0, 10);

    @Mock
    private StorefrontFacade storefront;

    @Mock
    private MenuService menus;

    @Spy
    private Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @InjectMocks
    private StorefrontReads reads;

    @Test
    void everyReadAsksForThePublishedShape() throws Exception {
        reads.site(STORE, EN);
        reads.layout(STORE, EN, PageKind.HOME);
        reads.menu(STORE, EN, MenuHandle.MAIN);
        reads.page(STORE, EN, SLUG);
        reads.post(STORE, EN, SLUG);
        reads.posts(STORE, EN, PostsFilter.of(NEWS, null), PAGE);
        reads.postCategories(STORE, EN);
        reads.banners(STORE, EN, null);
        reads.faq(STORE, EN, null);
        reads.policy(STORE, EN, PolicyType.values()[0]);
        reads.sitemap(STORE, EN);

        verify(storefront).site(STORE, EN);
        verify(storefront).layout(STORE, EN, PageKind.HOME, false);
        verify(menus).resolved(STORE, MenuHandle.MAIN, EN, NOW);
        verify(storefront).page(STORE, EN, SLUG, false);
        verify(storefront).post(STORE, EN, SLUG, false);
        verify(storefront).posts(STORE, EN, NEWS, null, PAGE);
        verify(storefront).postCategories(STORE, EN);
        verify(storefront).effectiveBanners(STORE, EN, (BannerPlacement) null);
        verify(storefront).faq(STORE, EN, null);
        verify(storefront).policy(STORE, EN, PolicyType.values()[0], null);
        verify(storefront).sitemap(STORE, EN);
    }

    @Test
    void everyReadIsDeclaredOnAContentRegion() {
        List<String> names = List.of(ContentRegions.values()).stream().map(ContentRegions::regionName).toList();
        for (Method method : StorefrontReads.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }
            Cacheable cacheable = AnnotatedElementUtils.findMergedAnnotation(method, Cacheable.class);
            assertThat(cacheable).as(method.getName()).isNotNull();
            assertThat(names).contains(cacheable.cacheNames());
        }
    }
}
