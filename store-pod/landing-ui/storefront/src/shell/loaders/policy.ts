import 'server-only';
import {cache} from 'react';
import {notFound} from 'next/navigation';
import {getTranslations} from 'next-intl/server';
import {ContentService} from '@store-front/services/content-service';
import {parseDescription} from '@store-front/services/description-view-util';
import {isApiError, type PolicyType} from '@store-front/types';
import type {PolicyData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

const TYPES: PolicyType[] = ['TERMS', 'PRIVACY', 'RETURNS', 'SHIPPING', 'COOKIES', 'CUSTOM'];

export function asPolicyType(raw: string): PolicyType | undefined {
    const upper = raw.toUpperCase() as PolicyType;
    return TYPES.includes(upper) ? upper : undefined;
}

export const loadPolicy = cache(async (typeRaw: string, version?: number): Promise<PolicyData> => {
    const type = asPolicyType(typeRaw);
    if (!type) notFound();
    const ctx = await getStoreContext();
    let policy;
    try {
        policy = await ContentService.getPolicy(ctx, type, version);
    } catch (e) {
        if (isApiError(e) && e.category === 'NOT_FOUND') notFound();
        throw e;
    }
    const t = await getTranslations('COMMON');
    return {
        policy,
        html: parseDescription({description: policy.body} as never),
        breadcrumbs: [
            {id: 'home', name: t('HOME'), href: '/'},
            {id: policy.type, name: policy.heading, href: `/policies/${policy.type.toLowerCase()}`},
        ],
    };
});
