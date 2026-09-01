import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import {heroModel, productsModel, type SectionRenderProps} from '@store-front/theme';
import {sectionsFromChrome, type SectionChrome} from '@store-front/ui/sections/compose';
import {cn} from '@store-front/ui/lib/utils';
import {HeadlineSheet, SlidePoster} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {PosterImage} from '../components/PosterImage';
import {SectionHeading} from '../components/SectionHeading';

/**
 * fashion's section chrome — the whole page is the wall, so every primitive is something pasted,
 * printed or stamped on the plaster: sheets, day-glo paper, strips, rubber stamps. The composer
 * supplies the structure and semantics; only the voice lives here. Hero and products stay bespoke:
 * they are the wall's identity (the selling name sheet, the poster interleave).
 */
const chrome: SectionChrome = {
    Heading: ({title, subtitle, meta}) => <SectionHeading title={title} meta={meta} action={subtitle}/>,

    // Stamps are typographic overprints; the badge's icon name is deliberately not drawn.
    Badge: ({title, body}) => (
        <span className="flex max-w-56 flex-col items-center gap-2.5 text-center">
            <span className="stamp"><bdi dir="auto">{title}</bdi></span>
            {body && <span className="max-w-[26ch] text-sm leading-relaxed text-muted-foreground"><bdi dir="auto">{body}</bdi></span>}
        </span>
    ),

    NavToken: ({label, count, href}) => (
        <Link prefetch={false} href={href} className="strip strip-hover h-11 text-sm">
            <bdi dir="auto">{label}</bdi>
            {count !== undefined && <span className="tabular-nums text-muted-foreground">{count}</span>}
        </Link>
    ),

    // The tape runs straight across the wall, like the announcement it echoes — no tilt. With
    // artwork it becomes a pasted photo strip under an ink wash, the message printed over it.
    Band: ({message, action, backgroundSrc}) => (
        <div className={cn('tape relative flex min-h-14 flex-wrap items-center justify-center gap-x-6 gap-y-2 overflow-hidden px-5 py-3 text-center text-base',
            backgroundSrc ? 'text-white' : 'glo')}>
            {backgroundSrc && (
                <>
                    <Image src={backgroundSrc} alt="" fill className="object-cover"/>
                    <span aria-hidden className="absolute inset-0 bg-black/55"/>
                </>
            )}
            <span className="relative font-display uppercase tracking-wide"><bdi dir="auto">{message}</bdi></span>
            {action && (
                <Link prefetch={false} href={action.href} className="relative underline underline-offset-4">
                    <bdi dir="auto">{action.label}</bdi>
                </Link>
            )}
        </div>
    ),

    Panel: ({children, center}) => (
        <div className={cn('sheet sheen peel', center ? 'p-6 text-center sm:p-8 [--tilt:-0.5deg]' : 'divide-y px-5 py-2 sm:px-7 [--tilt:0.3deg]')}>
            {children}
        </div>
    ),

    // The quote is set in the poster voice — a caption pasted on the wall, not body copy.
    Quote: ({quote, author}) => (
        <figure className="sheet sheen flex h-full flex-col p-5">
            <blockquote className="font-display text-xl leading-snug"><bdi dir="auto">“{quote}”</bdi></blockquote>
            {author && (
                <figcaption className="mt-auto pt-3 text-xs uppercase tracking-[0.15em] text-muted-foreground">
                    <bdi dir="auto">{author}</bdi>
                </figcaption>
            )}
        </figure>
    ),

    MediaFigure: ({src, alt, caption, href, contained}) => {
        const figure = (
            <figure className="sheet sheen peel [--tilt:0.5deg]">
                <div className="relative overflow-hidden" style={{aspectRatio: contained ? '4 / 3' : '21 / 9'}}>
                    <PosterImage src={src} alt={alt} title={caption ?? alt} tone="faint"
                                 fit={contained ? 'contain' : 'cover'} sizes="(max-width: 1024px) 100vw, 1152px"/>
                </div>
                {caption && (
                    <figcaption className="px-3 py-2 text-[0.65rem] uppercase tracking-[0.2em] text-muted-foreground">
                        <bdi dir="auto">{caption}</bdi>
                    </figcaption>
                )}
            </figure>
        );
        return href ? <Link prefetch={false} href={href} className="sheet-lift block">{figure}</Link> : figure;
    },

    BrandLabel: ({src, name, href}) => {
        const label = (
            <span className="sheet sheet-flat flex w-32 flex-col items-center gap-1.5 p-3">
                {src && (
                    <span className="relative block h-10 w-full">
                        <Image src={src} alt={name ?? ''} fill className="object-contain"/>
                    </span>
                )}
                {name && (
                    <span className="font-display text-[0.65rem] uppercase tracking-[0.18em]"><bdi dir="auto">{name}</bdi></span>
                )}
            </span>
        );
        return href ? <Link prefetch={false} href={href} className="sheet-lift block">{label}</Link> : label;
    },

    // The striped typo-poster paper sits behind the player, so a slow or blocked embed still reads
    // as a sheet on the wall rather than a blank hole.
    VideoFrame: ({player, title}) => (
        <div className="sheet sheen p-2 pb-1 [--tilt:0.4deg]">
            <div className="typo-poster relative aspect-video w-full overflow-hidden">{player}</div>
            <p className="px-1 py-1.5 text-[0.65rem] uppercase tracking-[0.2em] text-muted-foreground">
                <bdi dir="auto">{title ?? 'Video'}</bdi>
            </p>
        </div>
    ),

    PostCard: ({href, title, excerpt, imageSrc}) => (
        <Link prefetch={false} href={href} className="sheet sheen peel sheet-lift group block h-full overflow-hidden">
            {imageSrc && (
                <span className="relative block aspect-[3/2] overflow-hidden bg-muted">
                    <Image src={imageSrc} alt={title} fill className="object-cover"/>
                </span>
            )}
            <span className="block p-4">
                <span className="font-display text-lg uppercase leading-tight group-hover:underline group-hover:underline-offset-4">
                    <bdi dir="auto">{title}</bdi>
                </span>
                {excerpt && <span className="mt-1 line-clamp-2 block text-sm text-muted-foreground"><bdi dir="auto">{excerpt}</bdi></span>}
            </span>
        </Link>
    ),

    form: {
        input: 'h-11 min-w-0 flex-1 border border-foreground/45 bg-background px-3 text-sm outline-none focus-visible:border-primary focus-visible:ring-[3px] focus-visible:ring-primary/35',
        button: 'glo h-11 px-5 text-sm',
    },
    panelTitleClass: 'font-display text-2xl uppercase leading-tight',
    proseClass: 'leading-relaxed [&_p+p]:mt-3',
    summaryClass: 'font-display text-sm uppercase tracking-wide',
    quoteGridClass: 'wall grid gap-4 md:grid-cols-3',
    // long copy is a printed sheet pinned to the wall, never bare flow text on the plaster
    proseOnPanel: true,
};

function HeroWall({ctx, section}: SectionRenderProps) {
    const model = heroModel(section);
    const [first, ...rest] = model.slides;
    const storeName = ctx.store.name;
    return (
        // A wall needs margin around its sheets: even at the layout's full width the posters keep
        // gutters instead of touching the viewport edge, or the paste-up reads as a print error.
        // The wall never autoplays — every image holds its own place, so autoplay/interval are
        // deliberately not consumed here.
        <div className="mx-auto grid w-full max-w-(--container-wide) grid-cols-2 gap-3 px-4 sm:gap-4 sm:px-6 lg:grid-cols-12 lg:items-stretch">
            <HeadlineSheet ctx={ctx} heading={model.heading} subheading={model.subheading}
                           cta={model.cta} strips={model.strips}
                           className={first ? 'col-span-2 lg:col-span-5 [--tilt:-0.7deg]' : 'col-span-2 lg:col-span-12 [--tilt:-0.7deg]'}/>
            {/* grid placement lives on these wrappers: a linked poster renders inside an anchor, and
                column classes on the figure would leave the anchor an unsized grid child */}
            {first && (
                <div className="col-span-2 min-w-0 lg:col-span-7">
                    <SlidePoster slide={first} index={0} total={model.slides.length} storeName={storeName} priority
                                 ratio="16 / 10" className="h-full lg:aspect-auto [--tilt:0.8deg]"/>
                </div>
            )}
            {rest.map((slide, index) => (
                <div key={slide.id} className="col-span-1 min-w-0 lg:col-span-4">
                    <SlidePoster slide={slide} index={index + 1} total={model.slides.length} storeName={storeName}
                                 ratio="4 / 3" className={cn('h-full', index % 2 === 0 ? '[--tilt:0.6deg]' : '[--tilt:-0.8deg]')}/>
                </div>
            ))}
        </div>
    );
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const model = productsModel(section, data);
    if (model.count === 0) return null;
    const lead = section.variant === 'rail' && model.leadSrc && (
        <figure className="sheet sheen peel relative h-full min-h-64 min-w-0 overflow-hidden">
            <PosterImage src={model.leadSrc} alt={model.title ?? ''} title={ctx.store.name} tone="faint"
                         sizes="(max-width: 1024px) 100vw, 40vw"/>
        </figure>
    );
    // The interleave: a `rail` section with a lead image pastes it as the wide poster inside the
    // grid, the old wall's signature; without one, the stretch is a plain run of product sheets.
    const grid = lead ? {base: 2 as const, sm: 3, lg: 4, xl: 6} : ctx.layout.productGrid;
    return (
        <section className="min-w-0">
            {model.title && <SectionHeading title={model.title} meta={model.count}/>}
            <ProductGrid products={model.products} storeContext={ctx.storeContext} grid={grid} lead={lead || undefined}/>
        </section>
    );
}

export const layoutSections = sectionsFromChrome(chrome, {
    hero: {classic: HeroWall, carousel: HeroWall, split: HeroWall},
    products: {grid: ProductsSection, rail: ProductsSection},
});
