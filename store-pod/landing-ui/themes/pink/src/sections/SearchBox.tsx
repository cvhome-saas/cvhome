'use client'
import {useMemo, useState} from 'react';
import {useTranslations} from 'next-intl';
import {SearchIcon, XIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {SearchCapabilities, StoreContext} from '@store-front/types';
import {noopSearchProvider, type SearchProvider, useSearch} from '@store-front/hooks/use-search';
import {provider as navProvider} from './search-provider.client';
import {Input} from '@store-front/ui/input';
import {Button} from '@store-front/ui/button';
import {cn} from '@store-front/ui/lib/utils';

/**
 * Search UI that is honest about what the platform can do: hidden when nothing is searchable,
 * suggestions-only (categories/pages) today, full text search when a provider with `text` arrives. The
 * placeholder is the short label — the long one truncated mid-word in both the masthead and the drawer —
 * and the panel states the suggestions-only scope where there is room to read it.
 * The provider is resolved client-side from the capabilities the shell put in LayoutData.
 */
export function SearchBox({storeContext, capabilities, className}: { storeContext: StoreContext; capabilities: SearchCapabilities; className?: string }) {
    const t = useTranslations('COMPONENTS.SEARCH');
    const provider = useMemo<SearchProvider>(() => capabilities.suggestions || capabilities.text ? navProvider : noopSearchProvider,
        [capabilities.suggestions, capabilities.text]);
    const {query, setQuery, result, loading, clear} = useSearch(storeContext, provider);
    const [focused, setFocused] = useState(false);
    if (!capabilities.text && !capabilities.suggestions) return null;
    const open = focused && query.trim().length > 0;
    return (
        <div className={cn('relative', className)} onBlur={e => { if (!e.currentTarget.contains(e.relatedTarget as Node)) setFocused(false); }}>
            <label className="relative block">
                <span className="sr-only">{t('LABEL')}</span>
                <SearchIcon className="pointer-events-none absolute start-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"/>
                <Input id="pink-search" name="q" type="search" role="combobox" aria-expanded={open} aria-controls="search-suggestions" aria-autocomplete="list" value={query}
                       placeholder={t('LABEL')}
                       onChange={e => setQuery(e.target.value)} onFocus={() => setFocused(true)} className="w-56 ps-8 pe-8"/>
                {query && (
                    <Button type="button" variant="ghost" size="icon-sm" aria-label={t('LABEL')} onClick={clear} className="absolute end-0.5 top-1/2 -translate-y-1/2">
                        <XIcon/>
                    </Button>
                )}
            </label>
            {open && (
                <div id="search-suggestions" role="listbox" className="hair absolute end-0 top-full z-50 mt-1 w-72 border-2 bg-popover p-0 text-popover-foreground shadow-overlay">
                    {!capabilities.text && <p className="wash cover-line hair border-b px-3 py-2">{t('SUGGESTIONS_ONLY')}</p>}
                    {loading && !result && <p className="px-3 py-2 text-sm text-muted-foreground">…</p>}
                    {result && result.hits.length === 0 && <p className="px-3 py-2 text-sm text-muted-foreground">{result.unavailable ? t('NOT_AVAILABLE') : t('NO_RESULTS')}</p>}
                    {result?.hits.map(h => (
                        <Link key={h.id} prefetch={false} href={h.href} role="option" onClick={clear}
                              className="hair flex items-center justify-between gap-2 border-b px-3 py-2 text-sm font-medium last:border-b-0 hover:bg-primary hover:text-primary-foreground focus:bg-primary focus:text-primary-foreground">
                            <span className="truncate">{h.title}</span>
                            <span className="dim cover-line shrink-0">{h.kind === 'category' ? t('CATEGORIES') : h.kind === 'page' ? t('PAGES') : t('PRODUCTS')}</span>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}
