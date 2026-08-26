package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.content.model.HomeSectionKind;
import com.asrevo.cvhome.content.model.menu.MenuTarget;

import lombok.Getter;
import lombok.Setter;

/**
 * One block of the store's home page, resolved for rendering: copy in the served locale and any image already
 * turned into a URL.
 */
@Getter
@Setter
public class StorefrontSection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String slug;

    private Integer sortOrder;

    private String servedLocale;

    private HomeSectionKind kind;

    /** The product group code, category code, banner slug or FAQ group key this block draws. */
    private String targetValue;

    private String title;

    private String subtitle;

    private String body;

    private String ctaLabel;

    private MenuTarget cta;

    private String imageUrl;

    private Integer itemLimit;

    private String layout;

}
