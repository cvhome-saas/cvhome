import {getTranslations} from 'next-intl/server';
import type {CategoryData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {Listing} from '../sections/Listing';

/** A feature opener: the section's name at cover scale on a flooded field, its subsections numbered under it. */
export async function Category({ctx, data}: PageProps<CategoryData>) {
    const t = await getTranslations('PAGE.CATEGORY');
    const {category} = data;
    const children = (category.children ?? []).filter(c => c.description && c.visible !== false);
    // The listing's own count is the truthful one; the category record's is often unset.
    const total = data.initial?.totalElements ?? category.productCount ?? 0;
    return (
        <>
            <header className="flood tone-light hair border-b-2">
                <PageShell width={ctx.layout.container} className="flex flex-col gap-5 py-8 lg:py-12">
                    <Breadcrumbs items={data.breadcrumbs} className="crumbs-flood"/>
                    <div className="flex flex-wrap items-end justify-between gap-4">
                        <h1 className="display text-4xl sm:text-5xl">{category.description.name}</h1>
                        {total > 0 && <span className="flag flag-ink">{t('PRODUCTS_IN_CATEGORY', {count: total})}</span>}
                    </div>
                    {children.length > 0 && (
                        <nav aria-label={t('SUBCATEGORIES')}>
                            <ul className="flex flex-wrap gap-x-5 gap-y-2">
                                {children.map((c, i) => (
                                    <li key={c.code}>
                                        <Link prefetch={false} href={`/category/${c.description.friendlyUrl}`}
                                              className="cover-line inline-flex items-baseline gap-2 underline-offset-4 hover:underline">
                                            <span aria-hidden className="figure">{String(i + 1).padStart(2, '0')}</span>
                                            {c.description.name}
                                        </Link>
                                    </li>
                                ))}
                            </ul>
                        </nav>
                    )}
                </PageShell>
            </header>
            <PageShell width={ctx.layout.container} className="py-8">
                <Listing storeContext={ctx.storeContext} category={category} initial={data.initial} initialQuery={data.query} facets={data.facets} grid={ctx.layout.productGrid}/>
            </PageShell>
        </>
    );
}
