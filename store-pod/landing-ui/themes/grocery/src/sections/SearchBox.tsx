'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {SearchIcon, XIcon} from 'lucide-react';
import {Link, useRouter} from '@store-front/i18n/navigation';
import {searchHref, type SearchCapabilities, type StoreContext} from '@store-front/types';
import {useSearch} from '@store-front/hooks/use-search';
import {useSearchProvider} from '@store-front/hooks/product-search-provider';
import {useSearchCombobox} from '@store-front/hooks/use-search-combobox';
import {Input} from '@store-front/ui/input';
import {Button} from '@store-front/ui/button';
import {cn} from '@store-front/ui/lib/utils';

/**
 * Search UI that is honest about what the platform can do: hidden when nothing is searchable,
 * suggestions-only (categories/pages) today, full text search when a provider with `text` arrives.
 * The provider is resolved client-side from the capabilities the shell put in LayoutData.
 */
export function SearchBox({storeContext, capabilities, className}: { storeContext: StoreContext; capabilities: SearchCapabilities; className?: string }) {
    const t = useTranslations('COMPONENTS.SEARCH');
    const router = useRouter();
    const provider = useSearchProvider(capabilities);
    const {query, setQuery, result, loading, clear} = useSearch(storeContext, provider);
    const [focused, setFocused] = useState(false);
    const open = focused && query.trim().length > 0;
    // Enter goes to the results page. Only when the platform can actually answer a text query — with
    // suggestions alone there is nothing behind /search worth sending anyone to.
    const submit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!capabilities.text || !query.trim()) return;
        setFocused(false);
        router.push(searchHref(query));
    };
    const hits = result?.hits ?? [];
    const seeAll = capabilities.text && hits.length > 0;
    const combobox = useSearchCombobox({
        optionCount: hits.length + (seeAll ? 1 : 0),
        open,
        resetKey: query,
        // Rows are the hits, then the "see all" row when there is one — the order they are rendered in.
        onSelect: index => {
            setFocused(false);
            router.push(index < hits.length ? hits[index].href : searchHref(query));
        },
        onClose: () => setFocused(false),
    });
    // After every hook: bailing earlier would make the hook calls below conditional.
    if (!capabilities.text && !capabilities.suggestions) return null;
    return (
        <div className={cn('relative', className)} onBlur={e => { if (!e.currentTarget.contains(e.relatedTarget as Node)) setFocused(false); }}>
            <form role="search" onSubmit={submit}>
                <label className="relative block">
                    <span className="sr-only">{t('LABEL')}</span>
                    <SearchIcon className="pointer-events-none absolute start-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"/>
                    <Input type="search" role="combobox" aria-expanded={open} aria-controls={combobox.listboxId} aria-autocomplete="list" {...combobox.inputProps} value={query}
                           placeholder={capabilities.text ? t('LABEL') : t('PLACEHOLDER')}
                           onChange={e => setQuery(e.target.value)} onFocus={() => setFocused(true)} className="h-11 w-full border-2 bg-card ps-8 pe-8"/>
                    {query && (
                        <Button type="button" variant="ghost" size="icon-sm" aria-label={t('LABEL')} onClick={clear} className="absolute end-0.5 top-1/2 -translate-y-1/2">
                            <XIcon/>
                        </Button>
                    )}
                </label>
            </form>
            {open && (
                <div id={combobox.listboxId} role="listbox" className="absolute end-0 top-full z-50 mt-1.5 w-72 rounded-overlay border-2 bg-popover p-1.5 text-popover-foreground shadow-overlay">
                    {!capabilities.text && <p className="px-2 py-1 text-xs text-muted-foreground">{t('SUGGESTIONS_ONLY')}</p>}
                    {loading && !result && <p className="px-2 py-2 text-sm text-muted-foreground">…</p>}
                    {result && result.hits.length === 0 && <p className="px-2 py-2 text-sm text-muted-foreground">{result.unavailable ? t('NOT_AVAILABLE') : t('NO_RESULTS')}</p>}
                    {hits.map((h, i) => (
                        <Link key={h.id} prefetch={false} href={h.href} {...combobox.optionProps(i)} onClick={clear}
                              className="flex items-center justify-between gap-2 rounded-control px-2 py-1.5 text-sm hover:bg-muted focus:bg-muted data-[active=true]:bg-muted">
                            <span className="truncate">{h.title}</span>
                            <span className="shrink-0 text-xs text-muted-foreground">{h.kind === 'category' ? t('CATEGORIES') : h.kind === 'page' ? t('PAGES') : t('PRODUCTS')}</span>
                        </Link>
                    ))}
                    {seeAll && (
                    <Link prefetch={false} href={searchHref(query)} {...combobox.optionProps(hits.length)} onClick={() => setFocused(false)}
                          className="block border-t px-3 py-2 text-sm font-medium hover:bg-accent focus:bg-accent data-[active=true]:bg-accent">
                        {t('SEE_ALL')}
                    </Link>
                )}
                </div>
            )}
        </div>
    );
}
