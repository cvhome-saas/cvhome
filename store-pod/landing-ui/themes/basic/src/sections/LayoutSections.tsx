import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import {heroModel, productsModel, slidesAsBanners, type SectionRenderProps} from '@store-front/theme';
import {EmptyOrHint, sectionsFromChrome, type SectionChrome} from '@store-front/ui/sections/compose';
import {cn} from '@store-front/ui/lib/utils';
import {Hero, ProductRail} from '../client';
import {ProductGrid} from '../components/ProductGrid';
import {SectionHeading} from '../components/SectionHeading';

/**
 * basic's section chrome — the plain catalogue. Chips mark state, index tabs file the
 * destinations, one primary band, drawn borders for the rest. The composer supplies structure and
 * semantics; the stage hero and product runs stay bespoke.
 */
const chrome: SectionChrome = {
    Heading: ({title, subtitle, meta}) => <SectionHeading title={title} subtitle={subtitle} meta={meta}/>,

    // Chips are typographic; the badge's icon name is deliberately not drawn.
    Badge: ({title, body}) => (
        <span className="flex max-w-56 flex-col items-center gap-2.5 text-center">
            <span className="chip"><bdi dir="auto">{title}</bdi></span>
            {body && <span className="max-w-[26ch] text-sm leading-relaxed text-muted-foreground"><bdi dir="auto">{body}</bdi></span>}
        </span>
    ),

    NavToken: ({label, count, href}) => (
        <Link prefetch={false} href={href} className="index-tab">
            <bdi dir="auto">{label}</bdi>
            {count !== undefined && <span className="text-xs tabular-nums text-muted-foreground">{count}</span>}
        </Link>
    ),

    Band: ({message, action, backgroundSrc}) => (
        <div className={cn('relative flex min-h-12 flex-wrap items-center justify-center gap-x-6 gap-y-2 overflow-hidden px-5 py-3 text-center',
            backgroundSrc ? 'text-white' : 'bg-primary text-primary-foreground')}>
            {backgroundSrc && (
                <>
                    <Image src={backgroundSrc} alt="" fill className="object-cover"/>
                    <span aria-hidden className="absolute inset-0 bg-black/50"/>
                </>
            )}
            <span className="relative text-sm font-semibold uppercase tracking-wide"><bdi dir="auto">{message}</bdi></span>
            {action && (
                <Link prefetch={false} href={action.href} className="relative text-sm underline underline-offset-4">
                    <bdi dir="auto">{action.label}</bdi>
                </Link>
            )}
        </div>
    ),

    Panel: ({children, center}) => (
        <div className={cn('border', center ? 'p-6 text-center sm:p-8' : 'divide-y px-5 py-2')}>{children}</div>
    ),

    Quote: ({quote, author}) => (
        <figure className="flex h-full flex-col border p-5">
            <blockquote className="text-sm leading-relaxed"><bdi dir="auto">“{quote}”</bdi></blockquote>
            {author && (
                <figcaption className="mt-auto pt-3 text-xs uppercase text-muted-foreground"><bdi dir="auto">{author}</bdi></figcaption>
            )}
        </figure>
    ),

    MediaFigure: ({src, alt, caption, href, contained}) => {
        const figure = (
            <figure className="overflow-hidden border">
                <div className={`relative w-full ${contained ? 'aspect-[4/3]' : 'aspect-[21/9]'}`}>
                    <Image src={src!} alt={alt} fill className={contained ? 'object-contain' : 'object-cover'}/>
                </div>
                {caption && (
                    <figcaption className="px-4 py-2 text-sm text-muted-foreground"><bdi dir="auto">{caption}</bdi></figcaption>
                )}
            </figure>
        );
        return href ? <Link prefetch={false} href={href} className="block">{figure}</Link> : figure;
    },

    BrandLabel: ({src, name, href}) => {
        const label = (
            <span className="flex w-32 flex-col items-center gap-1.5 border p-3">
                {src && (
                    <span className="relative block h-10 w-full">
                        <Image src={src} alt={name ?? ''} fill className="object-contain"/>
                    </span>
                )}
                {name && <span className="font-display text-[0.65rem] uppercase"><bdi dir="auto">{name}</bdi></span>}
            </span>
        );
        return href ? <Link prefetch={false} href={href} className="block">{label}</Link> : label;
    },

    VideoFrame: ({player}) => (
        <div className="border p-1">
            <div className="relative aspect-video w-full overflow-hidden">{player}</div>
        </div>
    ),

    PostCard: ({href, title, excerpt, imageSrc}) => (
        <Link prefetch={false} href={href} className="group block h-full border">
            {imageSrc && (
                <span className="relative block aspect-[3/2] overflow-hidden bg-muted">
                    <Image src={imageSrc} alt={title} fill className="object-cover"/>
                </span>
            )}
            <span className="block p-4">
                <span className="font-display text-sm uppercase group-hover:underline"><bdi dir="auto">{title}</bdi></span>
                {excerpt && <span className="mt-1 line-clamp-2 block text-sm text-muted-foreground"><bdi dir="auto">{excerpt}</bdi></span>}
            </span>
        </Link>
    ),

    form: {
        input: 'h-11 min-w-0 flex-1 border bg-background px-3 text-sm outline-none focus-visible:border-foreground',
        button: 'h-11 bg-primary px-5 text-sm font-semibold uppercase tracking-wide text-primary-foreground',
    },
    panelTitleClass: 'font-display text-2xl uppercase',
    proseClass: 'prose-basic',
    summaryClass: 'font-display text-sm font-semibold uppercase tracking-wide',
};

function HeroSection({ctx, section, preview}: SectionRenderProps) {
    const model = heroModel(section);
    const slides = slidesAsBanners(section.items);
    if (slides.length === 0 && !model.heading) return <EmptyOrHint preview={preview} label="Hero — add a slide or a heading"/>;
    return <Hero slides={slides} banner={slides[0]} storeName={model.heading ?? ctx.store.name}
                 facts={[]} anchor={section.anchor ?? undefined}
                 autoplay={model.autoplay ? model.interval : false}/>;
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

export const layoutSections = sectionsFromChrome(chrome, {
    // The stage renders name-plate-only without slides; every staging maps onto it.
    hero: {classic: HeroSection, carousel: HeroSection, split: HeroSection, minimal: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
});
