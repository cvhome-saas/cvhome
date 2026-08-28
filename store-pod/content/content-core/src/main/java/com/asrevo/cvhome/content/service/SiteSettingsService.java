package com.asrevo.cvhome.content.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.MediaAsset;
import com.asrevo.cvhome.content.entity.SiteSettings;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.model.site.MediaRef;
import com.asrevo.cvhome.content.model.site.PersistableSiteSettings;
import com.asrevo.cvhome.content.model.site.ReadableSiteSettings;
import com.asrevo.cvhome.content.model.site.SiteBranding;
import com.asrevo.cvhome.content.repository.MediaAssetRepository;
import com.asrevo.cvhome.content.repository.SiteSettingsRepository;
import com.asrevo.cvhome.content.storage.MediaStorage;
import com.asrevo.cvhome.content.support.JsonCodec;

import lombok.RequiredArgsConstructor;

/**
 * The store's appearance record: brand imagery, social links and site-level SEO.
 *
 * <p>
 * The row is created on first read rather than at provisioning time, the same way the media quota row is, so a
 * store that predates this feature needs no backfill.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SiteSettingsService {

    /** The field names used in the media usage index, so a console can say which slot holds an asset. */
    static final String LOGO = "branding.logo";

    static final String LOGO_DARK = "branding.logoDark";

    static final String FAVICON = "branding.favicon";

    static final String OG = "branding.og";

    private static final String SITE_TITLE = "Store appearance";

    private final SiteSettingsRepository repository;

    private final MediaAssetRepository assets;

    private final MediaUsageTracker usageTracker;

    private final MediaStorage storage;

    private final Clock clock;

    @Transactional
    public SiteSettings entity(StoreMerchantId store) {
        return repository.findById(store.getId()).orElseGet(() -> {
            SiteSettings created = new SiteSettings();
            created.setStoreMerchantId(store.getId());
            return repository.save(created);
        });
    }

    @Transactional
    public ReadableSiteSettings get(StoreMerchantId store, LanguageCode language) {
        return toReadable(entity(store), language);
    }

    /**
     * Replaces the whole record. A {@code null} media slot clears it — which is how a logo becomes removable;
     * merchant, which used to own these, only ever had upload endpoints.
     *
     * @throws ContentNotFoundException a referenced asset is not in this store's library
     */
    @Transactional(rollbackFor = Exception.class)
    public ReadableSiteSettings put(StoreMerchantId store, PersistableSiteSettings body, LanguageCode language,
                                    String actor) throws ContentNotFoundException {
        SiteSettings entity = entity(store);
        Map<String, Long> refs = references(body);
        requireOwned(store, refs.values());

        entity.setLogoMediaId(body.getLogoMediaId());
        entity.setLogoDarkMediaId(body.getLogoDarkMediaId());
        entity.setFaviconMediaId(body.getFaviconMediaId());
        entity.setOgMediaId(body.getOgMediaId());
        entity.setSeo(JsonCodec.write(body.getSeo() == null ? Map.of() : body.getSeo()));
        entity.setSocialLinks(JsonCodec.write(body.getSocialLinks() == null ? List.of() : body.getSocialLinks()));
        entity.setUpdatedAt(clock.instant());
        entity.setUpdatedBy(actor);
        SiteSettings saved = repository.save(entity);

        usageTracker.replace(store, MediaOwnerKind.SITE_SETTINGS, store.getId(), null, null, SITE_TITLE, refs);
        return toReadable(saved, language);
    }

    /**
     * The branding as the storefront needs it — every slot resolved to a URL in one query.
     */
    @Transactional(readOnly = true)
    public SiteBranding branding(StoreMerchantId store, LanguageCode language) {
        return branding(entity(store), language);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Map<String, String>> seo(SiteSettings entity) {
        Map<String, Map<String, String>> seo = JsonCodec.read(entity.getSeo(), Map.class);
        return seo == null ? Map.of() : seo;
    }

    /**
     * One SEO field in the requested locale, falling back to whatever single value exists when the store has not
     * translated it.
     */
    public String seoValue(SiteSettings entity, String field, LanguageCode language) {
        Map<String, String> byLocale = seo(entity).get(field);
        if (byLocale == null || byLocale.isEmpty()) {
            return null;
        }
        String exact = language == null ? null : byLocale.get(language.code());
        if (exact != null && !exact.isBlank()) {
            return exact;
        }
        return byLocale.values().stream().filter(v -> v != null && !v.isBlank()).findFirst().orElse(null);
    }

    /**
     * Read as an array, not a {@code List}: a generic list comes back as maps, and the cast to
     * {@code List<SocialLink>} then only fails later, when the response is serialised.
     */
    public List<SocialLink> socialLinks(SiteSettings entity) {
        SocialLink[] links = JsonCodec.read(entity.getSocialLinks(), SocialLink[].class);
        return links == null ? List.of() : List.of(links);
    }

    public SiteBranding branding(SiteSettings entity, LanguageCode language) {
        Map<Long, MediaAsset> byId = new LinkedHashMap<>();
        List<Long> wanted = references(entity).values().stream().filter(java.util.Objects::nonNull).distinct()
                .toList();
        if (!wanted.isEmpty()) {
            assets.findByStoreMerchantIdAndIdIn(entity.getStoreMerchantId(), wanted)
                    .forEach(a -> byId.put(a.getId(), a));
        }
        return new SiteBranding(ref(byId.get(entity.getLogoMediaId()), language),
                ref(byId.get(entity.getLogoDarkMediaId()), language),
                ref(byId.get(entity.getFaviconMediaId()), language),
                ref(byId.get(entity.getOgMediaId()), language));
    }

    @SuppressWarnings("unchecked")
    private MediaRef ref(MediaAsset a, LanguageCode language) {
        if (a == null) {
            return null;
        }
        String alt = null;
        Map<String, String> alts = JsonCodec.read(a.getAltTexts(), Map.class);
        if (alts != null && !alts.isEmpty()) {
            alt = language == null ? null : alts.get(language.code());
            if (alt == null) {
                alt = alts.values().stream().filter(v -> v != null && !v.isBlank()).findFirst().orElse(null);
            }
        }
        return new MediaRef(a.getId(), storage.url(a.getStorageKey()), alt, a.getWidth(), a.getHeight());
    }

    private ReadableSiteSettings toReadable(SiteSettings entity, LanguageCode language) {
        ReadableSiteSettings r = new ReadableSiteSettings();
        r.setLogoMediaId(entity.getLogoMediaId());
        r.setLogoDarkMediaId(entity.getLogoDarkMediaId());
        r.setFaviconMediaId(entity.getFaviconMediaId());
        r.setOgMediaId(entity.getOgMediaId());
        r.setSeo(new LinkedHashMap<>(seo(entity)));
        r.setSocialLinks(socialLinks(entity));
        r.setBranding(branding(entity, language));
        r.setUpdatedAt(entity.getUpdatedAt());
        r.setUpdatedBy(entity.getUpdatedBy());
        return r;
    }

    private static Map<String, Long> references(PersistableSiteSettings body) {
        Map<String, Long> refs = new LinkedHashMap<>();
        put(refs, LOGO, body.getLogoMediaId());
        put(refs, LOGO_DARK, body.getLogoDarkMediaId());
        put(refs, FAVICON, body.getFaviconMediaId());
        put(refs, OG, body.getOgMediaId());
        return refs;
    }

    private static Map<String, Long> references(SiteSettings entity) {
        Map<String, Long> refs = new LinkedHashMap<>();
        put(refs, LOGO, entity.getLogoMediaId());
        put(refs, LOGO_DARK, entity.getLogoDarkMediaId());
        put(refs, FAVICON, entity.getFaviconMediaId());
        put(refs, OG, entity.getOgMediaId());
        return refs;
    }

    private static void put(Map<String, Long> refs, String field, Long id) {
        if (id != null) {
            refs.put(field, id);
        }
    }

    private void requireOwned(StoreMerchantId store, java.util.Collection<Long> ids)
            throws ContentNotFoundException {
        List<Long> wanted = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (wanted.isEmpty()) {
            return;
        }
        List<Long> found = new ArrayList<>(assets.findByStoreMerchantIdAndIdIn(store.getId(), wanted).stream()
                .map(MediaAsset::getId).toList());
        for (Long id : wanted) {
            if (!found.contains(id)) {
                throw ContentNotFoundException.media(id, store);
            }
        }
    }

}
