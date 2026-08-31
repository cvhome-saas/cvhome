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
 * The plate of a section: the index of refinements ruled down the start side, the sort and the printed
 * page address across the top, the die-cuts ranged between them, and the page turn at the foot.
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
        <div className="flex flex-col">
            {listing.facets.manufacturers.length > 0 && (
                <fieldset className="hair border-b py-4 first:pt-0">
                    <legend className="cover-line mb-3">{t('FILTER_BY_MANUFACTURER')}</legend>
                    <RadioGroup value={query.manufacturerId?.toString() ?? 'all'} onValueChange={v => listing.setManufacturer(v === 'all' ? undefined : Number(v))} className="gap-2.5">
                        <div className="flex items-center gap-2.5"><RadioGroupItem value="all" id="m-all"/><Label htmlFor="m-all" className="text-sm font-medium normal-case tracking-normal">{t('ALL')}</Label></div>
                        {listing.facets.manufacturers.map(m => (
                            <div key={m.id} className="flex items-center gap-2.5">
                                <RadioGroupItem value={String(m.id)} id={`m-${m.id}`}/>
                                <Label htmlFor={`m-${m.id}`} className="text-sm font-medium normal-case tracking-normal">{m.description?.name}</Label>
                            </div>
                        ))}
                    </RadioGroup>
                </fieldset>
            )}
            {optionFacetGroups(listing.facets.options).map(group => (
                <fieldset key={group.name} className="hair border-b py-4">
                    <legend className="cover-line mb-3">{t('FILTER_BY_OPTION', {option: group.name})}</legend>
                    <div className="flex flex-col gap-2.5">
                        {group.values.map(v => (
                            <div key={v.id} className="flex items-center gap-2.5">
                                <Checkbox id={`ov-${v.id}`} checked={!!query.optionValueIds?.includes(v.id)} onCheckedChange={() => listing.toggleOptionValue(v.id)}/>
                                <Label htmlFor={`ov-${v.id}`} className="text-sm font-medium normal-case tracking-normal">{v.name}</Label>
                            </div>
                        ))}
                    </div>
                </fieldset>
            ))}
            {listing.hasActiveFilters && <Button variant="ghost" size="sm" className="mt-3 self-start" onClick={listing.clearFilters}>{t('CLEAR_FILTERS')}</Button>}
        </div>
    );

    return (
        <div className="flex flex-col gap-6 lg:flex-row lg:gap-10">
            {hasFacets && (
                <aside className="hidden w-56 shrink-0 lg:block" aria-label={t('FILTERS')}>
                    <h2 className="hair display mb-3 border-b-2 pb-2 text-lg">{t('FILTERS')}</h2>
                    {facetPanel}
                </aside>
            )}
            <div className="flex min-w-0 flex-1 flex-col gap-4">
                <div className="hair flex flex-wrap items-center justify-between gap-3 border-b pb-3">
                    <p className="cover-line" aria-live="polite">{t('RESULTS_COUNT', {count: total})}</p>
                    <div className="flex items-center gap-2">
                        {hasFacets && (
                            <Drawer>
                                <DrawerTrigger asChild>
                                    <Button variant="outline" size="sm" className="lg:hidden"><SlidersHorizontalIcon data-icon="inline-start"/>{t('FILTERS')}</Button>
                                </DrawerTrigger>
                                <DrawerContent side="start" className="hair w-[88vw] max-w-sm overflow-y-auto rounded-none border-e-2">
                                    <DrawerHeader className="flood hair border-b-2">
                                        <DrawerTitle className="display text-2xl">{t('FILTERS')}</DrawerTitle>
                                        <DrawerDescription className="sr-only">{t('FILTERS')}</DrawerDescription>
                                    </DrawerHeader>
                                    <div className="px-4 pb-6">{facetPanel}</div>
                                </DrawerContent>
                            </Drawer>
                        )}
                        <Label htmlFor="sort" className="sr-only">{t('SORT_LABEL')}</Label>
                        <Select value={query.sort} onValueChange={v => listing.setSort(v as ListingSort)}>
                            <SelectTrigger id="sort" size="sm" className="w-40"><SelectValue/></SelectTrigger>
                            <SelectContent className="hair rounded-none border-2">
                                <SelectGroup>
                                    {sorts.map(s => <SelectItem key={s} value={s} className="rounded-none">{t(`SORT.${s.toUpperCase()}`)}</SelectItem>)}
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
                    <ul className="plate grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5" aria-busy>
                        {Array.from({length: 10}).map((_, i) => (
                            <li key={i} className="flex flex-col">
                                <Skeleton className="aspect-product w-full rounded-none"/>
                                <div className="hair flex flex-col gap-2 border-t p-3"><Skeleton className="h-4 w-3/4"/><Skeleton className="h-6 w-1/2"/></div>
                            </li>
                        ))}
                    </ul>
                )}
                {status === 'empty' && <EmptyState kind="listing" action={listing.hasActiveFilters ? <Button variant="outline" onClick={listing.clearFilters}>{t('CLEAR_FILTERS')}</Button> : undefined}/>}
                {status === 'ready' && (
                    <div className={loading ? 'opacity-60 transition-opacity' : undefined} aria-busy={loading}>
                        <ProductGrid products={items} storeContext={storeContext} grid={grid}/>
                    </div>
                )}

                {totalPages > 1 && (
                    <nav className="hair mt-2 flex items-center justify-between gap-3 border-t-2 pt-4" aria-label={t('PAGE_OF', {page: page + 1, total: totalPages})}>
                        <Button variant="outline" size="sm" disabled={page <= 0} onClick={() => listing.setPage(page - 1)}>
                            <ChevronLeftIcon data-icon="inline-start" className="rtl:rotate-180"/>{t('PREVIOUS')}
                        </Button>
                        <span className="pagemark">{t('PAGE_OF', {page: page + 1, total: totalPages})}</span>
                        <Button variant="outline" size="sm" disabled={page + 1 >= totalPages} onClick={() => listing.setPage(page + 1)}>
                            {t('NEXT')}<ChevronRightIcon data-icon="inline-end" className="rtl:rotate-180"/>
                        </Button>
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
