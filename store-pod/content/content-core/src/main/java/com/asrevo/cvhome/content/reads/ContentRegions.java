package com.asrevo.cvhome.content.reads;

import java.time.Duration;
import java.util.List;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.content.model.storefront.StorefrontFaq;
import com.asrevo.cvhome.content.model.storefront.StorefrontLayout;
import com.asrevo.cvhome.content.model.storefront.StorefrontPage;
import com.asrevo.cvhome.content.model.storefront.StorefrontPolicy;
import com.asrevo.cvhome.content.model.storefront.StorefrontPost;
import com.asrevo.cvhome.content.model.storefront.StorefrontPostList;
import com.asrevo.cvhome.content.model.storefront.StorefrontSite;

/**
 * What content caches: every published storefront read, per store and language. The site, a layout and a menu are
 * on every page and change on publish, so they are held ten seconds; the rest a minute. A preview reads the draft
 * live and never comes here.
 */
public enum ContentRegions implements CacheRegion {

    SITE(Names.SITE, StorefrontSite.class, Duration.ofSeconds(10), 2_000),
    LAYOUT(Names.LAYOUT, StorefrontLayout.class, Duration.ofSeconds(10), 5_000),
    MENU(Names.MENU, List.class, Duration.ofSeconds(10), 5_000),
    PAGE(Names.PAGE, StorefrontPage.class, Duration.ofSeconds(60), 10_000),
    POST(Names.POST, StorefrontPost.class, Duration.ofSeconds(60), 10_000),
    POSTS(Names.POSTS, StorefrontPostList.class, Duration.ofSeconds(60), 5_000),
    POST_CATEGORIES(Names.POST_CATEGORIES, List.class, Duration.ofSeconds(60), 2_000),
    BANNERS(Names.BANNERS, List.class, Duration.ofSeconds(60), 5_000),
    FAQ(Names.FAQ, StorefrontFaq.class, Duration.ofSeconds(60), 5_000),
    POLICY(Names.POLICY, StorefrontPolicy.class, Duration.ofSeconds(60), 5_000),
    SITEMAP(Names.SITEMAP, List.class, Duration.ofSeconds(60), 2_000);

    private final String name;

    private final Class<?> valueType;

    private final Duration ttl;

    private final long maxSize;

    ContentRegions(String name, Class<?> valueType, Duration ttl, long maxSize) {
        this.name = name;
        this.valueType = valueType;
        this.ttl = ttl;
        this.maxSize = maxSize;
    }

    @Override
    public String regionName() {
        return name;
    }

    @Override
    public Class<?> valueType() {
        return valueType;
    }

    @Override
    public Duration ttl() {
        return ttl;
    }

    @Override
    public long maxSize() {
        return maxSize;
    }

    /** The names, as {@code @Cacheable} needs a constant. */
    public static final class Names {

        public static final String SITE = "content.site";

        public static final String LAYOUT = "content.layout";

        public static final String MENU = "content.menu";

        public static final String PAGE = "content.page";

        public static final String POST = "content.post";

        public static final String POSTS = "content.posts";

        public static final String POST_CATEGORIES = "content.post-categories";

        public static final String BANNERS = "content.banners";

        public static final String FAQ = "content.faq";

        public static final String POLICY = "content.policy";

        public static final String SITEMAP = "content.sitemap";

        private Names() {
        }
    }
}
