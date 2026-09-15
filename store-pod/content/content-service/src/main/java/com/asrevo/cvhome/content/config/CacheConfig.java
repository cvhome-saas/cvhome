package com.asrevo.cvhome.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.eviction.EvictionRules;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.entity.ContentRevision;
import com.asrevo.cvhome.content.entity.FaqGroup;
import com.asrevo.cvhome.content.entity.MediaAsset;
import com.asrevo.cvhome.content.entity.Menu;
import com.asrevo.cvhome.content.entity.MenuItem;
import com.asrevo.cvhome.content.entity.PageLayout;
import com.asrevo.cvhome.content.entity.PolicyVersion;
import com.asrevo.cvhome.content.entity.PostCategory;
import com.asrevo.cvhome.content.entity.Redirect;
import com.asrevo.cvhome.content.entity.SectionPreset;
import com.asrevo.cvhome.content.entity.SiteSettings;
import com.asrevo.cvhome.content.reads.ContentRegions;

/**
 * What content caches ({@link ContentRegions}) and which committed write drops which of it. A content row is a
 * page, a post, a banner or a faq entry, and the site lists the policies and menus, so most writes reach several
 * regions; a media asset is shown by url on a page or a layout. A layout revision, a status audit and a media
 * usage row change nothing a shopper sees and have no rule.
 */
@Configuration
public class CacheConfig {

    @Bean
    CacheRegions contentRegions() {
        return CacheRegions.of(ContentRegions.values());
    }

    @Bean
    EvictionRules contentEvictionRules() {
        return EvictionRules.in("com.asrevo.cvhome.content.entity")
                .on(Content.class, ContentDescription.class, ContentRevision.class)
                .evict(ContentRegions.PAGE, ContentRegions.POST, ContentRegions.POSTS, ContentRegions.POST_CATEGORIES,
                        ContentRegions.BANNERS, ContentRegions.FAQ, ContentRegions.SITE, ContentRegions.SITEMAP,
                        ContentRegions.MENU)
                .on(Menu.class, MenuItem.class)
                .evict(ContentRegions.MENU, ContentRegions.SITE)
                .on(PageLayout.class, SectionPreset.class)
                .evict(ContentRegions.LAYOUT)
                .on(PolicyVersion.class)
                .evict(ContentRegions.POLICY, ContentRegions.SITE, ContentRegions.SITEMAP)
                .on(SiteSettings.class)
                .evict(ContentRegions.SITE, ContentRegions.LAYOUT, ContentRegions.SITEMAP)
                .on(FaqGroup.class)
                .evict(ContentRegions.FAQ)
                .on(PostCategory.class)
                .evict(ContentRegions.POSTS, ContentRegions.POST_CATEGORIES, ContentRegions.POST)
                .on(Redirect.class)
                .evict(ContentRegions.SITEMAP)
                .on(MediaAsset.class)
                .evict(ContentRegions.PAGE, ContentRegions.POST, ContentRegions.LAYOUT, ContentRegions.BANNERS)
                .build();
    }
}
