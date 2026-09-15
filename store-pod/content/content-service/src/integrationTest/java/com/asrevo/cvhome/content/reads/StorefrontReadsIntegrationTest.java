package com.asrevo.cvhome.content.reads;

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
 * The storefront reads on a real content database: a second read costs no statement, an editor's committed write
 * (a redirect, which the sitemap lists) drops their own store's entries and leaves the other store's warm.
 */
@StorageIntegrationTest
class StorefrontReadsIntegrationTest {

    /** A seeded store (languages en, fr). */
    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    /** The other seeded store. */
    private static final StoreMerchantId OTHER_STORE = new StoreMerchantId("65f020632bc46470c104b76f");

    private static final LanguageCode EN = new LanguageCode("en");

    @Autowired
    private StorefrontReads reads;

    @Autowired
    private RedirectService redirects;

    @Test
    void aSecondReadCostsNoStatementAndAnEditorsWriteDropsTheirStoresEntriesAlone() throws Exception {
        reads.site(STORE, EN);
        reads.layout(STORE, EN, PageKind.HOME);
        reads.menu(STORE, EN, MenuHandle.MAIN);
        reads.sitemap(STORE, EN);
        reads.sitemap(OTHER_STORE, EN);

        SqlStatements.Recorded<Object> cached = SqlStatements.during(() -> {
            reads.site(STORE, EN);
            reads.layout(STORE, EN, PageKind.HOME);
            reads.menu(STORE, EN, MenuHandle.MAIN);
            return reads.sitemap(STORE, EN);
        });
        assertThat(cached.count()).as(cached.toString()).isZero();

        String slug = UUID.randomUUID().toString();
        redirects.moved(STORE, String.format("/content/%s", slug), String.format("/content/%s-moved", slug));

        SqlStatements.Recorded<Object> afterWrite = SqlStatements.during(() -> reads.sitemap(STORE, EN));
        assertThat(afterWrite.count()).as("the store that wrote reads the database again").isPositive();
        SqlStatements.Recorded<Object> otherStore = SqlStatements.during(() -> reads.sitemap(OTHER_STORE, EN));
        assertThat(otherStore.count()).as("another store's entries stayed warm").isZero();
        SqlStatements.Recorded<Object> untouched = SqlStatements.during(() -> reads.site(STORE, EN));
        assertThat(untouched.count()).as("a redirect does not stale the site").isZero();
    }
}
