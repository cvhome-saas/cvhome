import {getTranslations} from 'next-intl/server';
import type {HomeData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {SectionHeading} from '../components/SectionHeading';
import {ProductGrid} from '../components/ProductGrid';
import {Hero} from '../sections/Hero';
import {ProductRail} from '../sections/ProductRail';
import {EmptyState} from '../states/EmptyState';

/**
 * The catalogue: the cover (slider stage + title block) inside the gutters, then one spread per product
 * group under a running head — the HOME_PAGE group as a full ruled grid, the others as ruled rails.
 */
export async function Home({ctx, data}: PageProps<HomeData>) {
    const t = await getTranslations('PAGE.HOME');
    const {store} = ctx;
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : NaN;
    const facts = [store.address?.city, Number.isNaN(year) ? undefined : t('SINCE', {year})].filter(Boolean) as string[];
    const first = data.groups[0];
    return (
        <>
            <PageShell width={ctx.layout.container} className="pt-4 lg:pt-6">
                <Hero slides={data.hero.slides} banner={data.hero.banner} storeName={store.name} facts={facts} anchor={first ? `group-${first.code}` : undefined}/>
            </PageShell>
            {data.groups.length === 0 && (
                <PageShell className="py-section"><EmptyState kind="listing"/></PageShell>
            )}
            {data.groups.map(group => (
                <PageShell key={group.code} width={ctx.layout.container} className="pt-section">
                    <section id={`group-${group.code}`} aria-labelledby={`group-${group.code}-title`} className="scroll-mt-20">
                        <SectionHeading id={`group-${group.code}-title`} title={group.title} meta={group.products.length}/>
                        {group.code === 'HOME_PAGE' || group.products.length <= ctx.layout.productGrid.xl
                            ? <ProductGrid products={group.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>
                            : <ProductRail products={group.products} storeContext={ctx.storeContext}/>}
                    </section>
                </PageShell>
            ))}
        </>
    );
}
