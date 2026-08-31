import type {SectionRenderProps, ThemeSectionRegistry} from '@store-front/theme';
import type {Banner, SectionItem} from '@store-front/types';
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

/** The layout's inline slides, dressed as the banner shape the theme's Swiper hero already renders. */
export function slidesAsBanners(items: readonly SectionItem[] | null | undefined): Banner[] {
    return (items ?? [])
        .filter(item => typeof item.props.mediaUrl === 'string')
        .map((item, index): Banner => ({
            id: index,
            placement: 'CAROUSEL',
            position: index,
            servedLocale: null,
            title: item.text.heading ?? null,
            subtitle: item.text.subheading ?? null,
            body: null,
            ctaLabel: item.text.cta ?? null,
            target: null,
            desktopUrl: item.props.mediaUrl as string,
            mobileUrl: null,
            altText: item.text.heading ?? '',
            theme: null,
            startsAt: null,
            endsAt: null,
        }));
}

function HeroSection({section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    if (slides.length === 0) return null;
    return <Hero slides={slides}/>;
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
