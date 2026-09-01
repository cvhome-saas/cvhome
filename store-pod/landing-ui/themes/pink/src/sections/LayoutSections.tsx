import {getTranslations} from 'next-intl/server';
import {slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Hero} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * pink's layout-section registry — the issue. The hero is the cover: the name set as the masthead with
 * the merchant's slider bleeding off the end edge. The old cover's contents lines were the group list;
 * they return once hero data can see the page's anchored sections — a hand-faked list would print dead
 * links and zero counts, worse than the quiet cover.
 */

function HeroSection({ctx, section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    return <Hero slides={slides} storeName={section.text.heading ?? ctx.store.name} lines={[]}/>;
}

async function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const t = await getTranslations('PAGE.HOME');
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {title && (
                <SectionHeading title={title} subtitle={section.text.subtitle}
                                action={<span className="dim figure text-sm">{t('ITEMS_COUNT', {count: products.length})}</span>}/>
            )}
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
