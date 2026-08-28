'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {PageProps, SearchData} from '@store-front/theme';
import type {FacetBucket, FacetDimension, ListingSort} from '@store-front/types';
import {LISTING_SORTS} from '@store-front/types';
import {useProductSearch} from '@store-front/hooks/use-product-search';
import {primaryImage, productHref} from '@store-front/services/product-presenter';
import {AspectBox} from '@store-front/ui/aspect-box';
import {Button} from '@store-front/ui/button';
import {Badge} from '@store-front/ui/badge';

/**
 * The results page for a theme that has not designed its own.
 *
 * Undesigned on purpose, and built only from design tokens, so it takes on the theme's type, colour, spacing
 * and radius rather than looking like a different shop. A theme replaces it by exporting `pages.Search`.
 */
export function DefaultSearchPage({ctx, data}: PageProps<SearchData>) {
    const t = useTranslations('PAGE.SEARCH');
    const {query, products, page, facets, status, hasFilters, didYouMean, setSort, goToPage, toggle, clearFilters, retry} =
        useProductSearch(ctx.storeContext, {initial: data.initial, initialQuery: data.query, facets: data.facets});

    const groups: { label: string; dimension: FacetDimension; buckets: FacetBucket[] }[] = [
        {label: t('FILTER_BY_CATEGORY'), dimension: 'categoryIds' as const, buckets: facets.categories},
        {label: t('FILTER_BY_BRAND'), dimension: 'manufacturerIds' as const, buckets: facets.brands},
        {label: t('FILTER_BY_TYPE'), dimension: 'productTypeIds' as const, buckets: facets.types},
    ].filter(g => g.buckets.length > 0);

    return (
        <div className="mx-auto w-full max-w-(--container-content) px-4 py-8">
            <header className="mb-6">
                <h1 className="text-2xl font-semibold">
                    <bdi dir="auto">{query.q ? t('HEADING', {query: query.q}) : t('HEADING_EMPTY_QUERY')}</bdi>
                </h1>
                <p className="mt-1 text-sm text-muted-foreground">{t('RESULTS_COUNT', {count: page.totalElements})}</p>
                {didYouMean && (
                    <p className="mt-2 text-sm">
                        <bdi dir="auto">{t('DID_YOU_MEAN', {suggestion: didYouMean})}</bdi>
                    </p>
                )}
                {data.fallbackLanguage && (
                    <p className="mt-2 text-sm text-muted-foreground">{t('OTHER_LANGUAGE')}</p>
                )}
            </header>

            <div className="flex flex-col gap-8 lg:flex-row">
                {groups.length > 0 && (
                    <aside className="w-full shrink-0 lg:w-56">
                        <div className="mb-3 flex items-center justify-between">
                            <h2 className="text-sm font-medium">{t('FILTERS')}</h2>
                            {hasFilters && (
                                <Button type="button" variant="ghost" size="sm" onClick={clearFilters}>
                                    {t('CLEAR_FILTERS')}
                                </Button>
                            )}
                        </div>
                        {groups.map(group => (
                            <section key={group.dimension} className="mb-5">
                                <h3 className="mb-2 text-xs uppercase tracking-wide text-muted-foreground">{group.label}</h3>
                                <ul className="flex flex-col gap-1">
                                    {group.buckets.map(bucket => (
                                        <li key={bucket.id}>
                                            <label className="flex cursor-pointer items-center gap-2 text-sm">
                                                <input type="checkbox" checked={bucket.selected}
                                                       onChange={() => toggle(group.dimension, bucket.id)}/>
                                                <span className="truncate"><bdi dir="auto">{bucket.name}</bdi></span>
                                                <span className="ms-auto text-xs text-muted-foreground">{bucket.count}</span>
                                            </label>
                                        </li>
                                    ))}
                                </ul>
                            </section>
                        ))}
                    </aside>
                )}

                <main className="min-w-0 flex-1">
                    <div className="mb-4 flex items-center justify-end gap-2">
                        <label className="text-sm text-muted-foreground" htmlFor="search-sort">{t('SORT_LABEL')}</label>
                        <select id="search-sort" value={query.sort} onChange={e => setSort(e.target.value as ListingSort)}
                                className="rounded-control border bg-background px-2 py-1 text-sm">
                            {LISTING_SORTS.map(s => (
                                <option key={s} value={s}>{t(`SORT.${s.toUpperCase()}` as 'SORT.RELEVANCE')}</option>
                            ))}
                        </select>
                    </div>

                    {status === 'error' && (
                        <div className="rounded-card border p-8 text-center">
                            <p className="text-sm text-muted-foreground">{t('SEARCH_ERROR')}</p>
                            <Button type="button" variant="outline" size="sm" className="mt-3" onClick={retry}>
                                {t('SUBMIT')}
                            </Button>
                        </div>
                    )}

                    {status === 'empty' && (
                        <div className="rounded-card border p-8 text-center">
                            <p className="text-sm text-muted-foreground">{t('RESULTS_COUNT', {count: 0})}</p>
                        </div>
                    )}

                    {products.length > 0 && (
                        <ul className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
                            {products.map(product => {
                                const img = primaryImage(product);
                                return (
                                    <li key={product.id}>
                                        <Link prefetch={false} href={productHref(product)} className="group block">
                                            <AspectBox className="bg-card">
                                                <Image src={img.src} alt={img.alt} fill
                                                       sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
                                                       className="object-cover"/>
                                            </AspectBox>
                                            <p className="mt-2 line-clamp-2 text-sm group-hover:underline">
                                                <bdi dir="auto">{product.description?.name}</bdi>
                                            </p>
                                            {product.finalPrice != null && (
                                                <Badge variant="secondary" className="mt-1">{product.finalPrice}</Badge>
                                            )}
                                        </Link>
                                    </li>
                                );
                            })}
                        </ul>
                    )}

                    {page.totalPages > 1 && (
                        <nav className="mt-8 flex items-center justify-center gap-4">
                            <Button type="button" variant="outline" size="sm" disabled={query.page <= 0}
                                    onClick={() => goToPage(query.page - 1)}>
                                {t('PREVIOUS')}
                            </Button>
                            <span className="text-sm text-muted-foreground">
                                {t('PAGE_OF', {page: query.page + 1, total: page.totalPages})}
                            </span>
                            <Button type="button" variant="outline" size="sm"
                                    disabled={query.page + 1 >= page.totalPages}
                                    onClick={() => goToPage(query.page + 1)}>
                                {t('NEXT')}
                            </Button>
                        </nav>
                    )}
                </main>
            </div>
        </div>
    );
}
