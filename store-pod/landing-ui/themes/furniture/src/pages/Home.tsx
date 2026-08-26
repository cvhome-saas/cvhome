import {getTranslations} from 'next-intl/server';
import type {HomeData, PageProps} from '@store-front/theme';
import {CategoryService} from '@store-front/services/category-service';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {SectionHeading} from '../components/SectionHeading';
import {DirectoryBoard, type Department} from '../components/DirectoryBoard';
import {floors as boardFloors} from '../components/floors';
import {ProductGrid} from '../components/ProductGrid';
import {Hero} from '../sections/Hero';
import {ProductRail} from '../sections/ProductRail';
import {EmptyState} from '../states/EmptyState';

/** The merchant's category order is the building's floor order — 01 is the first department they listed. */
function departments(categories: Parameters<typeof boardFloors>[0]): Department[] {
    return boardFloors(categories)
        .map(c => ({code: c.code, name: c.description.name, href: `/category/${c.description.friendlyUrl}`, count: c.productCount ?? 0}));
}

/**
 * The building: its directory board beside the window onto the current floor, then one numbered plate per
 * product group, landings of terrazzo between them so the scroll breathes.
 */
export async function Home({ctx, data}: PageProps<HomeData>) {
    const t = await getTranslations('PAGE.HOME');
    const tc = await getTranslations('COMMON');
    const {store} = ctx;
    // The board needs the category tree, which the shell hands to the layout and not to this page.
    const tree = await CategoryService.getCategories(ctx.storeContext);
    const floors = departments(tree?.content);
    const totalItems = floors.reduce((n, d) => n + d.count, 0);

    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : NaN;
    const facts = [store.address?.city, Number.isNaN(year) ? undefined : t('SINCE', {year})].filter(Boolean) as string[];
    const caption = [tc('FLOORS_COUNT', {count: floors.length}), totalItems > 0 ? t('ITEMS_COUNT', {count: totalItems}) : undefined]
        .filter(Boolean).join(' · ');
    const firstGroup = data.groups[0];

    return (
        <>
            <PageShell width={ctx.layout.container} className="pt-6 lg:pt-10">
                <div className="grid grid-cols-[minmax(0,1fr)] items-stretch gap-5 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.15fr)] lg:gap-8">
                    <DirectoryBoard
                        title={store.name}
                        facts={facts}
                        departments={floors}
                        headings={{floor: tc('FLOOR'), department: tc('DEPARTMENT'), items: tc('ITEMS')}}
                        className="min-w-0"
                        action={firstGroup ? (
                            <a href={`#group-${firstGroup.code}`}
                               className="sign inline-flex items-center rounded-control bg-background px-5 py-3 text-xs text-foreground shadow-sm transition-transform duration-(--motion-fast) hover:translate-y-px">
                                {t('SHOP_NOW')}
                            </a>
                        ) : floors.length > 0 ? (
                            <Link prefetch={false} href={floors[0].href}
                                  className="sign inline-flex items-center rounded-control bg-background px-5 py-3 text-xs text-foreground shadow-sm transition-transform duration-(--motion-fast) hover:translate-y-px">
                                {t('SHOP_NOW')}
                            </Link>
                        ) : undefined}
                    />
                    <div className="min-w-0 overflow-hidden min-h-[15rem] lg:min-h-0">
                        <Hero slides={data.hero.slides} caption={caption}
                              planCaption={tc('FLOORS_COUNT', {count: floors.length})}/>
                    </div>
                </div>
            </PageShell>

            {data.groups.length === 0 && (
                <PageShell className="py-section"><EmptyState kind="listing"/></PageShell>
            )}

            {data.groups.map((group, i) => {
                const landing = i % 2 === 1;
                return (
                    <div key={group.code} className={landing ? 'terrazzo border-y border-[var(--brass-faint)]' : undefined}>
                        <PageShell width={ctx.layout.container} className="py-section">
                            <section id={`group-${group.code}`} aria-labelledby={`group-${group.code}-title`} className="scroll-mt-[calc(var(--header-h-lg)+1rem)]">
                                <SectionHeading
                                    id={`group-${group.code}-title`}
                                    plate={String(i + 1).padStart(2, '0')}
                                    title={group.title}
                                    meta={t('ITEMS_COUNT', {count: group.products.length})}/>
                                {group.code === 'HOME_PAGE' || group.products.length <= ctx.layout.productGrid.xl
                                    ? <ProductGrid products={group.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>
                                    : <ProductRail products={group.products} storeContext={ctx.storeContext}/>}
                            </section>
                        </PageShell>
                    </div>
                );
            })}
        </>
    );
}
