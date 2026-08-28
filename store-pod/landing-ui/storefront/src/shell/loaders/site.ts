import 'server-only';
import {cache} from 'react';
import {ContentService} from '@store-front/services/content-service';
import {parseDescription} from '@store-front/services/description-view-util';
import type {AnnouncementData, Banner, NavPage, SiteContent, StorefrontLink} from '@store-front/types';
import {getStoreContext} from '@/shell/request/store-context';

const NO_SEO = {metaTitle: null, metaDescription: null, keywords: null, canonicalUrl: null, noindex: false, ogImageUrl: null};

const NO_BRANDING = {logo: null, logoDark: null, favicon: null, og: null};

const EMPTY: SiteContent = {
    servedLocale: null, seo: NO_SEO, branding: NO_BRANDING, socialLinks: [],
    announcement: null, menus: {main: [], footer: []}, footerPages: [], policies: [],
};

/** The storefront `site` document — SEO, branding, socials, announcement, menus, pages, policies — per request. */
export const loadSite = cache(async (): Promise<SiteContent> => {
    const ctx = await getStoreContext();
    return (await ContentService.getSite(ctx)) ?? EMPTY;
});

/** A storefront link as the `NavPage` the themes render in navs and footers. */
export function linkAsNavPage(link: StorefrontLink, inMenu: boolean): NavPage {
    return {code: link.slug, name: link.title, href: link.href, inMenu};
}

/** The announcement strip's banner, decoded for the themes' Announcement bars. */
export function bannerAsAnnouncement(banner: Banner): AnnouncementData {
    return {
        // The dismissal key is stable across banners on purpose: a shopper who closed the strip should not have
        // it reappear because the merchant edited the copy.
        code: 'announcement',
        html: parseDescription({description: banner.body || (banner.title ?? '')} as never),
    };
}
