package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.banner.BannerTarget;
import com.asrevo.cvhome.content.model.banner.BannerTheme;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorefrontBanner implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private BannerPlacement placement;

    private Integer position;

    private String servedLocale;

    private String title;

    private String subtitle;

    private String body;

    private String ctaLabel;

    private BannerTarget target;

    private String desktopUrl;

    private String mobileUrl;

    private String altText;

    private BannerTheme theme;

    private Instant startsAt;

    private Instant endsAt;

}
