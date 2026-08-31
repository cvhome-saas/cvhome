import type {SectionRenderProps, ThemeSectionRegistry} from '@store-front/theme';
import type {Banner, SectionItem} from '@store-front/types';
import {HeroFrame} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * beauty's layout-section registry. Besides the catalogue variants it registers a theme-exclusive hero
 * variant, `editorial` — proof of the mechanism: the manifest offers it on beauty stores only, and a
 * store that later switches theme falls back to the kind's default variant instead of breaking.
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

function HeroSection({section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    if (slides.length === 0) return null;
    return <HeroFrame slides={slides}/>;
}

/** Exclusive: the first slide as a full plate with the copy set beside it, magazine-opening style. */
function HeroEditorial({section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    const lead = slides[0];
    return (
        <div className="grid gap-6 lg:grid-cols-[3fr_2fr] lg:items-center">
            {lead && (
                <div className="plate relative aspect-[4/3] overflow-hidden bg-muted">
                    {/* eslint-disable-next-line @next/next/no-img-element -- single art-directed still, no srcset */}
                    <img src={lead.desktopUrl ?? ''} alt={lead.altText ?? ''}
                         className="absolute inset-0 size-full object-cover"/>
                </div>
            )}
            <div className="flex flex-col gap-3">
                {section.text.heading && (
                    <h2 className="font-display text-3xl leading-tight lg:text-5xl">
                        <bdi dir="auto">{section.text.heading}</bdi>
                    </h2>
                )}
                {section.text.subheading && (
                    <p className="max-w-prose text-muted-foreground"><bdi dir="auto">{section.text.subheading}</bdi></p>
                )}
            </div>
        </div>
    );
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section>
            {title && <SectionHeading title={title} className="mb-4"/>}
            {rail
                ? <ProductRail products={products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroSection, carousel: HeroSection, editorial: HeroEditorial},
    products: {rail: ProductsSection, grid: ProductsSection},
};
