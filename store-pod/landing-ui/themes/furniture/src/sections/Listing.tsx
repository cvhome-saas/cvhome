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
import {Figure} from '../components/Figure';
import {EmptyState} from '../states/EmptyState';

/**
 * The floor itself: the refine rail lettered like the aisle signs, the count in its own tabular slot, the
 * goods on the grid, and pagination as two plates with the position between them.
 */
export function Listing({storeContext, category, initial, initialQuery, facets, grid}: {
    storeContext: StoreContext; category: Category; initial: ProductListingPage; initialQuery: ListingQuery; facets: ListingFacets; grid: ThemeLayoutConfig['productGrid'];
}) {
    const t = useTranslations('PAGE.CATEGORY');
    const tc = useTranslations('COMMON');
    const listing = useProductListing(storeContext, category, {initial: initial.content ? initial : undefined, initialQuery, facets});
    const {items, status, loading, total, page, totalPages, query, sorts} = listing;
    const hasFacets = listing.facets.manufacturers.length > 0 || listing.facets.options.length > 0;
    const legend = 'sign rule-brass mb-3 w-full border-b pb-2 text-[0.625rem] text-muted-foreground';

    const facetPanel = hasFacets && (
        <div className="flex flex-col gap-8">
            {listing.facets.manufacturers.length > 0 && (
                <fieldset>
                    <legend className={legend}>{t('FILTER_BY_MANUFACTURER')}</legend>
                    <RadioGroup value={query.manufacturerId?.toString() ?? 'all'} onValueChange={v => listing.setManufacturer(v === 'all' ? undefined : Number(v))} className="gap-2.5">
                        <div className="flex items-center gap-2.5"><RadioGroupItem value="all" id="m-all"/><Label htmlFor="m-all" className="text-sm font-normal">{t('ALL')}</Label></div>
                        {listing.facets.manufacturers.map(m => (
                            <div key={m.id} className="flex items-center gap-2.5">
                                <RadioGroupItem value={String(m.id)} id={`m-${m.id}`}/><Label htmlFor={`m-${m.id}`} className="text-sm font-normal">{m.description?.name}</Label>
                            </div>
                        ))}
                    </RadioGroup>
                </fieldset>
            )}
            {optionFacetGroups(listing.facets.options).map(group => (
                <fieldset key={group.name}>
                    <legend className={legend}>{t('FILTER_BY_OPTION', {option: group.name})}</legend>
                    <div className="flex flex-col gap-2.5">
                        {group.values.map(v => (
                            <div key={v.id} className="flex items-center gap-2.5">
                                <Checkbox id={`ov-${v.id}`} checked={!!query.optionValueIds?.includes(v.id)} onCheckedChange={() => listing.toggleOptionValue(v.id)}/>
                                <Label htmlFor={`ov-${v.id}`} className="text-sm font-normal">{v.name}</Label>
                            </div>
                        ))}
                    </div>
                </fieldset>
            ))}
            {listing.hasActiveFilters && (
                <Button variant="ghost" size="sm" onClick={listing.clearFilters} className="sign self-start text-[0.625rem]">{t('CLEAR_FILTERS')}</Button>
            )}
        </div>
    );

    return (
        <div className="flex flex-col gap-8 lg:flex-row lg:gap-14">
            {hasFacets && <aside className="hidden w-56 shrink-0 lg:block" aria-label={t('FILTERS')}>{facetPanel}</aside>}
            <div className="flex min-w-0 flex-1 flex-col gap-6">
                <div className="rule-brass flex flex-wrap items-center justify-between gap-3 border-b pb-4">
                    <p className="flex items-baseline gap-2 text-sm text-muted-foreground" aria-live="polite">
                        <Figure value={total} className="text-lg text-foreground"/>
                        <span className="sign text-[0.625rem]">{tc('ITEMS')}</span>
                    </p>
                    <div className="flex items-center gap-2">
                        {hasFacets && (
                            <Drawer>
                                <DrawerTrigger asChild>
                                    <Button variant="outline" size="sm" className="sign rule-brass text-[0.625rem] lg:hidden">
                                        <SlidersHorizontalIcon data-icon="inline-start"/>{t('FILTERS')}
                                    </Button>
                                </DrawerTrigger>
                                <DrawerContent side="start" className="w-[90vw] max-w-sm overflow-y-auto">
                                    <DrawerHeader className="enamel-flat rounded-none">
                                        <DrawerTitle className="sign-lg text-base">{t('FILTERS')}</DrawerTitle>
                                        <DrawerDescription className="sr-only">{t('FILTERS')}</DrawerDescription>
                                    </DrawerHeader>
                                    <div className="px-5 py-6">{facetPanel}</div>
                                </DrawerContent>
                            </Drawer>
                        )}
                        <Label htmlFor="sort" className="sr-only">{t('SORT_LABEL')}</Label>
                        <Select value={query.sort} onValueChange={v => listing.setSort(v as ListingSort)}>
                            <SelectTrigger id="sort" size="sm" className="rule-brass w-40 rounded-control"><SelectValue/></SelectTrigger>
                            <SelectContent>
                                <SelectGroup>
                                    {sorts.map(s => <SelectItem key={s} value={s}>{t(`SORT.${s.toUpperCase()}`)}</SelectItem>)}
                                </SelectGroup>
                            </SelectContent>
                        </Select>
                    </div>
                </div>

                {status === 'error' && (
                    <ErrorBlock title={t('LISTING_ERROR')} action={<Button variant="outline" className="sign rule-brass text-[0.625rem]" onClick={listing.retry}>{tc('RETRY')}</Button>}>
                        {listing.error?.message}
                    </ErrorBlock>
                )}
                {status === 'loading' && (
                    <ul className="grid grid-cols-2 gap-x-5 gap-y-10 lg:grid-cols-3 lg:gap-x-8 xl:grid-cols-4" aria-busy>
                        {Array.from({length: 8}).map((_, i) => (
                            <li key={i} className="flex flex-col gap-3.5">
                                <Skeleton className="aspect-product w-full rounded-none"/>
                                <Skeleton className="h-4 w-3/4"/><Skeleton className="h-8 w-full"/>
                            </li>
                        ))}
                    </ul>
                )}
                {status === 'empty' && <EmptyState kind="listing" action={listing.hasActiveFilters ? <Button variant="outline" className="sign rule-brass text-[0.625rem]" onClick={listing.clearFilters}>{t('CLEAR_FILTERS')}</Button> : undefined}/>}
                {status === 'ready' && (
                    <div className={loading ? 'opacity-55 transition-opacity duration-(--motion-base)' : undefined} aria-busy={loading}>
                        <ProductGrid products={items} storeContext={storeContext} grid={grid}/>
                    </div>
                )}

                {totalPages > 1 && (
                    <nav className="rule-brass mt-4 flex items-center justify-center gap-5 border-t pt-6" aria-label={t('PAGE_OF', {page: page + 1, total: totalPages})}>
                        <Button variant="outline" size="sm" className="sign rule-brass text-[0.625rem]" disabled={page <= 0} onClick={() => listing.setPage(page - 1)}>
                            <ChevronLeftIcon data-icon="inline-start" className="rtl:rotate-180"/>{t('PREVIOUS')}
                        </Button>
                        <span className="figure text-sm text-muted-foreground">{t('PAGE_OF', {page: page + 1, total: totalPages})}</span>
                        <Button variant="outline" size="sm" className="sign rule-brass text-[0.625rem]" disabled={page + 1 >= totalPages} onClick={() => listing.setPage(page + 1)}>
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
