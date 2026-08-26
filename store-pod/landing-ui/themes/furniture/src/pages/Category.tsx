import {getTranslations} from 'next-intl/server';
import type {CategoryData, PageProps} from '@store-front/theme';
import {CategoryService} from '@store-front/services/category-service';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {Listing} from '../sections/Listing';
import {findFloor} from '../components/floors';

/**
 * A floor of the building. The head is the department plate — the same floor number the directory board
 * gave it, the department's name, and what is on it — and the sub-departments hang beneath it as signs.
 */
export async function Category({ctx, data}: PageProps<CategoryData>) {
    const t = await getTranslations('PAGE.CATEGORY');
    const tc = await getTranslations('COMMON');
    const {category} = data;
    const children = (category.children ?? []).filter(c => c.description && c.visible !== false);

    // The floor number is the department's place in the merchant's own category order — the same number
    // the directory board printed. Root of this category's path first, then this category itself.
    const tree = await CategoryService.getCategories(ctx.storeContext);
    const floor = findFloor(tree?.content, data.breadcrumbs.map(b => b.name), category.code)?.number;

    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
            <Breadcrumbs items={data.breadcrumbs}/>

            <header className="flex flex-col gap-5">
                <div className="enamel-flat flex items-center gap-5 px-6 py-5 lg:gap-8 lg:px-9 lg:py-7">
                    {floor && (
                        <span aria-label={`${tc('FLOOR')} ${floor}`} className="floor-no rule-enamel border-e pe-5 text-4xl opacity-90 lg:pe-8 lg:text-5xl">
                            {floor}
                        </span>
                    )}
                    <div className="flex min-w-0 flex-col gap-2">
                        <h1 className="sign-lg text-2xl lg:text-3xl">{category.description.name}</h1>
                        {category.productCount > 0 && (
                            <p className="figure text-sm opacity-85 lg:text-base">{t('PRODUCTS_IN_CATEGORY', {count: category.productCount})}</p>
                        )}
                    </div>
                </div>

                {children.length > 0 && (
                    <nav aria-label={t('SUBCATEGORIES')}>
                        <ul className="flex flex-wrap gap-2.5">
                            {children.map(c => (
                                <li key={c.code}>
                                    <Link prefetch={false} href={`/category/${c.description.friendlyUrl}`}
                                          className="sign rule-brass inline-flex items-center gap-2.5 rounded-control border px-3.5 py-2 text-[0.625rem] transition-colors duration-(--motion-fast) hover:bg-primary hover:text-primary-foreground">
                                        {c.description.name}
                                        {c.productCount > 0 && <span className="figure text-[0.6875rem] opacity-70">{c.productCount}</span>}
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    </nav>
                )}
            </header>

            <Listing storeContext={ctx.storeContext} category={category} initial={data.initial} initialQuery={data.query}
                     facets={data.facets} grid={ctx.layout.productGrid}/>
        </PageShell>
    );
}
