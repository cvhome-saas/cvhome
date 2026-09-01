import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import {productsModel, slidesAsBanners, type SectionRenderProps} from '@store-front/theme';
import {sectionsFromChrome, type SectionChrome} from '@store-front/ui/sections/compose';
import {cn} from '@store-front/ui/lib/utils';
import {Masthead} from './Masthead';
import {ProductGrid} from '../components/ProductGrid';
import {SectionHeading} from '../components/SectionHeading';

/**
 * hunger's section chrome — the letterbox menu. Everything prints in ink and the plate: marks for
 * state, section bands for destinations, ruled lines for copy, a dashed coupon for the featured
 * surface. Zero radius, no carousels. The composer supplies structure and semantics; the masthead
 * and the menu's dish lines stay bespoke.
 */
const chrome: SectionChrome = {
    Heading: ({title, subtitle, meta}) => (
        <SectionHeading title={title} subtitle={subtitle}
                        action={meta !== undefined ? <span className="price text-sm">{meta}</span> : undefined}/>
    ),

    // Marks are printed outlines; the badge's icon name is deliberately not drawn.
    Badge: ({title, body}) => (
        <span className="flex max-w-56 flex-col items-center gap-2.5 text-center">
            <span className="mark"><bdi dir="auto">{title}</bdi></span>
            {body && <span className="max-w-[26ch] text-sm leading-relaxed text-muted-foreground"><bdi dir="auto">{body}</bdi></span>}
        </span>
    ),

    // Each destination is a section band of the menu, its count in the price column.
    NavToken: ({label, count, href}) => (
        <Link prefetch={false} href={href}
              className="plate flex items-baseline justify-between gap-4 px-3 py-2 transition-opacity hover:opacity-90">
            <span className="press text-lg"><bdi dir="auto">{label}</bdi></span>
            {count !== undefined && <span className="price text-sm">{count}</span>}
        </Link>
    ),
    navLayout: 'list',

    Band: ({message, action, backgroundSrc}) => (
        <div className={cn('relative flex min-h-12 flex-wrap items-center justify-center gap-x-6 gap-y-2 overflow-hidden px-5 py-3 text-center',
            backgroundSrc ? 'text-white' : 'plate')}>
            {backgroundSrc && (
                <>
                    <Image src={backgroundSrc} alt="" fill className="object-cover"/>
                    <span aria-hidden className="absolute inset-0 bg-black/55"/>
                </>
            )}
            <span className="press relative text-lg"><bdi dir="auto">{message}</bdi></span>
            {action && (
                <Link prefetch={false} href={action.href} className="relative text-sm font-bold underline underline-offset-4">
                    <bdi dir="auto">{action.label}</bdi>
                </Link>
            )}
        </div>
    ),

    // The featured surface is the clip-out coupon; everything else prints between ruled lines.
    Panel: ({children, center}) => (
        <div className={center
            ? 'border-2 border-dashed border-foreground/50 p-6 text-center sm:p-8'
            : 'divide-y divide-foreground/25 border-y border-foreground/25'}>
            {children}
        </div>
    ),

    Quote: ({quote, author}) => (
        <figure className="flex h-full flex-col border border-foreground/25 p-5">
            <blockquote className="press text-lg leading-snug"><bdi dir="auto">“{quote}”</bdi></blockquote>
            {author && (
                <figcaption className="price mt-auto pt-3 text-xs"><bdi dir="auto">{author}</bdi></figcaption>
            )}
        </figure>
    ),

    MediaFigure: ({src, alt, caption, href, contained}) => {
        const figure = (
            <figure className="border border-foreground/25 p-1">
                <div className={`relative w-full ${contained ? 'aspect-[4/3]' : 'aspect-[21/9]'}`}>
                    <Image src={src!} alt={alt} fill className={contained ? 'object-contain' : 'object-cover'}/>
                </div>
                {caption && (
                    <figcaption className="press px-2 py-1.5 text-xs"><bdi dir="auto">{caption}</bdi></figcaption>
                )}
            </figure>
        );
        return href ? <Link prefetch={false} href={href} className="block">{figure}</Link> : figure;
    },

    BrandLabel: ({src, name, href}) => {
        const label = (
            <span className="flex w-32 flex-col items-center gap-1.5 border border-foreground/25 p-3">
                {src && (
                    <span className="relative block h-10 w-full">
                        <Image src={src} alt={name ?? ''} fill className="object-contain"/>
                    </span>
                )}
                {name && <span className="press text-xs"><bdi dir="auto">{name}</bdi></span>}
            </span>
        );
        return href ? <Link prefetch={false} href={href} className="block">{label}</Link> : label;
    },

    VideoFrame: ({player}) => (
        <div className="border border-foreground/25 p-1">
            <div className="relative aspect-video w-full overflow-hidden">{player}</div>
        </div>
    ),

    PostCard: ({href, title, excerpt, imageSrc}) => (
        <Link prefetch={false} href={href} className="group block h-full border border-foreground/25">
            {imageSrc && (
                <span className="relative block aspect-[3/2] overflow-hidden bg-muted">
                    <Image src={imageSrc} alt={title} fill className="object-cover"/>
                </span>
            )}
            <span className="block p-4">
                <span className="press text-base group-hover:underline"><bdi dir="auto">{title}</bdi></span>
                {excerpt && <span className="mt-1 line-clamp-2 block text-sm text-muted-foreground"><bdi dir="auto">{excerpt}</bdi></span>}
            </span>
        </Link>
    ),

    form: {
        input: 'h-11 min-w-0 flex-1 rounded-none border border-foreground/45 bg-background px-3 text-sm outline-none focus-visible:border-foreground',
        button: 'fold px-5 text-sm',
    },
    panelTitleClass: 'press text-2xl',
    proseClass: 'prose-hunger',
    summaryClass: 'press text-base',
};

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
    const model = productsModel(section, data);
    if (model.count === 0) return null;
    return (
        <section className="min-w-0">
            {model.title && <SectionHeading title={model.title} subtitle={model.subtitle}/>}
            {/* A menu does not hide its dishes behind arrows: `rail` sets the dense dish-line list,
                `grid` the larger board face. */}
            <ProductGrid products={model.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}
                         variant={section.variant === 'rail' ? 'line' : 'board'}/>
        </section>
    );
}

export const layoutSections = sectionsFromChrome(chrome, {
    // The masthead is already the menu's minimal print; every staging maps onto it.
    hero: {classic: HeroSection, carousel: HeroSection, split: HeroSection, minimal: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
});
