package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import com.asrevo.cvhome.content.model.PageTemplate;

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

    private PageTemplate template;

    /**
     * Reserved for the page builder; empty until then.
     */
    private List<Object> blocks = List.of();

    private StorefrontSeo seo;

    private List<StorefrontLink> breadcrumbs;

    private Instant updatedAt;

}
