'use client'
import {useTranslations} from 'next-intl';
import {ChevronLeftIcon, ChevronRightIcon, SlidersHorizontalIcon} from 'lucide-react';
import type {Category, ListingFacets, ListingQuery, ListingSort, ProductListingPage, StoreContext} from '@store-front/types';
import type {ThemeLayoutConfig} from '@store-front/theme';
import {useProductListing} from '@store-front/hooks/use-product-listing';
import {Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue} from '@store-front/ui/select';
import {RadioGroup, RadioGroupItem} from '@store-front/ui/radio-group';
import {Label} from '@store-front/ui/label';
import {Checkbox} from '@store-front/ui/checkbox';
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle, DrawerTrigger} from '@store-front/ui/drawer';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {ProductGrid} from '../components/ProductGrid';
import {EmptyState} from '../states/EmptyState';
import {GridSkeleton} from '../states/skeletons/shared';

/** Category listing: a notice sheet of filters (rail on desktop, drawer below), sort strip, result stub, the wall, page strips. */
export function Listing({storeContext, category, initial, initialQuery, facets, grid}: {
    storeContext: StoreContext; category: Category; initial: ProductListingPage; initialQuery: ListingQuery; facets: ListingFacets; grid: ThemeLayoutConfig['productGrid'];
}) {
    const t = useTranslations('PAGE.CATEGORY');
    const tc = useTranslations('COMMON');
    const listing = useProductListing(storeContext, category, {initial: initial.content ? initial : undefined, initialQuery, facets});
    const {items, status, loading, total, page, totalPages, query, sorts} = listing;
    const hasFacets = listing.facets.manufacturers.length > 0 || listing.facets.options.length > 0;
    const legend = 'mb-2 font-display text-sm uppercase tracking-wide';

    const facetPanel = hasFacets && (
        <div className="flex flex-col gap-6">
            {listing.facets.manufacturers.length > 0 && (
                <fieldset>
                    <legend className={legend}>{t('FILTER_BY_MANUFACTURER')}</legend>
                    <RadioGroup value={query.manufacturerId?.toString() ?? 'all'} onValueChange={v => listing.setManufacturer(v === 'all' ? undefined : Number(v))} className="gap-2">
                        <div className="flex items-center gap-2"><RadioGroupItem value="all" id="m-all"/><Label htmlFor="m-all" className="font-sans normal-case tracking-normal">{t('ALL')}</Label></div>
                        {listing.facets.manufacturers.map(m => (
                            <div key={m.id} className="flex items-center gap-2">
                                <RadioGroupItem value={String(m.id)} id={`m-${m.id}`}/><Label htmlFor={`m-${m.id}`} className="font-sans normal-case tracking-normal">{m.description?.name}</Label>
                            </div>
                        ))}
                    </RadioGroup>
                </fieldset>
            )}
            {optionFacetGroups(listing.facets.options).map(group => (
                <fieldset key={group.name}>
                    <legend className={legend}>{t('FILTER_BY_OPTION', {option: group.name})}</legend>
                    <div className="flex flex-col gap-2">
                        {group.values.map(v => (
                            <div key={v.id} className="flex items-center gap-2">
                                <Checkbox id={`ov-${v.id}`} checked={!!query.optionValueIds?.includes(v.id)} onCheckedChange={() => listing.toggleOptionValue(v.id)} className="rounded-none"/>
                                <Label htmlFor={`ov-${v.id}`} className="font-sans normal-case tracking-normal">{v.name}</Label>
                            </div>
                        ))}
                    </div>
                </fieldset>
            ))}
            {listing.hasActiveFilters && <button type="button" className="strip strip-hover h-8 w-fit text-xs" onClick={listing.clearFilters}>{t('CLEAR_FILTERS')}</button>}
        </div>
    );

    return (
        <div className="flex flex-col gap-6 lg:flex-row lg:gap-8">
            {hasFacets && <aside className="sheet hidden h-fit w-60 shrink-0 p-4 [--tilt:0.5deg] lg:block" aria-label={t('FILTERS')}>{facetPanel}</aside>}
            <div className="flex min-w-0 flex-1 flex-col gap-5">
                <div className="flex flex-wrap items-center justify-between gap-3">
                    <p className="strip h-8 bg-foreground px-2.5 text-xs text-background [--tilt:0.6deg]" aria-live="polite"><span className="tabular-nums">{t('RESULTS_COUNT', {count: total})}</span></p>
                    <div className="flex items-center gap-2">
                        {hasFacets && (
                            <Drawer>
                                <DrawerTrigger asChild>
                                    <button type="button" className="strip strip-hover h-9 text-xs lg:hidden"><SlidersHorizontalIcon className="size-4"/>{t('FILTERS')}</button>
                                </DrawerTrigger>
                                <DrawerContent side="start" className="w-[88vw] max-w-sm overflow-y-auto border-0 bg-(--wall) [background-image:var(--grain)]">
                                    <DrawerHeader><DrawerTitle className="strip h-9 w-fit text-base [--tilt:-1deg]">{t('FILTERS')}</DrawerTitle><DrawerDescription className="sr-only">{t('FILTERS')}</DrawerDescription></DrawerHeader>
                                    <div className="sheet mx-4 mb-6 p-4 [--tilt:0.4deg]">{facetPanel}</div>
                                </DrawerContent>
                            </Drawer>
                        )}
                        <Label htmlFor="sort" className="sr-only">{t('SORT_LABEL')}</Label>
                        <Select value={query.sort} onValueChange={v => listing.setSort(v as ListingSort)}>
                            <SelectTrigger id="sort" size="sm" className="strip h-9 w-40 border-0 text-xs shadow-sm [--tilt:0deg]"><SelectValue/></SelectTrigger>
                            <SelectContent className="sheet rounded-none border-0 [--tilt:0deg]">
                                <SelectGroup>
                                    {sorts.map(s => <SelectItem key={s} value={s} className="rounded-none font-display text-sm uppercase tracking-wide">{t(`SORT.${s.toUpperCase()}`)}</SelectItem>)}
                                </SelectGroup>
                            </SelectContent>
                        </Select>
                    </div>
                </div>

                {status === 'error' && (
                    <div className="sheet p-6 [--tilt:-0.4deg]">
                        <ErrorBlock title={t('LISTING_ERROR')} action={<button type="button" className="strip strip-hover h-9" onClick={listing.retry}>{tc('RETRY')}</button>}>
                            {listing.error?.message}
                        </ErrorBlock>
                    </div>
                )}
                {status === 'loading' && <GridSkeleton count={8}/>}
                {status === 'empty' && <EmptyState kind="listing" action={listing.hasActiveFilters ? <button type="button" className="strip strip-hover h-9" onClick={listing.clearFilters}>{t('CLEAR_FILTERS')}</button> : undefined}/>}
                {status === 'ready' && (
                    <div className={loading ? 'opacity-60 transition-opacity' : undefined} aria-busy={loading}>
                        <ProductGrid products={items} storeContext={storeContext} grid={grid}/>
                    </div>
                )}

                {totalPages > 1 && (
                    <nav className="mt-4 flex items-center justify-center gap-3" aria-label={t('PAGE_OF', {page: page + 1, total: totalPages})}>
                        <button type="button" className="strip strip-hover h-9 disabled:opacity-40 disabled:hover:bg-background disabled:hover:text-foreground [--tilt:-0.6deg]" disabled={page <= 0} onClick={() => listing.setPage(page - 1)}>
                            <ChevronLeftIcon className="size-4 rtl:rotate-180"/>{t('PREVIOUS')}
                        </button>
                        <span className="text-sm tabular-nums">{t('PAGE_OF', {page: page + 1, total: totalPages})}</span>
                        <button type="button" className="strip strip-hover h-9 disabled:opacity-40 disabled:hover:bg-background disabled:hover:text-foreground [--tilt:0.6deg]" disabled={page + 1 >= totalPages} onClick={() => listing.setPage(page + 1)}>
                            {t('NEXT')}<ChevronRightIcon className="size-4 rtl:rotate-180"/>
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
