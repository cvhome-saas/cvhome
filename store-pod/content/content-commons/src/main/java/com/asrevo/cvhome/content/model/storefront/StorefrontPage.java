package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorefrontPage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String slug;

    private String servedLocale;

    private String title;

    private String body;

    private StorefrontSeo seo;

    private List<StorefrontLink> breadcrumbs;

    private Instant updatedAt;

}
