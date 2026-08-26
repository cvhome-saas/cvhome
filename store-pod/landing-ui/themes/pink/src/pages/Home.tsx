import {getTranslations} from 'next-intl/server';
import type {HomeData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {Button} from '@store-front/ui/button';
import {cn} from '@store-front/ui/lib/utils';
import {PageShell} from '../components/PageShell';
import {SectionHeading} from '../components/SectionHeading';
import {ProductGrid} from '../components/ProductGrid';
import {Hero, type CoverLine} from '../sections/Hero';
import {ProductRail} from '../sections/ProductRail';
import {EmptyState} from '../states/EmptyState';

/**
 * The issue: cover (name, the real cover lines, the merchant's slider) then one feature per product group.
 * The cover lines are the contents — each is the numbered address of a section further down the page.
 */
export async function Home({ctx, data}: PageProps<HomeData>) {
    const t = await getTranslations('PAGE.HOME');
    const groups = data.groups.filter(g => g.products.length > 0);
    const lines: CoverLine[] = groups.map(g => ({id: g.code, title: g.title, count: g.products.length, href: `#group-${g.code}`}));

    return (
        <>
            <Hero slides={data.hero.slides} storeName={ctx.store.name} lines={lines} actionHref={lines[0]?.href}/>

            {data.groups.length === 0 && (
                <PageShell className="py-section">
                    <EmptyState kind="listing" action={<Button asChild variant="outline"><Link prefetch={false} href="/">{t('SHOP_NOW')}</Link></Button>}/>
                </PageShell>
            )}

            {groups.map((group, i) => (
                <div key={group.code} className={cn(i % 2 === 1 && 'wash')}>
                    <PageShell width={ctx.layout.container} className="py-section">
                        <section aria-labelledby={`group-${group.code}-title`} id={`group-${group.code}`} className="scroll-mt-header-lg">
                            <SectionHeading
                                pagemark={String(i + 1).padStart(2, '0')}
                                title={<span id={`group-${group.code}-title`}>{group.title}</span>}
                                action={<span className="dim figure text-sm">{t('ITEMS_COUNT', {count: group.products.length})}</span>}
                            />
                            {group.code === 'HOME_PAGE'
                                ? <ProductGrid products={group.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>
                                : <ProductRail products={group.products} storeContext={ctx.storeContext}/>}
                        </section>
                    </PageShell>
                </div>
            ))}
        </>
    );
}
