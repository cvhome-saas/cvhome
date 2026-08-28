import {getTranslations} from 'next-intl/server';
import type {HomeData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {Button} from '@store-front/ui/button';
import {PageShell} from '../components/PageShell';
import {SectionHeading} from '../components/SectionHeading';
import {ProductGrid} from '../components/ProductGrid';
import {Hero} from '../sections/Hero';
import {ProductRail} from '../sections/ProductRail';
import {EmptyState} from '../states/EmptyState';

/** Hero (slider images) → one section per product group; the HOME_PAGE group renders as a full grid. */
export async function Home({ctx, data}: PageProps<HomeData>) {
    const t = await getTranslations('PAGE.HOME');
    return (
        <>
            <Hero slides={data.hero.slides}/>
            {data.groups.length === 0 && (
                <PageShell className="py-section">
                    <EmptyState kind="listing" action={<Button asChild variant="outline"><Link prefetch={false} href="/">{t('SHOP_NOW')}</Link></Button>}/>
                </PageShell>
            )}
            {data.groups.map(group => (
                <PageShell key={group.code} width={ctx.layout.container} className="py-section">
                    <section aria-labelledby={`group-${group.code}`}>
                        <SectionHeading title={<span id={`group-${group.code}`}>{group.title}</span>}/>
                        {group.code === 'HOME_PAGE'
                            ? <ProductGrid products={group.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>
                            : <ProductRail products={group.products} storeContext={ctx.storeContext}/>}
                    </section>
                </PageShell>
            ))}
        </>
    );
}
