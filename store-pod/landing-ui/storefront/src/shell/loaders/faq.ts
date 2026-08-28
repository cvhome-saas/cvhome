import 'server-only';
import {cache} from 'react';
import {getTranslations} from 'next-intl/server';
import {ContentService} from '@store-front/services/content-service';
import type {FaqData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

export const loadFaq = cache(async (group?: string): Promise<FaqData> => {
    const ctx = await getStoreContext();
    const [faq, t, tf] = await Promise.all([
        ContentService.getFaq(ctx, group),
        getTranslations('COMMON'),
        getTranslations('PAGE.FAQ'),
    ]);
    return {
        faq: faq ?? {servedLocale: null, groups: [], jsonLd: ''},
        breadcrumbs: [{id: 'home', name: t('HOME'), href: '/'}, {id: 'faq', name: tf('TITLE'), href: '/help'}],
    };
});
