import 'server-only';
import {cache} from 'react';
import {notFound, redirect} from 'next/navigation';
import {getTranslations} from 'next-intl/server';
import {ContentService} from '@store-front/services/content-service';
import {parseDescription} from '@store-front/services/description-view-util';
import {isApiError, type Page, type StorefrontPage} from '@store-front/types';
import type {ContentData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

/** The storefront page in the legacy `Page` shape the themes render. */
function asPage(p: StorefrontPage): Page {
    return {
        id: p.id, code: p.slug, visible: true, linkToMenu: false, contentType: 'PAGE',
        description: {
            id: p.id, language: p.servedLocale, name: p.title, description: p.body, friendlyUrl: p.slug,
            keyWords: p.seo?.keywords ?? null, highlights: null, metaDescription: p.seo?.metaDescription ?? null,
            title: p.seo?.metaTitle ?? p.title, priceAppender: null,
        },
    };
}

export const loadContent = cache(async (url: string, preview?: string): Promise<ContentData> => {
    const ctx = await getStoreContext();
    let page: StorefrontPage;
    try {
        page = await ContentService.getStorefrontPage(ctx, url, preview);
    } catch (e) {
        if (isApiError(e) && e.category === 'NOT_FOUND') {
            // a slug that moved: the service keeps a redirect from the old path
            const moved = await ContentService.getRedirect(ctx, `/content/${url}`);
            if (moved) redirect(`/${ctx.locale}${moved.to}`);
            notFound();
        }
        throw e;
    }
    const t = await getTranslations('COMMON');
    return {
        page: asPage(page),
        html: parseDescription({description: page.body} as never),
        breadcrumbs: [
            {id: 'home', name: t('HOME'), href: '/'},
            ...page.breadcrumbs.map(b => ({id: b.slug, name: b.title, href: b.href})),
        ],
        seo: page.seo,
        template: page.template,
        blocks: page.blocks,
    };
});
