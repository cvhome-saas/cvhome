import {getTranslations} from 'next-intl/server';
import type {CategoryData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {Listing} from '../client';

/** A zone: its quoted name as the display headline, child zones as plates in a row, the listing below. */
export async function Category({ctx, data}: PageProps<CategoryData>) {
    const t = await getTranslations('PAGE.CATEGORY');
    const {category} = data;
    const children = (category.children ?? []).filter(c => c.description && c.visible !== false);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-5 py-6 lg:py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="flex flex-col gap-4 border-b border-foreground pb-5">
                <div className="flex flex-wrap items-end justify-between gap-4">
                    <h1 className="q font-display text-4xl font-bold uppercase leading-[0.9] tracking-tight [overflow-wrap:anywhere] sm:text-6xl">{category.description.name}</h1>
                    {category.productCount > 0 && <p className="plate px-2 py-1 font-mono text-xs uppercase tracking-wide">{t('PRODUCTS_IN_CATEGORY', {count: category.productCount})}</p>}
                </div>
                {children.length > 0 && (
                    <ul className="flex flex-wrap">
                        {children.map(c => (
                            <li key={c.code} className="-ms-px -mt-px first:ms-0">
                                <Link prefetch={false} href={`/category/${c.description.friendlyUrl}`} className="plate inline-flex px-3 py-1.5 font-mono text-xs uppercase tracking-wide hover:bg-foreground hover:text-background"><span className="q">{c.description.name}</span></Link>
                            </li>
                        ))}
                    </ul>
                )}
            </header>
            <Listing storeContext={ctx.storeContext} category={category} initial={data.initial} initialQuery={data.query} facets={data.facets} grid={ctx.layout.productGrid}/>
        </PageShell>
    );
}
