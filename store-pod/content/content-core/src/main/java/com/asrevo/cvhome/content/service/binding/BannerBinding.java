package com.asrevo.cvhome.content.service.binding;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.errors.ContentErrors;
import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.banner.BannerMeta;
import com.asrevo.cvhome.content.model.banner.PersistableBanner;
import com.asrevo.cvhome.content.model.banner.ReadableBanner;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.service.ContentMapper;
import com.asrevo.cvhome.content.service.ContentTypeBinding;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.content.support.Strings;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * Banners and promos. Placement and window are columns (the storefront queries them); target, artwork, theme and
 * audience live in {@code meta}. Copy per locale: headline = title, subtext = subtitle, button = ctaLabel, alt.
 */
@Component
@RequiredArgsConstructor
public class BannerBinding implements ContentTypeBinding<PersistableBanner, ReadableBanner> {

    private final MediaService media;

    private final ContentRepository repository;

    private final Clock clock;

    @Override
    public ContentType type() {
        return ContentType.BANNER;
    }

    @Override
    public Class<PersistableBanner> persistableClass() {
        return PersistableBanner.class;
    }

    @Override
    public ReadableBanner newReadable() {
        return new ReadableBanner();
    }

    @Override
    public boolean requiresBody() {
        return false;
    }

    @Override
    public void apply(Content entity, PersistableBanner dto) {
        entity.setPlacement(dto.getPlacement());
        entity.setStartsAt(dto.getStartsAt());
        entity.setEndsAt(dto.getEndsAt());
        entity.setMeta(JsonCodec.write(new BannerMeta(dto.getTarget(), dto.getArtwork(), dto.getTheme(),
                dto.isLoggedInOnly())));
    }

    @Override
    public void populate(Content entity, ReadableBanner dto) {
        BannerMeta meta = meta(entity);
        dto.setPlacement(entity.getPlacement());
        dto.setStartsAt(entity.getStartsAt());
        dto.setEndsAt(entity.getEndsAt());
        dto.setTarget(meta.target());
        dto.setArtwork(meta.artwork());
        dto.setTheme(meta.theme());
        dto.setLoggedInOnly(meta.loggedInOnly());
        dto.setStatus(entity.getStatus());
        dto.setLocales(ContentMapper.locales(entity));
        dto.setAudit(ContentMapper.audit(entity));
        if (meta.artwork() != null) {
            Map<Long, String> urls = media.urls(entity.getStoreMerchantId(),
                    List.of(meta.artwork().desktopMediaId() == null ? -1L : meta.artwork().desktopMediaId(),
                            meta.artwork().mobileMediaId() == null ? -1L : meta.artwork().mobileMediaId()));
            dto.setDesktopUrl(urls.get(meta.artwork().desktopMediaId()));
            dto.setMobileUrl(urls.get(meta.artwork().mobileMediaId()));
        }
    }

    @Override
    public String subtitle(Content entity, LanguageCode language) {
        BannerMeta meta = meta(entity);
        String placement = entity.getPlacement() == null ? "—" : entity.getPlacement().name().toLowerCase();
        String target = meta.target() == null || meta.target().value() == null ? "" : meta.target().value();
        return target.isEmpty() ? placement : String.format("%s · %s", placement, target);
    }

    /**
     * Publishing needs alt text once artwork is set, and the placement must have room: HERO 1, CAROUSEL 8,
     * STRIP 1, COLLECTION 1 per target — counting the other banners whose windows overlap this one's.
     */
    @Override
    public List<FieldError> publishProblems(Content entity, ContentDescription source) {
        List<FieldError> problems = new ArrayList<>();
        BannerMeta meta = meta(entity);
        boolean hasArtwork = meta.artwork() != null && meta.artwork().desktopMediaId() != null;
        if (entity.getPlacement() != BannerPlacement.STRIP && hasArtwork && Strings.blank(source.getAltText())) {
            problems.add(FieldError.of(String.format("translations.%s.altText", source.getLanguageCode().code()),
                    ContentErrors.PUBLISH_INCOMPLETE, "Alt text is required for the artwork."));
        }
        if (entity.getPlacement() == null) {
            problems.add(FieldError.of("placement", ContentErrors.PUBLISH_INCOMPLETE, "Placement is required."));
            return problems;
        }
        Long conflict = capacityConflict(entity);
        if (conflict != null) {
            problems.add(FieldError.of("placement", ContentErrors.BANNER_CAPACITY_EXCEEDED,
                    String.format("Placement %s is full (%d live); banner %d overlaps.", entity.getPlacement(),
                            entity.getPlacement().capacity(), conflict)));
        }
        return problems;
    }

    @Override
    public void afterSave(Content entity) {
        // capacity is enforced at publish time (publishProblems); nothing to do on a draft save
    }

    @Override
    public Map<String, Long> mediaReferences(Content entity) {
        Map<String, Long> refs = new LinkedHashMap<>();
        BannerMeta meta = meta(entity);
        if (meta.artwork() != null) {
            if (meta.artwork().desktopMediaId() != null) {
                refs.put("artwork.desktop", meta.artwork().desktopMediaId());
            }
            if (meta.artwork().mobileMediaId() != null) {
                refs.put("artwork.mobile", meta.artwork().mobileMediaId());
            }
        }
        return refs;
    }

    /**
     * @return the id of a published banner in the same placement (and, for COLLECTION, the same target) whose
     * window overlaps, when that would exceed the placement's capacity; else {@code null}
     */
    Long capacityConflict(Content entity) {
        BannerPlacement placement = entity.getPlacement();
        List<Content> others = repository.findAllByType(entity.getStoreMerchantId(), ContentType.BANNER).stream()
                .filter(c -> !c.getId().equals(entity.getId()))
                .filter(c -> c.getStatus() == ContentStatus.PUBLISHED || c.getStatus() == ContentStatus.SCHEDULED)
                .filter(c -> c.getPlacement() == placement)
                .filter(c -> placement != BannerPlacement.COLLECTION || sameTarget(c, entity))
                .filter(c -> overlaps(c, entity))
                .toList();
        return others.size() >= placement.capacity() ? others.getFirst().getId() : null;
    }

    private static boolean sameTarget(Content a, Content b) {
        var ta = meta(a).target();
        var tb = meta(b).target();
        return ta != null && tb != null && ta.equals(tb);
    }

    private static boolean overlaps(Content a, Content b) {
        Instant aStart = a.getStartsAt() == null ? Instant.MIN : a.getStartsAt();
        Instant aEnd = a.getEndsAt() == null ? Instant.MAX : a.getEndsAt();
        Instant bStart = b.getStartsAt() == null ? Instant.MIN : b.getStartsAt();
        Instant bEnd = b.getEndsAt() == null ? Instant.MAX : b.getEndsAt();
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    /**
     * Whether the banner is live right now: published, inside its publish window and its own schedule.
     */
    public boolean effective(Content c) {
        Instant now = clock.instant();
        if (!c.servable(now)) {
            return false;
        }
        if (c.getStartsAt() != null && c.getStartsAt().isAfter(now)) {
            return false;
        }
        return c.getEndsAt() == null || c.getEndsAt().isAfter(now);
    }

    public static BannerMeta meta(Content entity) {
        BannerMeta m = JsonCodec.read(entity.getMeta(), BannerMeta.class);
        return m != null ? m : new BannerMeta(null, null, null, false);
    }

}
