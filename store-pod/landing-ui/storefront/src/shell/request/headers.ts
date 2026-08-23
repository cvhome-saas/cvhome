import 'server-only';
import {headers} from 'next/headers';
import {cache} from 'react';

/**
 * The routing headers spg/Caddy injects per request (`MerchantRoutingService.mapHeaders`).
 * Node lowercases header names. Reading these avoids an API round-trip for colours and languages.
 */
export interface StoreHeaders {
    storeId: string | undefined;
    theme: string | undefined;
    colorTheme: string | undefined;
    defaultLanguage: string | undefined;
    supportedLanguages: string[];
}

export const getStoreHeaders = cache(async (): Promise<StoreHeaders> => {
    const h = await headers();
    return {
        storeId: h.get('store-id') ?? undefined,
        theme: h.get('theme') ?? undefined,
        colorTheme: h.get('color-theme') ?? undefined,
        defaultLanguage: h.get('default-language') ?? undefined,
        supportedLanguages: (h.get('supported-languages') ?? '').split(',').map(s => s.trim()).filter(Boolean),
    };
});
