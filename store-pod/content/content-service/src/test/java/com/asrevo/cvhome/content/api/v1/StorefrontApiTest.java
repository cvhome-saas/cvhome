package com.asrevo.cvhome.content.api.v1;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.PreviewTokens;
import com.asrevo.cvhome.content.facade.StorefrontFacade;
import com.asrevo.cvhome.content.model.layout.PageKind;
import com.asrevo.cvhome.content.service.MenuService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The shopper-facing reads, and the caching decision that hangs off a preview token.
 *
 * <p>
 * Everything here is public and cached for a minute at the edge, which is what makes the preview path dangerous:
 * a draft served with the same cache headers as a published page would be cached and then served to shoppers who
 * hold no preview token. So a valid token flips the response to {@code no-store} <em>and</em> asks the facade for
 * the draft; an absent or invalid one does neither. Both halves have to move together, which is why they are
 * asserted together.
 * </p>
 */
class StorefrontApiTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final String SLUG = "about-us";
    private static final String TOKEN = "preview-token";
    private static final String NO_STORE = "no-store";
    private static final String PATH = "/old-path";
    private static final String NEW_PATH = "/new-path";
    private static final String MAX_AGE = "max-age=60";
    private static final String FORGED = "forged";

    private final StorefrontFacade storefront = Mockito.mock(StorefrontFacade.class);
    private final MenuService menus = Mockito.mock(MenuService.class);
    private final PreviewTokens previews = Mockito.mock(PreviewTokens.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private final StorefrontApi api = new StorefrontApi(storefront, menus, previews, clock);

    @Test
    void aPublishedPageIsCachedAtTheEdge() throws Exception {
        when(previews.valid(null, STORE, SLUG)).thenReturn(false);

        ResponseEntity<?> response = api.page(STORE, ENGLISH, SLUG, null);

        assertThat(response.getHeaders().getCacheControl()).contains(MAX_AGE).doesNotContain(NO_STORE);
        verify(storefront).page(STORE, ENGLISH, SLUG, false);
    }

    @Test
    void aValidPreviewTokenAsksForTheDraftAndForbidsCachingIt() throws Exception {
        when(previews.valid(TOKEN, STORE, SLUG)).thenReturn(true);

        ResponseEntity<?> response = api.page(STORE, ENGLISH, SLUG, TOKEN);

        // Both halves move together: a draft cached at the edge would be served to shoppers with no token.
        assertThat(response.getHeaders().getCacheControl()).contains(NO_STORE);
        verify(storefront).page(STORE, ENGLISH, SLUG, true);
    }

    @Test
    void thePostPreviewFollowsTheSameRule() throws Exception {
        when(previews.valid(TOKEN, STORE, SLUG)).thenReturn(true);
        when(previews.valid(null, STORE, SLUG)).thenReturn(false);

        assertThat(api.post(STORE, ENGLISH, SLUG, TOKEN).getHeaders().getCacheControl()).contains(NO_STORE);
        assertThat(api.post(STORE, ENGLISH, SLUG, null).getHeaders().getCacheControl()).doesNotContain(NO_STORE);

        verify(storefront).post(STORE, ENGLISH, SLUG, true);
        verify(storefront).post(STORE, ENGLISH, SLUG, false);
    }

    @Test
    void anInvalidPreviewTokenIsTreatedAsNoTokenAtAll() throws Exception {
        when(previews.valid(FORGED, STORE, SLUG)).thenReturn(false);

        ResponseEntity<?> response = api.page(STORE, ENGLISH, SLUG, FORGED);

        assertThat(response.getHeaders().getCacheControl()).doesNotContain(NO_STORE);
        verify(storefront).page(STORE, ENGLISH, SLUG, false);
    }

    @Test
    void theCachedReadsAllCarryTheSameEdgePolicy() {
        api.site(STORE, ENGLISH);
        api.posts(STORE, ENGLISH, null, null, PageRequest.of(0, 20));
        api.postCategories(STORE, ENGLISH);
        api.banners(STORE, ENGLISH, null);
        api.faq(STORE, ENGLISH, null);
        api.sitemap(STORE, ENGLISH);

        assertThat(api.site(STORE, ENGLISH).getHeaders().getCacheControl())
                .contains(MAX_AGE, "public", "stale-while-revalidate=60");
        verify(storefront).posts(STORE, ENGLISH, null, null, PageRequest.of(0, 20));
        verify(storefront).postCategories(STORE, ENGLISH);
        verify(storefront).effectiveBanners(STORE, ENGLISH, null);
        verify(storefront).faq(STORE, ENGLISH, null);
        verify(storefront).sitemap(STORE, ENGLISH);
    }

    @Test
    void aMenuIsResolvedAgainstTheClockSoScheduledItemsAppearOnTime() {
        api.menu(STORE, ENGLISH, null);

        verify(menus).resolved(eq(STORE), eq(null), eq(ENGLISH), eq(clock.instant()));
    }

    @Test
    void aKnownRedirectAnswersWithBothEnds() {
        when(storefront.redirect(STORE, PATH)).thenReturn(Optional.of(NEW_PATH));

        ResponseEntity<Map<String, String>> response = api.redirect(STORE, ENGLISH, PATH);

        assertThat(response.getBody()).containsEntry("from", PATH).containsEntry("to", NEW_PATH);
        assertThat(response.getHeaders().getCacheControl()).contains(MAX_AGE);
    }

    @Test
    void anUnknownRedirectIsAFourOhFourRatherThanAnEmptyBody() {
        when(storefront.redirect(STORE, PATH)).thenReturn(Optional.empty());

        assertThat(api.redirect(STORE, ENGLISH, PATH).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void aPolicyReadPassesItsRequestedVersionThrough() throws Exception {
        api.policy(STORE, ENGLISH, null, 3);

        verify(storefront).policy(STORE, ENGLISH, null, 3);
    }

    @Test
    void theLayoutPreviewIsKeyedByTheLayoutsOwnSlugNotAPageSlug() {
        // A layout has no slug of its own, so the preview token is issued against "layout:<kind>"; asking the token
        // store for the page slug instead would let a page's token unlock a layout draft.
        when(previews.valid(TOKEN, STORE, LayoutApi.previewSlug(PageKind.HOME))).thenReturn(true);

        ResponseEntity<?> response = api.layout(STORE, ENGLISH, PageKind.HOME, TOKEN);

        assertThat(response.getHeaders().getCacheControl()).contains(NO_STORE);
        verify(storefront).layout(STORE, ENGLISH, PageKind.HOME, true);
    }

    @Test
    void aLayoutWithoutAValidTokenIsTheCachedPublishedOne() {
        when(previews.valid(any(), eq(STORE), any())).thenReturn(false);

        ResponseEntity<?> response = api.layout(STORE, ENGLISH, PageKind.HOME, null);

        assertThat(response.getHeaders().getCacheControl()).doesNotContain(NO_STORE);
        verify(storefront).layout(STORE, ENGLISH, PageKind.HOME, false);
    }
}
