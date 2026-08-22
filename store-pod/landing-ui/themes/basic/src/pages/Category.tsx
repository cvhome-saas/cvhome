import {getTranslations} from 'next-intl/server';
import type {CategoryData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {Listing} from '../sections/Listing';

/** A section of the catalogue: its running head with the count, its sub-index as chips, then the ruled listing. */
export async function Category({ctx, data}: PageProps<CategoryData>) {
    const t = await getTranslations('PAGE.CATEGORY');
    const {category} = data;
    const children = (category.children ?? []).filter(c => c.description && c.visible !== false);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-6 py-6 lg:py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="flex flex-col gap-4">
                <div className="running-head">
                    <h1 className="display text-4xl lg:text-5xl"><bdi dir="auto">{category.description.name}</bdi></h1>
                    {category.productCount > 0 && <p className="mb-1 shrink-0 text-sm tabular-nums text-muted-foreground">{t('PRODUCTS_IN_CATEGORY', {count: category.productCount})}</p>}
                </div>
                {children.length > 0 && (
                    <nav aria-label={t('SUBCATEGORIES')}>
                        <ul className="flex flex-wrap gap-2">
                            {children.map(c => (
                                <li key={c.code}>
                                    <Link prefetch={false} href={`/category/${c.description.friendlyUrl}`} className="chip">
                                        <bdi dir="auto">{c.description.name}</bdi>
                                        {c.productCount > 0 && <span className="text-xs tabular-nums text-muted-foreground">{c.productCount}</span>}
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    </nav>
                )}
            </header>
            <Listing storeContext={ctx.storeContext} category={category} initial={data.initial} initialQuery={data.query} facets={data.facets} grid={ctx.layout.productGrid}/>
        </PageShell>
    );
}
