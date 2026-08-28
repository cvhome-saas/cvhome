'use client'
import {useTranslations} from 'next-intl';
import {ChevronLeftIcon, ChevronRightIcon, SlidersHorizontalIcon, XIcon} from 'lucide-react';
import type {FacetBucket, FacetDimension, ListingSort, StoreContext} from '@store-front/types';
import type {SearchData, ThemeLayoutConfig} from '@store-front/theme';
import {LISTING_SORTS} from '@store-front/types';
import {useProductSearch} from '@store-front/hooks/use-product-search';
import {Button} from '@store-front/ui/button';
import {Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue} from '@store-front/ui/select';
import {Checkbox} from '@store-front/ui/checkbox';
import {Label} from '@store-front/ui/label';
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle, DrawerTrigger} from '@store-front/ui/drawer';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {ProductGrid} from '../components/ProductGrid';
import {EmptyState} from '../states/EmptyState';
import {GridSkeleton} from '../states/skeletons/shared';

/**
 * Search results in the catalogue's own voice: the ruled rail of counted facets, the running count, the ruled
 * grid, page x of y — the same furniture as the category listing, because a shopper who filtered a category
 * yesterday should not have to learn a second set of controls today.
 *
 * What is different is what search adds: the chips of what is currently narrowing the results, and the line
 * that admits when the shop answered a slightly different question than the one that was asked.
 */
export function SearchResults({storeContext, data, grid}: {
    storeContext: StoreContext; data: SearchData; grid: ThemeLayoutConfig['productGrid'];
}) {
    const t = useTranslations('PAGE.SEARCH');
    const tc = useTranslations('COMMON');
    const search = useProductSearch(storeContext, {
        initial: data.initial, initialQuery: data.query, facets: data.facets,
    });
    const {query, products, page, facets, status, hasFilters, setSort, goToPage, toggle, clearFilters, retry} = search;

    const groups: { label: string; dimension: FacetDimension; buckets: FacetBucket[] }[] = [
        {label: t('FILTER_BY_CATEGORY'), dimension: 'categoryIds' as const, buckets: facets.categories},
        {label: t('FILTER_BY_BRAND'), dimension: 'manufacturerIds' as const, buckets: facets.brands},
        {label: t('FILTER_BY_TYPE'), dimension: 'productTypeIds' as const, buckets: facets.types},
    ].filter(g => g.buckets.length > 0);
    const hasFacets = groups.length > 0;
    const legend = 'mb-3 block w-full border-b pb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground';

    const selected = groups.flatMap(g => g.buckets.filter(b => b.selected).map(b => ({...b, dimension: g.dimension})));

    const facetPanel = hasFacets && (
        <div className="flex flex-col gap-6">
            {groups.map(group => (
                <fieldset key={group.dimension}>
                    <legend className={legend}>{group.label}</legend>
                    <div className="flex flex-col gap-2.5">
                        {group.buckets.map(bucket => (
                            <div key={bucket.id} className="flex items-center gap-2.5">
                                <Checkbox id={`${group.dimension}-${bucket.id}`} checked={bucket.selected}
                                          onCheckedChange={() => toggle(group.dimension, bucket.id)}/>
                                <Label htmlFor={`${group.dimension}-${bucket.id}`} className="min-w-0 flex-1 font-normal">
                                    <bdi dir="auto" className="truncate">{bucket.name}</bdi>
                                </Label>
                                <span className="shrink-0 text-xs tabular-nums text-muted-foreground">{bucket.count}</span>
                            </div>
                        ))}
                    </div>
                </fieldset>
            ))}
            {hasFilters && <Button variant="outline" size="sm" onClick={clearFilters} className="self-start">{t('CLEAR_FILTERS')}</Button>}
        </div>
    );

    return (
        <div className="flex flex-col gap-6 lg:flex-row lg:gap-10">
            {hasFacets && <aside className="hidden w-56 shrink-0 lg:block" aria-label={t('FILTERS')}>{facetPanel}</aside>}
            <div className="flex min-w-0 flex-1 flex-col gap-4">
                <div className="flex flex-wrap items-center justify-between gap-3 border-b pb-3">
                    <p className="text-sm tabular-nums text-muted-foreground" aria-live="polite">
                        {t('RESULTS_COUNT', {count: page.totalElements})}
                    </p>
                    <div className="flex items-center gap-2">
                        {hasFacets && (
                            <Drawer>
                                <DrawerTrigger asChild>
                                    <Button variant="outline" size="sm" className="lg:hidden">
                                        <SlidersHorizontalIcon data-icon="inline-start"/>{t('FILTERS')}
                                    </Button>
                                </DrawerTrigger>
                                <DrawerContent side="start" className="w-[88vw] max-w-sm overflow-y-auto">
                                    <DrawerHeader className="border-b">
                                        <DrawerTitle className="display text-2xl">{t('FILTERS')}</DrawerTitle>
                                        <DrawerDescription className="sr-only">{t('FILTERS')}</DrawerDescription>
                                    </DrawerHeader>
                                    <div className="px-4 pb-6">{facetPanel}</div>
                                </DrawerContent>
                            </Drawer>
                        )}
                        <Label htmlFor="search-sort" className="sr-only">{t('SORT_LABEL')}</Label>
                        <Select value={query.sort} onValueChange={v => setSort(v as ListingSort)}>
                            <SelectTrigger id="search-sort" size="sm" className="w-40"><SelectValue/></SelectTrigger>
                            <SelectContent>
                                <SelectGroup>
                                    {LISTING_SORTS.map(s => <SelectItem key={s} value={s}>{t(`SORT.${s.toUpperCase()}`)}</SelectItem>)}
                                </SelectGroup>
                            </SelectContent>
                        </Select>
                    </div>
                </div>

                {selected.length > 0 && (
                    <ul className="flex flex-wrap gap-2" aria-label={t('FILTERS')}>
                        {selected.map(bucket => (
                            <li key={`${bucket.dimension}-${bucket.id}`}>
                                <button type="button" onClick={() => toggle(bucket.dimension, bucket.id)}
                                        className="chip hover:bg-muted">
                                    <bdi dir="auto">{bucket.name}</bdi>
                                    <XIcon className="size-3.5" aria-hidden/>
                                    <span className="sr-only">{t('CLEAR_FILTERS')}</span>
                                </button>
                            </li>
                        ))}
                    </ul>
                )}

                {status === 'error' && (
                    <ErrorBlock title={t('SEARCH_ERROR')} className="border"
                                action={<Button variant="outline" onClick={retry}>{tc('RETRY')}</Button>}/>
                )}
                {status === 'loading' && <GridSkeleton/>}
                {status === 'empty' && (
                    <EmptyState kind="search"
                                action={hasFilters ? <Button variant="outline" onClick={clearFilters}>{t('CLEAR_FILTERS')}</Button> : undefined}/>
                )}
                {status === 'ready' && <ProductGrid products={products} storeContext={storeContext} grid={grid}/>}

                {page.totalPages > 1 && (
                    <nav className="mt-2 flex items-center justify-between gap-3 border-t pt-4"
                         aria-label={t('PAGE_OF', {page: query.page + 1, total: page.totalPages})}>
                        <Button variant="outline" size="sm" disabled={query.page <= 0} onClick={() => goToPage(query.page - 1)}>
                            <ChevronLeftIcon data-icon="inline-start" className="rtl:rotate-180"/>{t('PREVIOUS')}
                        </Button>
                        <span className="display text-sm tabular-nums text-muted-foreground">
                            {t('PAGE_OF', {page: query.page + 1, total: page.totalPages})}
                        </span>
                        <Button variant="outline" size="sm" disabled={query.page + 1 >= page.totalPages}
                                onClick={() => goToPage(query.page + 1)}>
                            {t('NEXT')}<ChevronRightIcon data-icon="inline-end" className="rtl:rotate-180"/>
                        </Button>
                    </nav>
                )}
            </div>
        </div>
    );
}

/** The line above the results, when the shop answered a slightly different question than the one asked. */
export function SearchNotice({didYouMean, fallbackLanguage}: { didYouMean?: string; fallbackLanguage?: string }) {
    const t = useTranslations('PAGE.SEARCH');
    if (!didYouMean && !fallbackLanguage) return null;
    return (
        <p className="text-sm text-muted-foreground">
            {didYouMean && <bdi dir="auto">{t('DID_YOU_MEAN', {suggestion: didYouMean})}</bdi>}
            {!didYouMean && fallbackLanguage && t('OTHER_LANGUAGE')}
        </p>
    );
}
