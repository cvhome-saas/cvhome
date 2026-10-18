package com.asrevo.cvhome.content.entity;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.StoreScoped;

import static org.assertj.core.api.Assertions.assertThat;

/** Every content entity with an eviction rule names its store; a description or a menu item through its owner. */
class StoreScopedEntitiesTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    @Test
    void ownersNameTheirStoreAndPartsAnswerThroughTheirOwner() {
        Content content = new Content();
        content.setStoreMerchantId(STORE);
        ContentDescription description = new ContentDescription();
        description.setContent(content);
        Menu menu = new Menu();
        menu.setStoreMerchantId(STORE.getId());
        MenuItem item = new MenuItem();
        item.setMenu(menu);
        ContentRevision revision = new ContentRevision();
        revision.setStoreMerchantId(STORE.getId());
        ContentStatusAudit audit = new ContentStatusAudit();
        audit.setStoreMerchantId(STORE.getId());
        PageLayout layout = new PageLayout();
        layout.setStoreMerchantId(STORE.getId());
        PolicyVersion policy = new PolicyVersion();
        policy.setStoreMerchantId(STORE.getId());
        FaqGroup faq = new FaqGroup();
        faq.setStoreMerchantId(STORE.getId());
        MediaAsset asset = new MediaAsset();
        asset.setStoreMerchantId(STORE.getId());
        MediaFolder folder = new MediaFolder();
        folder.setStoreMerchantId(STORE.getId());
        MediaQuota quota = new MediaQuota();
        quota.setStoreMerchantId(STORE.getId());
        PostCategory category = new PostCategory();
        category.setStoreMerchantId(STORE.getId());
        Redirect redirect = new Redirect();
        redirect.setStoreMerchantId(STORE.getId());
        SectionPreset preset = new SectionPreset();
        preset.setStoreMerchantId(STORE.getId());
        SiteSettings settings = new SiteSettings();
        settings.setStoreMerchantId(STORE.getId());

        StoreScoped[] all = {content, description, menu, item, revision, audit, layout, policy, faq, asset, folder,
            quota, category, redirect, preset, settings};
        for (StoreScoped entity : all) {
            assertThat(entity.scopedStore()).as(entity.getClass().getSimpleName()).isEqualTo(STORE);
        }
        StoreScoped[] orphans = {new ContentDescription(), new MenuItem(), new Menu(), new Redirect(), new Content()};
        for (StoreScoped orphan : orphans) {
            assertThat(orphan.scopedStore()).as(orphan.getClass().getSimpleName()).isNull();
        }
    }
}
