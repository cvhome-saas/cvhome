import {Link} from '@store-front/i18n/navigation';
import {heroModel, productsModel, slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {EmptyOrHint} from '@store-front/ui/sections/compose';
import {Hero, ProductRail} from '../client';
import {ProductGrid} from '../components/ProductGrid';
import {SectionHeading} from '../components/SectionHeading';

/**
 * starter's layout-section registry — the reference implementation of `ThemeDefinition.sections`.
 * A theme registers renderers per kind and variant; anything it leaves out renders through the shell's
 * fallbacks, so this file only claims what the theme genuinely designs: its Swiper hero and its own
 * product surfaces. Copy this shape when giving another theme designed sections.
 *
 * The hero consumes `heroModel`, so every declared field renders: slides become the Swiper (with the
 * copy and CTA overlaid), and a slide-less hero — the `hero-text` preset — is the type-led panel below.
 */

function HeroSection({section, preview}: SectionRenderProps) {
    const model = heroModel(section);
    const slides = slidesAsBanners(section.items);
    if (slides.length > 0) {
        return <Hero slides={slides} autoplay={model.autoplay ? model.interval : false}/>;
    }
    if (!model.heading && !model.subheading && !model.cta) {
        return <EmptyOrHint preview={preview} label="Hero — add a slide or a heading"/>;
    }
    return (
        <section className="bg-muted">
            <div className="mx-auto flex max-w-3xl flex-col items-center gap-4 px-6 py-16 text-center sm:py-24">
                {model.heading && (
                    <h1 className="text-3xl font-semibold sm:text-5xl"><bdi dir="auto">{model.heading}</bdi></h1>
                )}
                {model.subheading && (
                    <p className="text-muted-foreground sm:text-lg"><bdi dir="auto">{model.subheading}</bdi></p>
                )}
                {model.cta && (
                    <Link prefetch={false} href={model.cta.href}
                          className="mt-2 inline-flex items-center rounded-md bg-primary px-5 py-3 text-sm font-medium text-primary-foreground">
                        <bdi dir="auto">{model.cta.label}</bdi>
                    </Link>
                )}
            </div>
        </section>
    );
}

function ProductsSection({ctx, section, data, preview}: SectionRenderProps) {
    const model = productsModel(section, data);
    if (model.count === 0) return <EmptyOrHint preview={preview} label="Products — pick a source that has products"/>;
    const rail = section.variant === 'rail';
    return (
        <section>
            {model.title && <SectionHeading title={model.title} subtitle={model.subtitle} className="mb-4"/>}
            {rail
                ? <ProductRail products={model.products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={model.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroSection, carousel: HeroSection, split: HeroSection, minimal: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
};
