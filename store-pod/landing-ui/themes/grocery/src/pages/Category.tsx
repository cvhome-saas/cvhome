import {getTranslations} from 'next-intl/server';
import type {CategoryData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {Listing} from '../client';

/** One aisle: the board (name + count), sub-aisle tiles, then the crate listing. */
export async function Category({ctx, data}: PageProps<CategoryData>) {
    const t = await getTranslations('PAGE.CATEGORY');
    const {category} = data;
    const children = (category.children ?? []).filter(c => c.description && c.visible !== false);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-6 py-6 lg:py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="flex flex-col gap-3 border-b-2 pb-4">
                <div className="flex flex-wrap items-baseline gap-x-4 gap-y-1">
                    <h1 className="signage text-4xl lg:text-5xl"><bdi dir="auto">{category.description.name}</bdi></h1>
                    {category.productCount > 0 && (
                        <p className="text-sm font-semibold tabular-nums text-muted-foreground">{t('PRODUCTS_IN_CATEGORY', {count: category.productCount})}</p>
                    )}
                </div>
                {children.length > 0 && (
                    <ul className="scroll-x scroll-x-fade -mx-gutter flex gap-2 px-gutter lg:mx-0 lg:flex-wrap lg:px-0">
                        {children.map(c => (
                            <li key={c.code} className="flex shrink-0">
                                <Link prefetch={false} href={`/category/${c.description.friendlyUrl}`} className="aisle-tile">
                                    <bdi dir="auto">{c.description.name}</bdi>
                                    {c.productCount > 0 && <span className="count">{c.productCount}</span>}
                                </Link>
                            </li>
                        ))}
                    </ul>
                )}
            </header>
            <Listing storeContext={ctx.storeContext} category={category} initial={data.initial} initialQuery={data.query} facets={data.facets} grid={ctx.layout.productGrid}/>
        </PageShell>
    );
}
