package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorefrontPost implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String slug;

    private String servedLocale;

    private String title;

    private String excerpt;

    /**
     * Only on the single-post read.
     */
    private String body;

    private String heroImageUrl;

    private Instant publishedAt;

    private String authorName;

    private int readingMinutes;

    private boolean featured;

    private List<StorefrontLink> categories;

    private List<String> tags;

    private StorefrontSeo seo;

    private List<StorefrontPost> related;

}
