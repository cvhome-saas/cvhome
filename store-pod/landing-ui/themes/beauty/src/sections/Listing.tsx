'use client'
import {useTranslations} from 'next-intl';
import {ChevronLeftIcon, ChevronRightIcon, SlidersHorizontalIcon} from 'lucide-react';
import type {Category, ListingFacets, ListingQuery, ListingSort, ProductListingPage, StoreContext} from '@store-front/types';
import type {ThemeLayoutConfig} from '@store-front/theme';
import {useProductListing} from '@store-front/hooks/use-product-listing';
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle, DrawerTrigger} from '@store-front/ui/drawer';
import {cn} from '@store-front/ui/lib/utils';
import {ProductGrid} from '../components/ProductGrid';
import {TagButton} from '../components/TagButton';
import {EmptyState} from '../states/EmptyState';
import {CardSkeleton} from '../states/skeletons/shared';

/**
 * The stockroom listing: a facet column of quoted plates (the active one wears the tag), a sort rail, the
 * count in mono, plates on a 1px grid, NN / NN pagination. On phones the facets come in a drawer.
 */
export function Listing({storeContext, category, initial, initialQuery, facets, grid}: {
    storeContext: StoreContext; category: Category; initial: ProductListingPage; initialQuery: ListingQuery; facets: ListingFacets; grid: ThemeLayoutConfig['productGrid'];
}) {
    const t = useTranslations('PAGE.CATEGORY');
    const tc = useTranslations('COMMON');
    const listing = useProductListing(storeContext, category, {initial: initial.content ? initial : undefined, initialQuery, facets});
    const {items, status, loading, total, page, totalPages, query, sorts} = listing;
    const hasFacets = listing.facets.manufacturers.length > 0 || listing.facets.options.length > 0;
    const pad = (n: number) => String(n).padStart(2, '0');
    const opt = (active: boolean) => cn('flex w-full items-center justify-between border-b border-foreground px-3 py-2 text-start font-mono text-xs uppercase tracking-wide last:border-b-0', active ? 'tag rounded-none border-x-0' : 'hover:bg-foreground hover:text-background');

    const facetPanel = hasFacets && (
        <div className="flex flex-col gap-4">
            {listing.facets.manufacturers.length > 0 && (
                <fieldset className="plate">
                    <legend className="sr-only">{t('FILTER_BY_MANUFACTURER')}</legend>
                    <p aria-hidden className="q border-b border-foreground px-3 py-1.5 font-display text-sm font-semibold uppercase tracking-wide">{t('FILTER_BY_MANUFACTURER')}</p>
                    <div role="radiogroup" aria-label={t('FILTER_BY_MANUFACTURER')}>
                        <button type="button" role="radio" aria-checked={!query.manufacturerId} onClick={() => listing.setManufacturer(undefined)} className={opt(!query.manufacturerId)}><span className="q">{t('ALL')}</span></button>
                        {listing.facets.manufacturers.map(m => (
                            <button key={m.id} type="button" role="radio" aria-checked={query.manufacturerId === m.id} onClick={() => listing.setManufacturer(m.id)} className={opt(query.manufacturerId === m.id)}>
                                <span className="q">{m.description?.name}</span>
                            </button>
                        ))}
                    </div>
                </fieldset>
            )}
            {optionFacetGroups(listing.facets.options).map(group => (
                <fieldset key={group.name} className="plate">
                    <legend className="sr-only">{t('FILTER_BY_OPTION', {option: group.name})}</legend>
                    <p aria-hidden className="q border-b border-foreground px-3 py-1.5 font-display text-sm font-semibold uppercase tracking-wide">{group.name}</p>
                    {group.values.map(v => {
                        const on = !!query.optionValueIds?.includes(v.id);
                        return <button key={v.id} type="button" role="checkbox" aria-checked={on} onClick={() => listing.toggleOptionValue(v.id)} className={opt(on)}><span className="q">{v.name}</span><span aria-hidden className={cn('size-2.5 border', on ? 'border-primary-foreground bg-primary-foreground' : 'border-foreground')}/></button>;
                    })}
                </fieldset>
            ))}
            {listing.hasActiveFilters && <button type="button" onClick={listing.clearFilters} className="q self-start font-mono text-xs uppercase tracking-wide underline underline-offset-4">{t('CLEAR_FILTERS')}</button>}
        </div>
    );

    return (
        <div className="flex flex-col gap-5 lg:flex-row lg:gap-6">
            {hasFacets && <aside className="hidden w-56 shrink-0 lg:block" aria-label={t('FILTERS')}>{facetPanel}</aside>}
            <div className="flex min-w-0 flex-1 flex-col gap-4">
                <div className="flex flex-wrap items-stretch justify-between gap-2 border-b border-foreground pb-3">
                    <p className="plate flex items-center px-2 font-mono text-xs uppercase tracking-wide" aria-live="polite">{t('RESULTS_COUNT', {count: total})}</p>
                    <div className="flex items-stretch gap-2">
                        {hasFacets && (
                            <Drawer>
                                <DrawerTrigger asChild>
                                    <button type="button" className="plate flex items-center gap-2 px-3 font-mono text-xs uppercase tracking-wide hover:bg-foreground hover:text-background lg:hidden"><SlidersHorizontalIcon className="size-3.5"/>{t('FILTERS')}</button>
                                </DrawerTrigger>
                                <DrawerContent side="start" className="w-[88vw] max-w-sm overflow-y-auto rounded-none border-foreground p-0 [&>button]:end-4 [&>button]:top-3.5 [&>button]:rounded-none">
                                    <DrawerHeader className="border-b border-foreground py-3"><DrawerTitle className="q font-display text-base font-semibold uppercase tracking-wide">{t('FILTERS')}</DrawerTitle><DrawerDescription className="sr-only">{t('FILTERS')}</DrawerDescription></DrawerHeader>
                                    <div className="p-4">{facetPanel}</div>
                                </DrawerContent>
                            </Drawer>
                        )}
                        <div role="radiogroup" aria-label={t('SORT_LABEL')} className="plate flex items-stretch divide-x divide-foreground rtl:divide-x-reverse">
                            {sorts.map(s => (
                                <button key={s} type="button" role="radio" aria-checked={query.sort === s} onClick={() => listing.setSort(s as ListingSort)}
                                        className={cn('q px-3 font-mono text-xs uppercase tracking-wide', query.sort === s ? 'tag rounded-none' : 'hover:bg-muted')}>
                                    {t(`SORT.${s.toUpperCase()}`)}
                                </button>
                            ))}
                        </div>
                    </div>
                </div>

                {status === 'error' && (
                    <div className="plate p-6 text-center">
                        <div className="hazard mx-auto mb-4 h-2 w-24" aria-hidden/>
                        <p className="font-display text-xl font-semibold uppercase">{t('LISTING_ERROR')}</p>
                        <p className="mt-1 font-mono text-xs text-muted-foreground">{listing.error?.message}</p>
                        <TagButton size="sm" className="mt-4" onClick={listing.retry}>{tc('RETRY')}</TagButton>
                    </div>
                )}
                {status === 'loading' && (
                    <ul className="grid grid-cols-2 ps-px pt-px lg:grid-cols-3 xl:grid-cols-4" aria-busy>
                        {Array.from({length: 8}).map((_, i) => <li key={i} className="-ms-px -mt-px border border-foreground bg-background p-3"><CardSkeleton/></li>)}
                    </ul>
                )}
                {status === 'empty' && <EmptyState kind="listing" action={listing.hasActiveFilters ? <TagButton size="sm" onClick={listing.clearFilters}>{t('CLEAR_FILTERS')}</TagButton> : undefined}/>}
                {status === 'ready' && (
                    <div className={loading ? 'opacity-60 transition-opacity' : undefined} aria-busy={loading}>
                        <ProductGrid products={items} storeContext={storeContext} grid={grid}/>
                    </div>
                )}

                {totalPages > 1 && (
                    <nav className="mt-2 flex items-stretch justify-center font-mono text-xs" aria-label={t('PAGE_OF', {page: page + 1, total: totalPages})}>
                        <button type="button" disabled={page <= 0} onClick={() => listing.setPage(page - 1)} className="plate flex items-center gap-1 px-3 py-2 uppercase tracking-wide hover:bg-foreground hover:text-background disabled:opacity-40 disabled:hover:bg-background disabled:hover:text-foreground">
                            <ChevronLeftIcon className="size-3.5 rtl:rotate-180"/>{t('PREVIOUS')}
                        </button>
                        <span className="plate -ms-px flex items-center px-3 tabular-nums">{pad(page + 1)} / {pad(totalPages)}</span>
                        <button type="button" disabled={page + 1 >= totalPages} onClick={() => listing.setPage(page + 1)} className="plate -ms-px flex items-center gap-1 px-3 py-2 uppercase tracking-wide hover:bg-foreground hover:text-background disabled:opacity-40 disabled:hover:bg-background disabled:hover:text-foreground">
                            {t('NEXT')}<ChevronRightIcon className="size-3.5 rtl:rotate-180"/>
                        </button>
                    </nav>
                )}
            </div>
        </div>
    );
}

/** The counted option groups, as the rail's checkbox lists render them — "Red (12)". */
function optionFacetGroups(options: ListingFacets['options']): { name: string; values: { id: number; name: string }[] }[] {
    return options
        .filter(o => o.values.length > 0)
        .map(o => ({name: o.name, values: o.values.map(v => ({id: v.id, name: `${v.name} (${v.count})`}))}));
}
