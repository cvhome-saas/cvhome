import type {SectionRenderProps, ThemeSectionRegistry} from '@store-front/theme';
import type {Banner, SectionItem} from '@store-front/types';
import {Hero} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * basic's layout-section registry: the theme's own hero and product surfaces, wired to the builder's
 * document. Everything unregistered renders through the shell fallbacks.
 */

function slidesAsBanners(items: readonly SectionItem[] | null | undefined): Banner[] {
    return (items ?? [])
        .filter(item => typeof item.props.mediaUrl === 'string')
        .map((item, index): Banner => ({
            id: index, placement: 'CAROUSEL', position: index, servedLocale: null,
            title: item.text.heading ?? null, subtitle: item.text.subheading ?? null, body: null,
            ctaLabel: item.text.cta ?? null, target: null,
            desktopUrl: item.props.mediaUrl as string, mobileUrl: null,
            altText: item.text.heading ?? '', theme: null, startsAt: null, endsAt: null,
        }));
}

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
