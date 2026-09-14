package com.asrevo.cvhome.content.entity;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every content entity that carries a store names it, a description through its content and a menu item through its
 * menu, so a write drops only that store's cached reads; the rows that carry none clear every store.
 */
class ContentEntityStoreTest {

    private static final String STORE_ID = "65f023632bc46470c104b76f";

    private static final StoreMerchantId STORE = new StoreMerchantId(STORE_ID);

    @Test
    void theEntitiesThatCarryTheStoreNameIt() {
        Content content = new Content();
        content.setStoreMerchantId(STORE);
        ContentDescription description = new ContentDescription();
        description.setContent(content);
        Menu menu = new Menu();
        menu.setStoreMerchantId(STORE_ID);
        MenuItem item = new MenuItem();
        item.setMenu(menu);
        ContentRevision revision = new ContentRevision();
        revision.setStoreMerchantId(STORE_ID);
        ContentStatusAudit audit = new ContentStatusAudit();
        audit.setStoreMerchantId(STORE_ID);
        PageLayout layout = new PageLayout();
        layout.setStoreMerchantId(STORE_ID);
        PolicyVersion policy = new PolicyVersion();
        policy.setStoreMerchantId(STORE_ID);

        assertThat(ContentEntityStore.of(content)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(description)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(menu)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(item)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(revision)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(audit)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(layout)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(policy)).isEqualTo(STORE);
    }

    @Test
    void theSiteEntitiesNameItToo() {
        FaqGroup faq = new FaqGroup();
        faq.setStoreMerchantId(STORE_ID);
        MediaAsset asset = new MediaAsset();
        asset.setStoreMerchantId(STORE_ID);
        MediaFolder folder = new MediaFolder();
        folder.setStoreMerchantId(STORE_ID);
        MediaQuota quota = new MediaQuota();
        quota.setStoreMerchantId(STORE_ID);
        PostCategory category = new PostCategory();
        category.setStoreMerchantId(STORE_ID);
        Redirect redirect = new Redirect();
        redirect.setStoreMerchantId(STORE_ID);
        SectionPreset preset = new SectionPreset();
        preset.setStoreMerchantId(STORE_ID);
        SiteSettings settings = new SiteSettings();
        settings.setStoreMerchantId(STORE_ID);

        assertThat(ContentEntityStore.of(faq)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(asset)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(folder)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(quota)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(category)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(redirect)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(preset)).isEqualTo(STORE);
        assertThat(ContentEntityStore.of(settings)).isEqualTo(STORE);
    }

    @Test
    void theRowsWithoutAStoreClearEveryStore() {
        assertThat(ContentEntityStore.of(new PageLayoutRevision())).isNull();
        assertThat(ContentEntityStore.of(new MediaUsageRow())).isNull();
        assertThat(ContentEntityStore.of(new Redirect())).as("a store column left null").isNull();
        assertThat(ContentEntityStore.of(null)).isNull();
    }
}
