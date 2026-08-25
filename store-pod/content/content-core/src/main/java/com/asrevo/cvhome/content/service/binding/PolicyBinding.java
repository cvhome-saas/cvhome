package com.asrevo.cvhome.content.service.binding;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.model.policy.PersistablePolicy;
import com.asrevo.cvhome.content.model.policy.PolicyMeta;
import com.asrevo.cvhome.content.model.policy.ReadablePolicy;
import com.asrevo.cvhome.content.service.ContentMapper;
import com.asrevo.cvhome.content.service.ContentTypeBinding;
import com.asrevo.cvhome.content.service.PolicyService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * Legal policies: heading = title, body per locale on the head; type/effective date as columns; jurisdiction,
 * acceptance and display flags in {@code meta}. One head per type per store.
 */
@Component
@RequiredArgsConstructor
public class PolicyBinding implements ContentTypeBinding<PersistablePolicy, ReadablePolicy> {

    private final PolicyService policies;

    @Override
    public ContentType type() {
        return ContentType.POLICY;
    }

    @Override
    public Class<PersistablePolicy> persistableClass() {
        return PersistablePolicy.class;
    }

    @Override
    public ReadablePolicy newReadable() {
        return new ReadablePolicy();
    }

    @Override
    public void apply(Content entity, PersistablePolicy dto) throws ContentConflictException {
        policies.assertTypeFree(entity.getStoreMerchantId(), dto.getPolicyType(), entity.getId());
        entity.setPolicyType(dto.getPolicyType());
        entity.setStartsAt(dto.getEffectiveFrom());
        entity.setShowInFooter(dto.isShowInFooter());
        entity.setMeta(JsonCodec.write(new PolicyMeta(dto.getJurisdiction(), dto.isRequiresAcceptance(),
                dto.isNotifyCustomers(),
                new PolicyMeta.DisplayAt(dto.isShowInFooter(), dto.isShowAtCheckout(), dto.isShowAtSignup()))));
    }

    @Override
    public void populate(Content entity, ReadablePolicy dto) {
        PolicyMeta meta = meta(entity);
        dto.setPolicyType(entity.getPolicyType());
        dto.setEffectiveFrom(entity.getStartsAt());
        dto.setJurisdiction(meta.jurisdiction());
        dto.setRequiresAcceptance(meta.requiresAcceptance());
        dto.setNotifyCustomers(meta.notifyCustomers());
        dto.setShowInFooter(meta.displayAt() == null || meta.displayAt().footer());
        dto.setShowAtCheckout(meta.displayAt() != null && meta.displayAt().checkout());
        dto.setShowAtSignup(meta.displayAt() != null && meta.displayAt().signup());
        dto.setStatus(entity.getStatus());
        dto.setLocales(ContentMapper.locales(entity));
        dto.setAudit(ContentMapper.audit(entity));
        dto.setLiveVersion(policies.liveVersion(entity));
        dto.setVersions(policies.versions(entity));
    }

    @Override
    public String subtitle(Content entity, LanguageCode language) {
        String type = entity.getPolicyType() == null ? "policy" : entity.getPolicyType().name().toLowerCase();
        int live = policies.liveVersion(entity);
        // The type and version number carry the meaning; an English "unpublished" would sit untranslated in
        // an Arabic console, so an unpublished policy shows the type alone.
        return live == 0 ? type : String.format("%s · v%d", type, live);
    }

    @Override
    public String storefrontPath(Content entity) {
        return String.format("/policies/%s", entity.getCode());
    }

    /**
     * Publishing the head cuts a new immutable version whenever its text differs from the live one, so "Publish
     * policy" and "new version" are the same gesture.
     */
    @Override
    public void afterSave(Content entity) {
        if (entity.getStatus() != com.asrevo.cvhome.content.model.ContentStatus.PUBLISHED) {
            return;
        }
        var live = policies.live(entity);
        var current = ContentMapper.translations(entity);
        if (live.isEmpty() || !sameText(PolicyService.translations(live.get()), current)) {
            var request = new com.asrevo.cvhome.content.model.policy.PublishPolicyVersionRequest();
            request.setEffectiveFrom(entity.getStartsAt());
            policies.publishVersion(entity, request, entity.getUpdatedBy());
        }
    }

    @Override
    public void afterDelete(Content entity) {
        policies.forget(entity);
    }

    static boolean sameText(java.util.List<com.asrevo.cvhome.content.model.common.ContentTranslation> a,
                            java.util.List<com.asrevo.cvhome.content.model.common.ContentTranslation> b) {
        if (a.size() != b.size()) {
            return false;
        }
        java.util.Map<String, String> left = new java.util.HashMap<>();
        for (var t : a) {
            left.put(t.getLanguage().code(), textKey(t));
        }
        for (var t : b) {
            if (!java.util.Objects.equals(left.get(t.getLanguage().code()), textKey(t))) {
                return false;
            }
        }
        return true;
    }

    private static String textKey(com.asrevo.cvhome.content.model.common.ContentTranslation t) {
        return String.format("%s\u0000%s", t.getTitle(), t.getBody());
    }

    public static PolicyMeta meta(Content entity) {
        PolicyMeta m = JsonCodec.read(entity.getMeta(), PolicyMeta.class);
        return m != null ? m : new PolicyMeta(null, false, false, new PolicyMeta.DisplayAt(true, false, false));
    }

}
