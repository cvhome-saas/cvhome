import {slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Hero} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * basic's layout-section registry: the theme's own hero and product surfaces, wired to the builder's
 * document. Everything unregistered renders through the shell fallbacks.
 */

function HeroSection({ctx, section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    if (slides.length === 0 && !section.text.heading) return null;
    return <Hero slides={slides} banner={slides[0]} storeName={section.text.heading ?? ctx.store.name}
                 facts={[]} anchor={section.anchor ?? undefined}/>;
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section>
            {title && <SectionHeading title={title} subtitle={section.text.subtitle} className="mb-4"/>}
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
