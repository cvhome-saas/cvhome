package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.content.model.site.SiteBranding;

import lombok.Getter;
import lombok.Setter;

/**
 * Everything the storefront layout needs in one read: site SEO, brand imagery, social links, the announcement,
 * both menus, the footer pages and the policy links.
 */
@Getter
@Setter
public class StorefrontSite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String servedLocale;

    /** The store's own title and description, already resolved to the served locale. */
    private StorefrontSeo seo;

    /** Logo, favicon and default share image, resolved to URLs. */
    private SiteBranding branding;

    private List<SocialLink> socialLinks;

    private StorefrontBanner announcement;

    private Map<String, List<StorefrontMenuNode>> menus;

    private List<StorefrontLink> footerPages;

    private List<StorefrontLink> policies;

}
