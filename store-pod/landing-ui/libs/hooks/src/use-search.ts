'use client'
import {useCallback, useEffect, useRef, useState} from "react";
import {NO_SEARCH, SearchCapabilities, SearchResult, StoreContext} from "@store-front/types";

export interface SearchProvider {
    capabilities: SearchCapabilities;
    search(storeContext: StoreContext, query: string, signal?: AbortSignal): Promise<SearchResult>;
}

export const noopSearchProvider: SearchProvider = {
    capabilities: NO_SEARCH,
    async search(_ctx, query) {
        return {query, hits: [], unavailable: true};
    },
};

/**
 * Debounced search over a provider. The shell decides the provider; themes only read `capabilities`
 * and render accordingly (hide the field, or show suggestions-only).
 */
export function useSearch(storeContext: StoreContext, provider: SearchProvider, debounceMs = 200) {
    const [query, setQuery] = useState('');
    const [result, setResult] = useState<SearchResult | undefined>();
    const [loading, setLoading] = useState(false);
    const abort = useRef<AbortController | null>(null);

    useEffect(() => {
        abort.current?.abort();
        if (!query.trim()) {
            setResult(undefined);
            setLoading(false);
            return;
        }
        const controller = new AbortController();
        abort.current = controller;
        setLoading(true);
        const handle = setTimeout(() => {
            provider.search(storeContext, query.trim(), controller.signal)
                .then(r => { if (!controller.signal.aborted) setResult(r); })
                .catch(() => { if (!controller.signal.aborted) setResult({query, hits: [], unavailable: true}); })
                .finally(() => { if (!controller.signal.aborted) setLoading(false); });
        }, debounceMs);
        return () => {
            clearTimeout(handle);
            controller.abort();
        };
    }, [query, provider, storeContext.store, storeContext.locale, debounceMs]);

    const clear = useCallback(() => setQuery(''), []);

    return {query, setQuery, result, loading, clear, capabilities: provider.capabilities};
}
