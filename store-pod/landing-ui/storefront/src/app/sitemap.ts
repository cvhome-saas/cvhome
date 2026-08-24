import type {MetadataRoute} from 'next';
import {headers} from 'next/headers';
import {ContentService} from '@store-front/services/content-service';
import {locales} from '@store-front/types/constant';
import {getStoreContext} from '@/shell/request/store-context';

/** Published, indexable content per locale: pages, posts, policies, the help index. Products/categories come from the catalog later. */
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
    const h = await headers();
    const host = h.get('x-forwarded-host') ?? h.get('host') ?? 'localhost';
    const proto = h.get('x-forwarded-proto') ?? 'http';
    const ctx = await getStoreContext();
    const entries = await ContentService.getSitemap(ctx);
    const supported = (h.get('supported-languages') ?? '').split(',').map(s => s.trim()).filter(Boolean);
    const langs = supported.length ? supported.filter(l => (locales as readonly string[]).includes(l)) : [ctx.locale];
    return entries.flatMap(e => langs.map(lang => ({
        url: `${proto}://${host}/${lang}${e.loc}`,
        lastModified: e.lastmod ? new Date(e.lastmod) : undefined,
        changeFrequency: e.changefreq as MetadataRoute.Sitemap[number]['changeFrequency'],
    })));
}
