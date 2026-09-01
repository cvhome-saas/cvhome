import {slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {HeadlineSheet, SlidePoster} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {PosterImage} from '../components/PosterImage';
import {SectionHeading} from '../components/SectionHeading';

/**
 * fashion's layout-section registry — the wall. The theme's home identity was slider images pasted as
 * peeling posters around a name sheet, never a carousel; the hero renderer keeps that: the first slide
 * is the big poster beside the sheet, every further slide gets its own place in a pasted row under it.
 * `image` sections go up as posters too — on this wall a picture is a sheet, not a banner strip.
 */

function HeroWall({ctx, section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    const [first, ...rest] = slides;
    const storeName = ctx.store.name;
    return (
        // A wall needs margin around its sheets: even at the layout's full width the posters keep
        // gutters instead of touching the viewport edge, or the paste-up reads as a print error.
        <div className="wall mx-auto grid w-full max-w-(--container-wide) grid-cols-2 gap-3 px-4 sm:gap-4 sm:px-6 lg:grid-cols-12 lg:items-stretch">
            <HeadlineSheet ctx={ctx} heading={section.text.heading} subheading={section.text.subheading}
                           className={first ? 'col-span-2 lg:col-span-5 [--tilt:-0.7deg]' : 'col-span-2 lg:col-span-12 [--tilt:-0.7deg]'}/>
            {first && (
                <SlidePoster slide={first} index={0} total={slides.length} storeName={storeName} priority
                             ratio="16 / 10" className="col-span-2 lg:col-span-7 lg:h-full lg:aspect-auto [--tilt:0.8deg]"/>
            )}
            {rest.map((slide, i) => (
                <SlidePoster key={slide.id} slide={slide} index={i + 1} total={slides.length} storeName={storeName}
                             ratio="4 / 3" className={cnTilt(i)}/>
            ))}
        </div>
    );
}

/** Alternating paste angles so a long row still reads as hand-pasted, not printed. */
function cnTilt(index: number): string {
    const tilt = index % 2 === 0 ? '[--tilt:0.6deg]' : '[--tilt:-0.8deg]';
    return `col-span-1 lg:col-span-4 ${tilt}`;
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    return (
        <section className="min-w-0">
            {title && <SectionHeading title={title} meta={products.length}/>}
            <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>
        </section>
    );
}

function ImageSheet({ctx, section}: SectionRenderProps) {
    const src = typeof section.props.mediaUrl === 'string' ? section.props.mediaUrl : undefined;
    if (!src) return null;
    const contained = section.variant === 'contained';
    return (
        <figure className="sheet sheen peel relative overflow-hidden [--tilt:0.5deg]"
                style={{aspectRatio: contained ? '4 / 3' : '21 / 9'}}>
            <PosterImage src={src} alt={section.text.title ?? ''} title={ctx.store.name} tone="faint"
                         fit={contained ? 'contain' : 'cover'} sizes="(max-width: 1024px) 100vw, 1152px"/>
        </figure>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroWall, carousel: HeroWall, split: HeroWall},
    products: {grid: ProductsSection, rail: ProductsSection},
    image: {full: ImageSheet, contained: ImageSheet},
};
