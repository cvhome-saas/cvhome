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

/** Category listing: facets (rail on desktop, drawer on mobile), sort, result count, grid, pagination. */
export function Listing({storeContext, category, initial, initialQuery, facets, grid}: {
    storeContext: StoreContext; category: Category; initial: ProductListingPage; initialQuery: ListingQuery; facets: ListingFacets; grid: ThemeLayoutConfig['productGrid'];
}) {
    const t = useTranslations('PAGE.CATEGORY');
    const tc = useTranslations('COMMON');
    const listing = useProductListing(storeContext, category, {initial: initial.content ? initial : undefined, initialQuery, facets});
    const {items, status, loading, total, page, totalPages, query, sorts} = listing;
    const hasFacets = listing.facets.manufacturers.length > 0 || listing.facets.variants.length > 0;

    const facetPanel = hasFacets && (
        <div className="flex flex-col gap-6">
            {listing.facets.manufacturers.length > 0 && (
                <fieldset>
                    <legend className="mb-2 text-sm font-semibold">{t('FILTER_BY_MANUFACTURER')}</legend>
                    <RadioGroup value={query.manufacturerId?.toString() ?? 'all'} onValueChange={v => listing.setManufacturer(v === 'all' ? undefined : Number(v))} className="gap-2">
                        <div className="flex items-center gap-2"><RadioGroupItem value="all" id="m-all"/><Label htmlFor="m-all">{t('ALL')}</Label></div>
                        {listing.facets.manufacturers.map(m => (
                            <div key={m.id} className="flex items-center gap-2">
                                <RadioGroupItem value={String(m.id)} id={`m-${m.id}`}/><Label htmlFor={`m-${m.id}`}>{m.description?.name}</Label>
                            </div>
                        ))}
                    </RadioGroup>
                </fieldset>
            )}
            {groupVariantFacets(listing.facets.variants).map(group => (
                <fieldset key={group.name}>
                    <legend className="mb-2 text-sm font-semibold">{t('FILTER_BY_OPTION', {option: group.name})}</legend>
                    <div className="flex flex-col gap-2">
                        {group.values.map(v => (
                            <div key={v.id} className="flex items-center gap-2">
                                <Checkbox id={`ov-${v.id}`} checked={!!query.optionValueIds?.includes(v.id)} onCheckedChange={() => listing.toggleOptionValue(v.id)}/>
                                <Label htmlFor={`ov-${v.id}`}>{v.name}</Label>
                            </div>
                        ))}
                    </div>
                </fieldset>
            ))}
            {listing.hasActiveFilters && <Button variant="ghost" size="sm" onClick={listing.clearFilters}>{t('CLEAR_FILTERS')}</Button>}
        </div>
    );

    return (
        <div className="flex flex-col gap-6 lg:flex-row lg:gap-10">
            {hasFacets && <aside className="hidden w-56 shrink-0 lg:block" aria-label={t('FILTERS')}>{facetPanel}</aside>}
            <div className="flex min-w-0 flex-1 flex-col gap-4">
                <div className="flex flex-wrap items-center justify-between gap-3">
                    <p className="text-sm text-muted-foreground" aria-live="polite">{t('RESULTS_COUNT', {count: total})}</p>
                    <div className="flex items-center gap-2">
                        {hasFacets && (
                            <Drawer>
                                <DrawerTrigger asChild>
                                    <Button variant="outline" size="sm" className="lg:hidden"><SlidersHorizontalIcon data-icon="inline-start"/>{t('FILTERS')}</Button>
                                </DrawerTrigger>
                                <DrawerContent side="start" className="w-[88vw] max-w-sm overflow-y-auto">
                                    <DrawerHeader><DrawerTitle>{t('FILTERS')}</DrawerTitle><DrawerDescription className="sr-only">{t('FILTERS')}</DrawerDescription></DrawerHeader>
                                    <div className="px-4 pb-6">{facetPanel}</div>
                                </DrawerContent>
                            </Drawer>
                        )}
                        <Label htmlFor="sort" className="sr-only">{t('SORT_LABEL')}</Label>
                        <Select value={query.sort} onValueChange={v => listing.setSort(v as ListingSort)}>
                            <SelectTrigger id="sort" size="sm" className="w-40"><SelectValue/></SelectTrigger>
                            <SelectContent>
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
                    <ul className="grid grid-cols-2 gap-x-4 gap-y-8 lg:grid-cols-3 xl:grid-cols-4" aria-busy>
                        {Array.from({length: 8}).map((_, i) => <li key={i} className="flex flex-col gap-3"><Skeleton className="aspect-product w-full rounded-image"/><Skeleton className="h-4 w-3/4"/><Skeleton className="h-4 w-1/3"/></li>)}
                    </ul>
                )}
                {status === 'empty' && <EmptyState kind="listing" action={listing.hasActiveFilters ? <Button variant="outline" onClick={listing.clearFilters}>{t('CLEAR_FILTERS')}</Button> : undefined}/>}
                {status === 'ready' && (
                    <div className={loading ? 'opacity-60 transition-opacity' : undefined} aria-busy={loading}>
                        <ProductGrid products={items} storeContext={storeContext} grid={grid}/>
                    </div>
                )}

                {totalPages > 1 && (
                    <nav className="mt-4 flex items-center justify-center gap-3" aria-label={t('PAGE_OF', {page: page + 1, total: totalPages})}>
                        <Button variant="outline" size="sm" disabled={page <= 0} onClick={() => listing.setPage(page - 1)}>
                            <ChevronLeftIcon data-icon="inline-start" className="rtl:rotate-180"/>{t('PREVIOUS')}
                        </Button>
                        <span className="text-sm tabular-nums text-muted-foreground">{t('PAGE_OF', {page: page + 1, total: totalPages})}</span>
                        <Button variant="outline" size="sm" disabled={page + 1 >= totalPages} onClick={() => listing.setPage(page + 1)}>
                            {t('NEXT')}<ChevronRightIcon data-icon="inline-end" className="rtl:rotate-180"/>
                        </Button>
                    </nav>
                )}
            </div>
        </div>
    );
}

function groupVariantFacets(variants: ListingFacets['variants']): { name: string; values: { id: number; name: string }[] }[] {
    const groups = new Map<string, Map<number, string>>();
    for (const v of variants) {
        for (const vv of [v.variation, v.variationValue]) {
            const option = vv?.option;
            const value = vv?.optionValue;
            if (!option?.name || !value) continue;
            if (!groups.has(option.name)) groups.set(option.name, new Map());
            groups.get(option.name)!.set(value.id, value.name || value.description || value.code);
        }
    }
    return [...groups.entries()].map(([name, values]) => ({name, values: [...values.entries()].map(([id, n]) => ({id, name: n}))}));
}
