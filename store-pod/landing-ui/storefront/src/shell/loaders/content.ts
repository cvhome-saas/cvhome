import 'server-only';
import {cache} from 'react';
import {notFound} from 'next/navigation';
import {getTranslations} from 'next-intl/server';
import {ContentService} from '@store-front/services/content-service';
import {parseDescription} from '@store-front/services/description-view-util';
import {isApiError} from '@store-front/types';
import type {ContentData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

export const loadContent = cache(async (url: string): Promise<ContentData> => {
    const ctx = await getStoreContext();
    let page;
    try {
        page = await ContentService.getPage(ctx, url);
    } catch (e) {
        if (isApiError(e) && e.category === 'NOT_FOUND') notFound();
        throw e;
    }
    if (!page) notFound();
    const t = await getTranslations('COMMON');
    return {
        page,
        html: parseDescription(page.description),
        breadcrumbs: [
            {id: 'home', name: t('HOME'), href: '/'},
            {id: String(page.id), name: page.description?.name ?? '', href: `/content/${page.description?.friendlyUrl ?? ''}`},
        ],
    };
});
