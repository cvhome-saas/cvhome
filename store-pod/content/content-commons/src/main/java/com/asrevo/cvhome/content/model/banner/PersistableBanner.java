package com.asrevo.cvhome.content.model.banner;

import java.io.Serial;
import java.time.Instant;

import jakarta.validation.constraints.NotNull;

import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.common.PersistableContent;

import lombok.Getter;
import lombok.Setter;

/**
 * A merchandising placement. Per-locale copy (headline = title, subtext = subtitle, button = ctaLabel, alt text)
 * lives on the translations; placement, window, target and artwork here.
 */
@Getter
@Setter
public class PersistableBanner extends PersistableContent {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private BannerPlacement placement;

    private Instant startsAt;

    private Instant endsAt;

    private BannerTarget target;

    private BannerArtwork artwork;

    private BannerTheme theme;

    private boolean loggedInOnly;

}
