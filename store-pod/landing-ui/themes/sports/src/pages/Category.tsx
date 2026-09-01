import {getTranslations} from 'next-intl/server';
import type {CategoryData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {Listing} from '../client';

export async function Category({ctx, data}: PageProps<CategoryData>) {
    const t = await getTranslations('PAGE.CATEGORY');
    const {category} = data;
    const children = (category.children ?? []).filter(c => c.description && c.visible !== false);
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-6 py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="flex flex-col gap-2">
                <h1 className="text-3xl font-semibold tracking-tight">{category.description.name}</h1>
                {category.productCount > 0 && <p className="text-sm text-muted-foreground">{t('PRODUCTS_IN_CATEGORY', {count: category.productCount})}</p>}
                {children.length > 0 && (
                    <ul className="mt-2 flex flex-wrap gap-2">
                        {children.map(c => (
                            <li key={c.code}>
                                <Link prefetch={false} href={`/category/${c.description.friendlyUrl}`} className="inline-flex rounded-badge border px-3 py-1 text-sm hover:bg-muted">
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
