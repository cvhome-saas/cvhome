import {heroModel, slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Hero} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * starter's layout-section registry — the reference implementation of `ThemeDefinition.sections`.
 * A theme registers renderers per kind and variant; anything it leaves out renders through the shell's
 * fallbacks, so this file only claims what the theme genuinely designs: its Swiper hero and its own
 * product surfaces. Copy this shape when giving another theme designed sections.
 */

function HeroSection({section}: SectionRenderProps) {
    const model = heroModel(section);
    const slides = slidesAsBanners(section.items);
    if (slides.length === 0) return null;
    return <Hero slides={slides} autoplay={model.autoplay ? model.interval : false}/>;
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
