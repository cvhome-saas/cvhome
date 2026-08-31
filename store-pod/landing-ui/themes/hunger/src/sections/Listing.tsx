'use client'
import {useTranslations} from 'next-intl';
import {ChevronLeftIcon, ChevronRightIcon, SlidersHorizontalIcon} from 'lucide-react';
import type {Category, ListingFacets, ListingQuery, ListingSort, ProductListingPage, StoreContext} from '@store-front/types';
import type {ThemeLayoutConfig} from '@store-front/theme';
import {useProductListing} from '@store-front/hooks/use-product-listing';
import {Button} from '@store-front/ui/button';
import {Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue} from '@store-front/ui/select';
import {RadioGroup, RadioGroupItem} from '@store-front/ui/radio-group';
import {Label} from '@store-front/ui/label';
import {Checkbox} from '@store-front/ui/checkbox';
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle, DrawerTrigger} from '@store-front/ui/drawer';
import {Skeleton} from '@store-front/ui/skeleton';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {ProductGrid} from '../components/ProductGrid';
import {EmptyState} from '../states/EmptyState';

/**
 * The section's dish lines, with the narrowing controls printed above them: filters as a ruled rail on
 * desktop and a drawer on phones, sort in the same rule, the count in the sheet's small print.
 */
export function Listing({storeContext, category, initial, initialQuery, facets, grid}: {
    storeContext: StoreContext; category: Category; initial: ProductListingPage; initialQuery: ListingQuery; facets: ListingFacets; grid: ThemeLayoutConfig['productGrid'];
}) {
    const t = useTranslations('PAGE.CATEGORY');
    const tc = useTranslations('COMMON');
    const listing = useProductListing(storeContext, category, {initial: initial.content ? initial : undefined, initialQuery, facets});
    const {items, status, loading, total, page, totalPages, query, sorts} = listing;
    const hasFacets = listing.facets.manufacturers.length > 0 || listing.facets.options.length > 0;

    const facetPanel = hasFacets && (
        <div className="flex flex-col gap-6">
            {listing.facets.manufacturers.length > 0 && (
                <fieldset>
                    <legend className="press mb-2 w-full border-b border-foreground pb-1 text-sm tracking-wide">{t('FILTER_BY_MANUFACTURER')}</legend>
                    <RadioGroup value={query.manufacturerId?.toString() ?? 'all'} onValueChange={v => listing.setManufacturer(v === 'all' ? undefined : Number(v))} className="gap-2">
                        <div className="flex items-center gap-2"><RadioGroupItem value="all" id="m-all"/><Label htmlFor="m-all" className="font-normal">{t('ALL')}</Label></div>
                        {listing.facets.manufacturers.map(m => (
                            <div key={m.id} className="flex items-center gap-2">
                                <RadioGroupItem value={String(m.id)} id={`m-${m.id}`}/><Label htmlFor={`m-${m.id}`} className="font-normal">{m.description?.name}</Label>
                            </div>
                        ))}
                    </RadioGroup>
                </fieldset>
            )}
            {optionFacetGroups(listing.facets.options).map(group => (
                <fieldset key={group.name}>
                    <legend className="press mb-2 w-full border-b border-foreground pb-1 text-sm tracking-wide">{t('FILTER_BY_OPTION', {option: group.name})}</legend>
                    <div className="flex flex-col gap-2">
                        {group.values.map(v => (
                            <div key={v.id} className="flex items-center gap-2">
                                <Checkbox id={`ov-${v.id}`} checked={!!query.optionValueIds?.includes(v.id)} onCheckedChange={() => listing.toggleOptionValue(v.id)}/>
                                <Label htmlFor={`ov-${v.id}`} className="font-normal">{v.name}</Label>
                            </div>
                        ))}
                    </div>
                </fieldset>
            ))}
            {listing.hasActiveFilters && (
                <button type="button" onClick={listing.clearFilters} className="press self-start text-xs tracking-wide underline decoration-primary underline-offset-4">
                    {t('CLEAR_FILTERS')}
                </button>
            )}
        </div>
    );

    return (
        <div className="flex flex-col gap-5 lg:flex-row lg:gap-10">
            {hasFacets && <aside className="hidden w-52 shrink-0 lg:block" aria-label={t('FILTERS')}>{facetPanel}</aside>}
            <div className="flex min-w-0 flex-1 flex-col gap-3">
                <div className="flex flex-wrap items-center justify-between gap-3 border-b border-foreground pb-2">
                    <p className="press text-xs tracking-wide text-muted-foreground" aria-live="polite">{t('RESULTS_COUNT', {count: total})}</p>
                    <div className="flex items-center gap-2">
                        {hasFacets && (
                            <Drawer>
                                <DrawerTrigger asChild>
                                    <button type="button" className="fold h-9 text-xs lg:hidden">
                                        <SlidersHorizontalIcon className="size-3.5"/>{t('FILTERS')}
                                    </button>
                                </DrawerTrigger>
                                <DrawerContent side="start" className="w-[88vw] max-w-sm overflow-y-auto border-e-2 border-foreground">
                                    <DrawerHeader><DrawerTitle className="press text-xl">{t('FILTERS')}</DrawerTitle><DrawerDescription className="sr-only">{t('FILTERS')}</DrawerDescription></DrawerHeader>
                                    <div className="px-4 pb-6">{facetPanel}</div>
                                </DrawerContent>
                            </Drawer>
                        )}
                        <Label htmlFor="sort" className="sr-only">{t('SORT_LABEL')}</Label>
                        <Select value={query.sort} onValueChange={v => listing.setSort(v as ListingSort)}>
                            <SelectTrigger id="sort" size="sm" className="w-36 border-foreground"><SelectValue/></SelectTrigger>
                            <SelectContent className="border-2 border-foreground">
                                <SelectGroup>
                                    {sorts.map(s => <SelectItem key={s} value={s}>{t(`SORT.${s.toUpperCase()}`)}</SelectItem>)}
                                </SelectGroup>
                            </SelectContent>
                        </Select>
                    </div>
                </div>

                {status === 'error' && (
                    <ErrorBlock title={t('LISTING_ERROR')} action={<Button variant="outline" onClick={listing.retry}>{tc('RETRY')}</Button>}>
                        {listing.error?.message}
                    </ErrorBlock>
                )}
                {status === 'loading' && (
                    <ul className="grid border-t border-border" aria-busy>
                        {Array.from({length: 8}).map((_, i) => (
                            <li key={i} className="flex items-center gap-3 border-b border-border py-3.5">
                                <Skeleton className="h-6 w-10"/>
                                <div className="flex flex-1 flex-col gap-2"><Skeleton className="h-4 w-2/3"/><Skeleton className="h-3 w-1/2"/></div>
                                <Skeleton className="h-6 w-16"/>
                            </li>
                        ))}
                    </ul>
                )}
                {status === 'empty' && (
                    <EmptyState kind="listing" action={listing.hasActiveFilters ? (
                        <button type="button" onClick={listing.clearFilters} className="fold h-10 px-5">{t('CLEAR_FILTERS')}</button>
                    ) : undefined}/>
                )}
                {status === 'ready' && (
                    <div className={loading ? 'opacity-60 transition-opacity' : undefined} aria-busy={loading}>
                        <ProductGrid products={items} storeContext={storeContext} grid={grid}/>
                    </div>
                )}

                {totalPages > 1 && (
                    <nav className="mt-4 flex items-center justify-center gap-0" aria-label={t('PAGE_OF', {page: page + 1, total: totalPages})}>
                        <button type="button" className="fold h-9 text-xs disabled:opacity-40" disabled={page <= 0} onClick={() => listing.setPage(page - 1)}>
                            <ChevronLeftIcon className="size-3.5 rtl:rotate-180"/>{t('PREVIOUS')}
                        </button>
                        <span className="press -mx-px border border-foreground px-4 py-2 text-xs tabular-nums">{t('PAGE_OF', {page: page + 1, total: totalPages})}</span>
                        <button type="button" className="fold h-9 text-xs disabled:opacity-40" disabled={page + 1 >= totalPages} onClick={() => listing.setPage(page + 1)}>
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
