package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Everything the storefront layout needs in one read: SEO snippets, the announcement, both menus, the footer
 * pages and the policy links.
 */
@Getter
@Setter
public class StorefrontSite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String servedLocale;

    private Map<String, String> snippets;

    private StorefrontBanner announcement;

    private Map<String, List<StorefrontMenuNode>> menus;

    private List<StorefrontLink> footerPages;

    private List<StorefrontLink> policies;

}
