package com.asrevo.cvhome.content.facade;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.MenuHandle;
import com.asrevo.cvhome.content.model.layout.PageKind;
import com.asrevo.cvhome.content.service.RedirectService;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The storefront's site, layout and menus come from the cache on a second read, and any content write clears it.
 *
 * <p>
 * Each was a read-write transaction on every page view in the 2026-09-14 load test, the site alone 12 statements. A
 * write has to clear them, or an editor who publishes and looks would see the old copy.
 * </p>
 */
@StorageIntegrationTest
class CachedStorefrontIntegrationTest {

    /** A seeded store (languages en, fr). */
    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final LanguageCode EN = new LanguageCode("en");

    @Autowired
    private CachedStorefront storefront;

    @Autowired
    private RedirectService redirects;

    @Test
    void aSecondReadCostsNoStatementAndAContentWriteClearsTheCache() throws Exception {
        storefront.site(STORE, EN);
        storefront.layout(STORE, EN, PageKind.HOME);
        storefront.menu(STORE, MenuHandle.MAIN, EN);

        SqlStatements.Recorded<Object> cached = SqlStatements.during(() -> {
            storefront.site(STORE, EN);
            storefront.layout(STORE, EN, PageKind.HOME);
            return storefront.menu(STORE, MenuHandle.MAIN, EN);
        });
        assertThat(cached.count()).as(cached.toString()).isZero();

        String slug = UUID.randomUUID().toString();
        redirects.moved(STORE, String.format("/content/%s", slug), String.format("/content/%s-moved", slug));

        SqlStatements.Recorded<Object> afterWrite = SqlStatements.during(() -> storefront.site(STORE, EN));
        assertThat(afterWrite.count()).isPositive();
    }
}
