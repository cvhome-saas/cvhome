import 'server-only';
import {cache} from 'react';
import {ContentService} from '@store-front/services/content-service';
import type {Banner, Box, Page, SiteContent, StorefrontLink} from '@store-front/types';
import {getStoreContext} from '@/shell/request/store-context';

const EMPTY: SiteContent = {servedLocale: null, snippets: {}, announcement: null, menus: {main: [], footer: []}, footerPages: [], policies: []};

/** The storefront `site` document — snippets, announcement, menus, footer pages, policies — once per request. */
export const loadSite = cache(async (): Promise<SiteContent> => {
    const ctx = await getStoreContext();
    return (await ContentService.getSite(ctx)) ?? EMPTY;
});

/** A storefront link as the legacy `Page` the themes still render in footers and menus. */
export function linkAsPage(link: StorefrontLink, locale: string, linkToMenu: boolean): Page {
    return {
        id: 0,
        code: link.slug,
        visible: true,
        linkToMenu,
        contentType: 'PAGE',
        description: {
            id: 0, language: locale, name: link.title, description: '', friendlyUrl: link.slug,
            keyWords: null, highlights: null, metaDescription: null, title: link.title, priceAppender: null,
        },
    };
}

/** The announcement strip as the legacy `Box` the themes' Announcement bars take. */
export function bannerAsBox(banner: Banner, locale: string): Box {
    return {
        id: banner.id,
        code: 'header-message',
        visible: true,
        contentType: 'BOX',
        description: {
            id: banner.id, language: banner.servedLocale ?? locale, name: banner.title ?? '',
            description: banner.body || (banner.title ?? ''), friendlyUrl: '', keyWords: null, highlights: null,
            metaDescription: null, title: banner.title ?? '', priceAppender: null,
        },
    };
}
