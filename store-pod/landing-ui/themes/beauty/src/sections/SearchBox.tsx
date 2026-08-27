'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {SearchIcon, XIcon} from 'lucide-react';
import {Link, useRouter} from '@store-front/i18n/navigation';
import {searchHref, type SearchCapabilities, type StoreContext} from '@store-front/types';
import {useSearch} from '@store-front/hooks/use-search';
import {useSearchProvider} from '@store-front/hooks/product-search-provider';
import {useSearchCombobox} from '@store-front/hooks/use-search-combobox';
import {cn} from '@store-front/ui/lib/utils';

/** A "SEARCH" plate. Honest: suggestions over categories/pages until the platform has text search. */
export function SearchBox({storeContext, capabilities, className, wide}: { storeContext: StoreContext; capabilities: SearchCapabilities; className?: string; wide?: boolean }) {
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
                <label className="plate relative flex h-9 items-center">
                    <span className="sr-only">{t('LABEL')}</span>
                    <SearchIcon className="pointer-events-none absolute start-2 size-3.5 text-muted-foreground"/>
                    <input type="search" role="combobox" aria-expanded={open} aria-controls={combobox.listboxId} aria-autocomplete="list" {...combobox.inputProps} value={query}
                           placeholder={t('LABEL')} title={capabilities.text ? undefined : t('PLACEHOLDER')} onChange={e => setQuery(e.target.value)} onFocus={() => setFocused(true)}
                           className={cn('h-full bg-transparent ps-7 pe-7 font-mono text-xs uppercase tracking-wide outline-none placeholder:text-muted-foreground', wide ? 'w-full' : 'w-48 xl:w-56')}/>
                    {query && <button type="button" aria-label={t('LABEL')} onClick={clear} className="absolute end-1 flex size-6 items-center justify-center hover:bg-foreground hover:text-background"><XIcon className="size-3.5"/></button>}
                </label>
            </form>
            {open && (
                <div id={combobox.listboxId} role="listbox" className="plate absolute end-0 top-full z-50 -mt-px w-72 shadow-overlay">
                    {!capabilities.text && <p className="border-b border-foreground px-2 py-1 font-mono text-[0.65rem] uppercase tracking-wide text-muted-foreground">{t('SUGGESTIONS_ONLY')}</p>}
                    {loading && !result && <p className="px-2 py-2 font-mono text-xs">…</p>}
                    {result && result.hits.length === 0 && <p className="px-2 py-2 font-mono text-xs uppercase tracking-wide text-muted-foreground">{result.unavailable ? t('NOT_AVAILABLE') : t('NO_RESULTS')}</p>}
                    {hits.map((h, i) => (
                        <Link key={h.id} prefetch={false} href={h.href} {...combobox.optionProps(i)} onClick={clear} className="flex items-center justify-between gap-2 px-2 py-1.5 font-mono text-xs uppercase tracking-wide hover:bg-foreground hover:text-background focus:bg-foreground focus:text-background data-[active=true]:bg-foreground data-[active=true]:text-background">
                            <span className="q truncate">{h.title}</span>
                            <span className="shrink-0 text-[0.65rem] opacity-70">{h.kind === 'category' ? t('CATEGORIES') : h.kind === 'page' ? t('PAGES') : t('PRODUCTS')}</span>
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
