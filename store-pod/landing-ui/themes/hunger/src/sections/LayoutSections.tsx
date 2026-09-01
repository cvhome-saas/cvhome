import {slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Masthead} from './Masthead';
import {ProductGrid} from '../components/ProductGrid';
import {SectionHeading} from '../components/SectionHeading';

/**
 * hunger's layout-section registry — the sheet. The hero is the theme's Masthead; product sections are
 * printed as menu sections. A menu does not hide its dishes behind arrows, so the `rail` variant sets
 * the dense dish-line list instead of a carousel, and `grid` sets the larger board face.
 */

function HeroSection({ctx, section}: SectionRenderProps) {
    // The masthead is print, not a flood: at the layout's default full width it still keeps the
    // sheet's own margins instead of running its caps into the viewport edge.
    return (
        <div className="mx-auto w-full max-w-(--container-wide) px-4 sm:px-6">
            <Masthead store={ctx.store} slides={slidesAsBanners(section.items)}/>
        </div>
    );
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    return (
        <section className="min-w-0">
            {title && <SectionHeading title={title} subtitle={section.text.subtitle}/>}
            <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}
                         variant={section.variant === 'rail' ? 'line' : 'board'}/>
        </section>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroSection, carousel: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
};
