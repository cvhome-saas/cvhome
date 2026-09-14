package com.asrevo.cvhome.content.entity;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Which store a content entity belongs to, for the cache eviction that follows its write. Most rows carry the store
 * id as a column; a description answers through its content, a menu item through its menu. A layout revision and a
 * media usage row carry none, and a write to them alone clears every store's cache rather than risk a stale one.
 */
public final class ContentEntityStore {

    private ContentEntityStore() {
    }

    public static StoreMerchantId of(Object entity) {
        return switch (entity) {
            case Content content -> content.getStoreMerchantId();
            case ContentDescription description -> of(description.getContent());
            case ContentRevision revision -> id(revision.getStoreMerchantId());
            case ContentStatusAudit audit -> id(audit.getStoreMerchantId());
            case Menu menu -> id(menu.getStoreMerchantId());
            case MenuItem item -> of(item.getMenu());
            case PageLayout layout -> id(layout.getStoreMerchantId());
            case PolicyVersion policy -> id(policy.getStoreMerchantId());
            case null, default -> ofSiteEntity(entity);
        };
    }

    private static StoreMerchantId ofSiteEntity(Object entity) {
        return switch (entity) {
            case FaqGroup group -> id(group.getStoreMerchantId());
            case MediaAsset asset -> id(asset.getStoreMerchantId());
            case MediaFolder folder -> id(folder.getStoreMerchantId());
            case MediaQuota quota -> id(quota.getStoreMerchantId());
            case PostCategory category -> id(category.getStoreMerchantId());
            case Redirect redirect -> id(redirect.getStoreMerchantId());
            case SectionPreset preset -> id(preset.getStoreMerchantId());
            case SiteSettings settings -> id(settings.getStoreMerchantId());
            case null, default -> null;
        };
    }

    private static StoreMerchantId id(String storeMerchantId) {
        return storeMerchantId == null ? null : new StoreMerchantId(storeMerchantId);
    }
}
