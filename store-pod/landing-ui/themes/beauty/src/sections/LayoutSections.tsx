import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import {heroModel, productsModel, slidesAsBanners, type SectionRenderProps} from '@store-front/theme';
import {EmptyOrHint, sectionsFromChrome, type SectionChrome} from '@store-front/ui/sections/compose';
import {cn} from '@store-front/ui/lib/utils';
import {HeroFrame, ProductRail} from '../client';
import {ProductGrid} from '../components/ProductGrid';
import {SectionHeading} from '../components/SectionHeading';

/**
 * beauty's section chrome — the industrial lab. Structure is drawn with 1px ink plates and 45°
 * hazard stripes; the zip-tie primary is the only colour spend; state is a mark, never a tint. The
 * composer supplies structure and semantics; the hero keeps its frame plus the theme-exclusive
 * `editorial` variant — proof a theme can extend the catalogue.
 */
const chrome: SectionChrome = {
    Heading: ({title, subtitle, meta}) => <SectionHeading title={title} meta={meta} action={subtitle}/>,

    // Plates are drawn boxes; the badge's icon name is deliberately not drawn.
    Badge: ({title, body}) => (
        <span className="plate flex max-w-64 flex-col gap-1.5 px-5 py-4 text-center">
            <span className="font-display text-base uppercase tracking-wide"><bdi dir="auto">{title}</bdi></span>
            {body && <span className="max-w-[26ch] text-sm leading-relaxed text-muted-foreground"><bdi dir="auto">{body}</bdi></span>}
        </span>
    ),

    NavToken: ({label, count, href}) => (
        <Link prefetch={false} href={href}
              className="plate inline-flex items-center gap-2 px-4 py-2.5 font-display text-sm uppercase tracking-wide transition-colors hover:bg-foreground hover:text-background">
            <bdi dir="auto">{label}</bdi>
            {count !== undefined && <span className="text-xs tabular-nums opacity-70">{count}</span>}
        </Link>
    ),

    // The taped-off band: the primary field between hazard stripes; artwork rides under an ink wash.
    Band: ({message, action, backgroundSrc}) => (
        <div className={cn(backgroundSrc ? 'relative overflow-hidden text-white' : 'bg-primary text-primary-foreground')}>
            {backgroundSrc && (
                <>
                    <Image src={backgroundSrc} alt="" fill className="object-cover"/>
                    <span aria-hidden className="absolute inset-0 bg-black/55"/>
                </>
            )}
            <div aria-hidden className={cn('relative h-1.5 opacity-60', backgroundSrc ? 'hazard' : 'hazard-on-primary')}/>
            <div className="relative flex min-h-11 flex-wrap items-center justify-center gap-x-6 gap-y-2 px-5 py-3 text-center">
                <span className="font-display text-base uppercase tracking-wide"><bdi dir="auto">{message}</bdi></span>
                {action && (
                    <Link prefetch={false} href={action.href} className="text-sm underline underline-offset-4">
                        <bdi dir="auto">{action.label}</bdi>
                    </Link>
                )}
            </div>
            <div aria-hidden className={cn('relative h-1.5 opacity-60', backgroundSrc ? 'hazard' : 'hazard-on-primary')}/>
        </div>
    ),

    Panel: ({children, center}) => (
        <div className={cn('plate', center ? 'p-6 text-center sm:p-8' : 'divide-y divide-foreground px-5 py-2')}>
            {children}
        </div>
    ),

    Quote: ({quote, author}) => (
        <figure className="plate flex h-full flex-col p-5">
            <blockquote className="font-display text-lg uppercase leading-snug tracking-wide"><bdi dir="auto">“{quote}”</bdi></blockquote>
            {author && (
                <figcaption className="mt-auto pt-3 text-xs text-muted-foreground"><bdi dir="auto">{author}</bdi></figcaption>
            )}
        </figure>
    ),

    MediaFigure: ({src, alt, caption, href, contained}) => {
        const figure = (
            <figure className="plate overflow-hidden p-1">
                <div className={`relative w-full ${contained ? 'aspect-[4/3]' : 'aspect-[21/9]'}`}>
                    <Image src={src!} alt={alt} fill className={contained ? 'object-contain' : 'object-cover'}/>
                </div>
                {caption && (
                    <figcaption className="px-2 py-2 font-display text-[0.65rem] uppercase tracking-wide text-muted-foreground">
                        <bdi dir="auto">{caption}</bdi>
                    </figcaption>
                )}
            </figure>
        );
        return href ? <Link prefetch={false} href={href} className="block">{figure}</Link> : figure;
    },

    BrandLabel: ({src, name, href}) => {
        const label = (
            <span className="plate flex w-32 flex-col items-center gap-1.5 p-3">
                {src && (
                    <span className="relative block h-10 w-full">
                        <Image src={src} alt={name ?? ''} fill className="object-contain"/>
                    </span>
                )}
                {name && <span className="font-display text-[0.65rem] uppercase tracking-wide"><bdi dir="auto">{name}</bdi></span>}
            </span>
        );
        return href ? <Link prefetch={false} href={href} className="block">{label}</Link> : label;
    },

    VideoFrame: ({player}) => (
        <div className="plate p-1">
            <div className="hazard-soft relative aspect-video w-full overflow-hidden">{player}</div>
        </div>
    ),

    PostCard: ({href, title, excerpt, imageSrc}) => (
        <Link prefetch={false} href={href} className="plate group block h-full overflow-hidden">
            {imageSrc && (
                <span className="relative block aspect-[3/2] overflow-hidden bg-muted">
                    <Image src={imageSrc} alt={title} fill className="object-cover"/>
                </span>
            )}
            <span className="block p-4">
                <span className="font-display text-sm uppercase tracking-wide group-hover:underline"><bdi dir="auto">{title}</bdi></span>
                {excerpt && <span className="mt-1 line-clamp-2 block text-sm text-muted-foreground"><bdi dir="auto">{excerpt}</bdi></span>}
            </span>
        </Link>
    ),

    form: {
        input: 'h-11 min-w-0 flex-1 rounded-none border border-foreground bg-background px-3 text-sm outline-none focus-visible:border-primary',
        button: 'h-11 bg-primary px-5 font-display text-sm uppercase tracking-wide text-primary-foreground',
    },
    panelTitleClass: 'font-display text-2xl uppercase tracking-wide',
    proseClass: 'leading-relaxed [&_p+p]:mt-3',
    summaryClass: 'font-display text-sm uppercase tracking-wide',
};

function HeroSection({section, preview}: SectionRenderProps) {
    const model = heroModel(section);
    const slides = slidesAsBanners(section.items);
    if (slides.length === 0) return <EmptyOrHint preview={preview} label="Hero — add a slide"/>;
    return <HeroFrame slides={slides} autoplay={model.autoplay ? model.interval : false}/>;
}

/** Exclusive: the first slide as a full plate with the copy set beside it, magazine-opening style. */
function HeroEditorial({section}: SectionRenderProps) {
    const model = heroModel(section);
    const lead = model.slides[0];
    return (
        <div className="grid gap-6 lg:grid-cols-[3fr_2fr] lg:items-center">
            {lead?.src && (
                <div className="plate relative aspect-[4/3] overflow-hidden bg-muted">
                    <Image src={lead.src} alt={lead.heading ?? ''} fill className="object-cover"/>
                </div>
            )}
            <div className="flex flex-col gap-3">
                {model.heading && (
                    <h2 className="font-display text-3xl leading-tight lg:text-5xl"><bdi dir="auto">{model.heading}</bdi></h2>
                )}
                {model.subheading && (
                    <p className="max-w-prose text-muted-foreground"><bdi dir="auto">{model.subheading}</bdi></p>
                )}
                {model.cta && (
                    <Link prefetch={false} href={model.cta.href}
                          className="mt-2 inline-flex w-fit bg-primary px-5 py-3 font-display text-sm uppercase tracking-wide text-primary-foreground">
                        <bdi dir="auto">{model.cta.label}</bdi>
                    </Link>
                )}
            </div>
        </div>
    );
}

function ProductsSection({ctx, section, data, preview}: SectionRenderProps) {
    const model = productsModel(section, data);
    if (model.count === 0) return <EmptyOrHint preview={preview} label="Products — pick a source that has products"/>;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {model.title && <SectionHeading title={model.title} meta={model.count}/>}
            {rail
                ? <ProductRail products={model.products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={model.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

export const layoutSections = sectionsFromChrome(chrome, {
    // split/minimal ride the editorial plate: image beside copy when there is one, the drawn
    // type column when there is not.
    hero: {classic: HeroSection, carousel: HeroSection, split: HeroEditorial, minimal: HeroEditorial, editorial: HeroEditorial},
    products: {rail: ProductsSection, grid: ProductsSection},
});
