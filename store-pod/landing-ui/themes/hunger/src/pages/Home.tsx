import {getTranslations} from 'next-intl/server';
import type {HomeData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {PageShell} from '../components/PageShell';
import {SectionHeading} from '../components/SectionHeading';
import {ProductGrid} from '../components/ProductGrid';
import {Masthead} from '../sections/Masthead';
import {EmptyState} from '../states/EmptyState';

/**
 * The sheet itself: a short masthead, then every product group printed as a menu section — band, dish
 * lines, price column — one continuous read. No carousels between sections: a menu does not hide its
 * dishes behind arrows.
 */
export async function Home({ctx, data}: PageProps<HomeData>) {
    const t = await getTranslations('PAGE.HOME');
    return (
        <PageShell width={ctx.layout.container}>
            <Masthead store={ctx.store} slides={data.hero.slides}/>

            {data.groups.length === 0 && (
                <div className="py-section">
                    <EmptyState kind="listing" action={
                        <Link prefetch={false} href="/" className="fold h-10 px-5">{t('SHOP_NOW')}</Link>
                    }/>
                </div>
            )}

            {/* The front face is set large and single-column; every section after it is the dense list, so the
                sheet paces itself instead of running one wall of lines from top to bottom. */}
            {data.groups.map((group, i) => (
                <section key={group.code} aria-labelledby={`group-${group.code}`} className={i === 0 ? 'pb-section' : 'py-section'}>
                    <SectionHeading title={<span id={`group-${group.code}`}>{group.title}</span>}/>
                    <ProductGrid products={group.products} storeContext={ctx.storeContext}
                                 grid={ctx.layout.productGrid} variant={i === 0 ? 'board' : 'line'}/>
                </section>
            ))}
        </PageShell>
    );
}
