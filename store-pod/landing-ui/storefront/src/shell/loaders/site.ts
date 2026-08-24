import 'server-only';
import {cache} from 'react';
import {ContentService} from '@store-front/services/content-service';
import {parseDescription} from '@store-front/services/description-view-util';
import type {AnnouncementData, Banner, NavPage, SiteContent, StorefrontLink} from '@store-front/types';
import {getStoreContext} from '@/shell/request/store-context';

const EMPTY: SiteContent = {servedLocale: null, snippets: {}, announcement: null, menus: {main: [], footer: []}, footerPages: [], policies: []};

/** The storefront `site` document — snippets, announcement, menus, footer pages, policies — once per request. */
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
        code: 'header-message',
        html: parseDescription({description: banner.body || (banner.title ?? '')} as never),
    };
}
