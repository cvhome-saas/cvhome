import 'server-only';
import type {Metadata} from 'next';
import {cache} from 'react';
import type {StorefrontSeo} from '@store-front/types';
import {getStore} from '@/shell/request/store-context';
import {loadSite} from '@/shell/loaders/site';

/** Store-level title, description and favicon, from the content site settings (one site read, cached). */
export const loadStoreMetadata = cache(async (): Promise<Metadata> => {
    const [store, site] = await Promise.all([getStore().catch(() => undefined), loadSite()]);
    const siteName = site.seo.metaTitle || store?.name || '';
    // The favicon is its own slot. It used to fall back to the logo, which put a wide wordmark in a 16px tab.
    const icon = site.branding.favicon?.url ?? site.branding.logo?.url;
    return {
        title: {default: siteName, template: siteName ? `%s · ${siteName}` : '%s'},
        description: site.seo.metaDescription || undefined,
        icons: icon ? {icon} : undefined,
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
