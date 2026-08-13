package com.asrevo.cvhome.content.service;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.banner.BannerArtwork;
import com.asrevo.cvhome.content.entity.banner.ContentBanner;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.errors.BannerArtworkRequiredException;
import com.asrevo.cvhome.content.errors.BannerCapacityExceededException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentVersionConflictException;
import com.asrevo.cvhome.content.errors.IllegalContentTransitionException;
import com.asrevo.cvhome.content.errors.InvalidMediaException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.LifecycleRequest;
import com.asrevo.cvhome.content.model.banner.BannerArtworkSpec;
import com.asrevo.cvhome.content.model.banner.BannerPlacement;
import com.asrevo.cvhome.content.model.banner.BannerView;
import com.asrevo.cvhome.content.model.banner.BannerWriteRequest;
import com.asrevo.cvhome.content.repository.ContentBannerRepository;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.MediaAssetRepository;

@Service
public class BannerService {
    private static final Map<BannerPlacement, Integer> CAPACITY = capacities();

    private final ContentV2Service contentService;
    private final ContentRepository contentRepository;
    private final ContentBannerRepository bannerRepository;
    private final MediaAssetRepository mediaRepository;
    private final Clock clock = Clock.systemUTC();

    public BannerService(ContentV2Service contentService, ContentRepository contentRepository,
                         ContentBannerRepository bannerRepository, MediaAssetRepository mediaRepository) {
        this.contentService = contentService;
        this.contentRepository = contentRepository;
        this.bannerRepository = bannerRepository;
        this.mediaRepository = mediaRepository;
    }

    @Transactional
    public BannerView create(StoreMerchantId store, LanguageCode language, BannerWriteRequest request, String actor)
            throws BannerCapacityExceededException, InvalidMediaException, ContentNotFoundException {
        requireBannerType(request);
        List<ContentBanner> placement = bannerRepository
                .findForUpdateByContentStoreMerchantIdAndPlacementOrderByPositionAscIdAsc(store,
                        request.placement());
        int capacity = CAPACITY.get(request.placement());
        if (placement.size() >= capacity) {
            throw BannerCapacityExceededException.forPlacement(request.placement().name(), capacity);
        }
        validateArtwork(store, request.artwork());
        ContentView contentView = contentService.create(store, language, request.content(), actor);
        Content content = contentRepository.findByIdAndStoreMerchantId(contentView.id(), store)
                .orElseThrow(() -> ContentNotFoundException.forId(contentView.id()));
        ContentBanner banner = new ContentBanner();
        banner.setContent(content);
        banner.setPlacement(request.placement());
        banner.setPosition(request.position());
        banner.setTargetKind(request.targetKind());
        banner.setTargetValue(request.targetValue());
        banner.setBackgroundColor(request.backgroundColor());
        banner.setForegroundColor(request.foregroundColor());
        banner.setLoginTarget(request.loginTarget());
        banner.setCountryCodes(request.countryCodes());
        BannerArtwork artwork = new BannerArtwork();
        artwork.setLanguageCode(language);
        artwork.setDesktopMediaId(request.artwork().desktopMediaId());
        artwork.setMobileMediaId(request.artwork().mobileMediaId());
        artwork.setAltText(request.artwork().altText());
        banner.addArtwork(artwork);
        return toView(bannerRepository.save(banner), contentView, language);
    }

    @Transactional(readOnly = true)
    public List<BannerView> list(StoreMerchantId store, LanguageCode language, BannerPlacement placement)
            throws ContentNotFoundException {
        List<BannerView> result = new java.util.ArrayList<>();
        for (ContentBanner banner : bannerRepository
                .findAllByContentStoreMerchantIdAndPlacementOrderByPositionAscIdAsc(store, placement)) {
            result.add(toView(banner, contentService.find(store, banner.getId()), language));
        }
        return List.copyOf(result);
    }

    @Transactional(readOnly = true)
    public List<BannerView> effective(StoreMerchantId store, LanguageCode language, BannerPlacement placement,
                                      String countryCode, boolean authenticated) throws ContentNotFoundException {
        Instant now = clock.instant();
        return list(store, language, placement).stream()
                .filter(it -> it.content().status() == ContentStatus.PUBLISHED)
                .filter(it -> it.content().publishAt() == null || !it.content().publishAt().isAfter(now))
                .filter(it -> it.content().unpublishAt() == null || it.content().unpublishAt().isAfter(now))
                .filter(it -> it.countryCodes().isEmpty() || it.countryCodes().contains(countryCode))
                .filter(it -> loginMatches(it.loginTarget(), authenticated))
                .toList();
    }

    private static boolean loginMatches(com.asrevo.cvhome.content.model.banner.LoginTarget target,
                                        boolean authenticated) {
        return switch (target) {
            case ANY -> true;
            case AUTHENTICATED -> authenticated;
            case ANONYMOUS -> !authenticated;
        };
    }

    @Transactional
    public BannerView transition(StoreMerchantId store, LanguageCode language, Long id, long version,
                                 ContentStatus target, LifecycleRequest request, String actor)
            throws ContentNotFoundException, ContentVersionConflictException, IllegalContentTransitionException,
            BannerArtworkRequiredException {
        ContentBanner banner = bannerRepository.findByIdAndContentStoreMerchantId(id, store)
                .orElseThrow(() -> ContentNotFoundException.forId(id));
        if (target == ContentStatus.PUBLISHED) {
            validatePublishArtwork(banner, language);
        }
        ContentView content = contentService.transition(store, id, version, target, request, actor);
        return toView(banner, content, language);
    }

    private void validateArtwork(StoreMerchantId store, BannerArtworkSpec artwork)
            throws InvalidMediaException {
        for (Long mediaId : Stream.of(artwork.desktopMediaId(), artwork.mobileMediaId()).toList()) {
            if (mediaId != null && mediaRepository.findByIdAndStoreMerchantIdAndDeletedAtIsNull(mediaId, store)
                    .isEmpty()) {
                throw InvalidMediaException.because("banner-media");
            }
        }
    }

    private static void validatePublishArtwork(ContentBanner banner, LanguageCode defaultLanguage)
            throws BannerArtworkRequiredException {
        boolean valid = banner.getArtworks().stream()
                .filter(it -> it.getLanguageCode().equals(defaultLanguage))
                .anyMatch(it -> it.getDesktopMediaId() != null && it.getAltText() != null
                        && !it.getAltText().isBlank());
        if (!valid) {
            throw BannerArtworkRequiredException.create();
        }
    }

    private static void requireBannerType(BannerWriteRequest request) throws InvalidMediaException {
        if (request.content().type() != ContentType.BANNER) {
            throw InvalidMediaException.because("content-type");
        }
    }

    private BannerView toView(ContentBanner banner, ContentView content, LanguageCode language) {
        BannerArtwork artwork = banner.getArtworks().stream()
                .filter(it -> it.getLanguageCode().equals(language))
                .findFirst().orElseGet(() -> banner.getArtworks().stream().findFirst().orElseThrow());
        return new BannerView(content, banner.getPlacement(), banner.getPosition(), banner.getTargetKind(),
                banner.getTargetValue(), banner.getBackgroundColor(), banner.getForegroundColor(),
                banner.getLoginTarget(), Set.copyOf(banner.getCountryCodes()), new BannerArtworkSpec(
                artwork.getDesktopMediaId(), artwork.getMobileMediaId(), artwork.getAltText()));
    }

    private static Map<BannerPlacement, Integer> capacities() {
        Map<BannerPlacement, Integer> result = new EnumMap<>(BannerPlacement.class);
        result.put(BannerPlacement.HOME_HERO, 5);
        result.put(BannerPlacement.HOME_SECONDARY, 12);
        result.put(BannerPlacement.CATEGORY, 10);
        result.put(BannerPlacement.CHECKOUT, 3);
        return Map.copyOf(result);
    }
}
