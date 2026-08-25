import {getTranslations} from 'next-intl/server';
import type {CategoryData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {Listing} from '../sections/Listing';

/** A stretch of wall for one category: trail strips, the category's name sheet with its subcategory strips, then the listing. */
export async function Category({ctx, data}: PageProps<CategoryData>) {
    const t = await getTranslations('PAGE.CATEGORY');
    const {category} = data;
    const children = (category.children ?? []).filter(c => c.description && c.visible !== false);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-6 py-6 lg:py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="sheet sheen w-fit max-w-full p-5 [--tilt:-0.6deg] sm:p-6">
                <h1 className="font-display text-4xl uppercase leading-[0.9] [overflow-wrap:anywhere] sm:text-5xl" dir="auto"><bdi>{category.description.name}</bdi></h1>
                {category.productCount > 0 && <p className="mt-2 text-xs uppercase tracking-wide text-muted-foreground"><span className="tabular-nums">{t('PRODUCTS_IN_CATEGORY', {count: category.productCount})}</span></p>}
                {children.length > 0 && (
                    <ul className="mt-4 flex flex-wrap gap-2">
                        {children.map((c, i) => (
                            <li key={c.code}>
                                <Link prefetch={false} href={`/category/${c.description.friendlyUrl}`} className={`strip strip-hover h-8 text-xs ${i % 2 ? '[--tilt:0.7deg]' : '[--tilt:-0.6deg]'}`}>
                                    {c.description.name}
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
