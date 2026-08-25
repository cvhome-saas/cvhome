import 'server-only';
import type {Metadata} from 'next';
import {cache} from 'react';
import type {StorefrontSeo} from '@store-front/types';
import {getStore} from '@/shell/request/store-context';
import {loadSite} from '@/shell/loaders/site';

/** Store-level title/description from the `meta-title` / `meta-description` snippets (one site read, cached). */
export const loadStoreMetadata = cache(async (): Promise<Metadata> => {
    const [store, site] = await Promise.all([getStore().catch(() => undefined), loadSite()]);
    const siteName = site.snippets['metaTitle'] || store?.name || '';
    return {
        title: {default: siteName, template: siteName ? `%s · ${siteName}` : '%s'},
        description: site.snippets['metaDescription'] || undefined,
        icons: store?.logo?.path ? {icon: store.logo.path} : undefined,
    };
});

export function pageMetadata(title: string | undefined, description?: string, seo?: StorefrontSeo | null): Metadata {
    const meta: Metadata = {title: title || undefined, description: description || undefined};
    if (seo) {
        if (seo.canonicalUrl) meta.alternates = {canonical: seo.canonicalUrl};
        if (seo.noindex) meta.robots = {index: false, follow: true};
        meta.openGraph = {
            title: seo.metaTitle || title || undefined,
            description: seo.metaDescription || description || undefined,
            images: seo.ogImageUrl ? [{url: seo.ogImageUrl}] : undefined,
        };
    }
    return meta;
}
