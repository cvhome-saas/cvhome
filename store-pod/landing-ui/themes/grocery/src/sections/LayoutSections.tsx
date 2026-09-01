import {getTranslations} from 'next-intl/server';
import {slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Hero} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * grocery's layout-section registry — the floor. The hero is the entrance price board answering the
 * merchant's slider; product sections are the aisles: short runs as crate grids, long runs as shelves
 * (the rail), each under its hanging board.
 */

async function HeroSection({ctx, section}: SectionRenderProps) {
    const t = await getTranslations('PAGE.HOME');
    const {store} = ctx;
    const slides = slidesAsBanners(section.items);
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : NaN;
    const facts = [store.address?.city, Number.isNaN(year) ? undefined : t('SINCE', {year})].filter(Boolean) as string[];
    return <Hero slides={slides} storeName={section.text.heading ?? store.name} facts={facts}/>;
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {title && <SectionHeading title={title} subtitle={section.text.subtitle} meta={products.length}/>}
            {rail
                ? <ProductRail products={products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroSection, carousel: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
};
