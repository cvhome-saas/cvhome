import 'server-only';
import type {Metadata} from 'next';
import {cache} from 'react';
import {ContentService} from '@store-front/services/content-service';
import {getStore, getStoreContext} from '@/shell/request/store-context';

/** Store-level title/description from the `meta-title` / `meta-description` CMS boxes (parallel, cached). */
export const loadStoreMetadata = cache(async (): Promise<Metadata> => {
    const ctx = await getStoreContext();
    const [store, title, description] = await Promise.all([
        getStore().catch(() => undefined),
        ContentService.getBox(ctx, 'meta-title'),
        ContentService.getBox(ctx, 'meta-description'),
    ]);
    const siteName = title?.description?.description || store?.name || '';
    return {
        title: {default: siteName, template: siteName ? `%s · ${siteName}` : '%s'},
        description: description?.description?.description || undefined,
        icons: store?.logo?.path ? {icon: store.logo.path} : undefined,
    };
});

export function pageMetadata(title: string | undefined, description?: string): Metadata {
    return {title: title || undefined, description: description || undefined};
}
