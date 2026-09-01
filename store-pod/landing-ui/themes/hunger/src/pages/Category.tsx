import {getTranslations} from 'next-intl/server';
import type {CategoryData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {Listing} from '../client';

/** One section of the menu, opened out: the band, its subsections as fold tabs, then the dish lines. */
export async function Category({ctx, data}: PageProps<CategoryData>) {
    const t = await getTranslations('PAGE.CATEGORY');
    const {category} = data;
    const children = (category.children ?? []).filter(c => c.description && c.visible !== false);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-6 py-6">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="flex flex-col gap-4">
                <div className="plate flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1 px-3 py-2">
                    <h1 className="press text-3xl leading-none sm:text-4xl">{category.description.name}</h1>
                    {category.productCount > 0 && (
                        <p className="press text-sm tracking-wide opacity-85">{t('PRODUCTS_IN_CATEGORY', {count: category.productCount})}</p>
                    )}
                </div>
                {children.length > 0 && (
                    <ul className="scroll-x flex w-full items-stretch" aria-label={t('SUBCATEGORIES')}>
                        {children.map(c => (
                            <li key={c.code} className="-ms-px first:ms-0">
                                <Link prefetch={false} href={`/category/${c.description.friendlyUrl}`} className="fold text-sm">
                                    {c.description.name}
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
