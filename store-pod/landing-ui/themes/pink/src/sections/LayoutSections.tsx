import Image from 'next/image';
import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import {heroModel, productsModel, slidesAsBanners, type SectionRenderProps} from '@store-front/theme';
import {sectionsFromChrome, type SectionChrome} from '@store-front/ui/sections/compose';
import {cn} from '@store-front/ui/lib/utils';
import {Hero} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * pink's section chrome — the issue. Flags print state, the contents lines number the
 * destinations, floods own whole regions, plates and cells rule the rest. The composer supplies
 * structure and semantics; the cover hero and the feature spreads stay bespoke.
 */
const chrome: SectionChrome = {
    Heading: ({title, subtitle, meta}) => (
        <SectionHeading title={title} subtitle={subtitle}
                        action={meta !== undefined ? <span className="dim figure text-sm">{meta}</span> : undefined}/>
    ),

    // Flags are printed marks; the badge's icon name is deliberately not drawn.
    Badge: ({title, body}) => (
        <span className="flex max-w-56 flex-col items-center gap-2.5 text-center">
            <span className="flag"><bdi dir="auto">{title}</bdi></span>
            {body && <span className="dim max-w-[26ch] text-sm leading-relaxed"><bdi dir="auto">{body}</bdi></span>}
        </span>
    ),

    // The issue's contents: every line carries its printed page number.
    NavToken: ({label, count, href, index}) => (
        <Link prefetch={false} href={href} className="group flex flex-wrap items-baseline gap-x-3 gap-y-1">
            <span aria-hidden className="figure text-sm">{String(index + 1).padStart(2, '0')}</span>
            <span className="display text-xl underline-offset-4 group-hover:underline sm:text-2xl"><bdi dir="auto">{label}</bdi></span>
            {count !== undefined && <span className="figure dim text-sm">{count}</span>}
        </Link>
    ),
    navLayout: 'list',

    Band: ({message, action, backgroundSrc}) => (
        <div className={cn('relative flex min-h-14 flex-wrap items-center justify-center gap-x-6 gap-y-2 overflow-hidden px-5 py-4 text-center',
            backgroundSrc ? 'text-white' : 'flood tone')}>
            {backgroundSrc && (
                <>
                    <Image src={backgroundSrc} alt="" fill className="object-cover"/>
                    <span aria-hidden className="absolute inset-0 bg-black/45"/>
                </>
            )}
            <span className="band-copy relative"><bdi dir="auto">{message}</bdi></span>
            {action && (
                <Link prefetch={false} href={action.href} className="relative text-sm font-bold underline underline-offset-4">
                    <bdi dir="auto">{action.label}</bdi>
                </Link>
            )}
        </div>
    ),

    // The featured surface is a flooded field; everything else ranges on a ruled plate.
    Panel: ({children, center}) => (
        <div className={center ? 'flood tone p-6 text-center sm:p-8' : 'plate grid-cols-1 [&>details]:cell [&>details]:border-0 [&>details]:px-4'}>
            {children}
        </div>
    ),

    Quote: ({quote, author}) => (
        <figure className="cell flex h-full flex-col p-5">
            <blockquote className="display text-xl leading-snug"><bdi dir="auto">“{quote}”</bdi></blockquote>
            {author && (
                <figcaption className="dim mt-auto pt-3 text-xs"><bdi dir="auto">{author}</bdi></figcaption>
            )}
        </figure>
    ),

    MediaFigure: ({src, alt, caption, href, contained}) => {
        const figure = (
            <figure className="hair overflow-hidden border">
                <div className={`relative w-full ${contained ? 'aspect-[4/3]' : 'aspect-[21/9]'}`}>
                    <Image src={src!} alt={alt} fill className={contained ? 'object-contain' : 'object-cover'}/>
                </div>
                {caption && (
                    <figcaption className="dim px-4 py-2 text-sm"><bdi dir="auto">{caption}</bdi></figcaption>
                )}
            </figure>
        );
        return href ? <Link prefetch={false} href={href} className="block">{figure}</Link> : figure;
    },

    BrandLabel: ({src, name, href}) => {
        const label = (
            <span className="cell flex w-32 flex-col items-center gap-1.5 p-3">
                {src && (
                    <span className="relative block h-10 w-full">
                        <Image src={src} alt={name ?? ''} fill className="object-contain"/>
                    </span>
                )}
                {name && <span className="display text-sm"><bdi dir="auto">{name}</bdi></span>}
            </span>
        );
        return href ? <Link prefetch={false} href={href} className="block">{label}</Link> : label;
    },

    VideoFrame: ({player}) => (
        <div className="cell p-2">
            <div className="relative aspect-video w-full overflow-hidden">{player}</div>
        </div>
    ),

    PostCard: ({href, title, excerpt, imageSrc}) => (
        <Link prefetch={false} href={href} className="cell group block h-full overflow-hidden">
            {imageSrc && (
                <span className="relative block aspect-[3/2] overflow-hidden bg-muted">
                    <Image src={imageSrc} alt={title} fill className="object-cover"/>
                </span>
            )}
            <span className="block p-4">
                <span className="display text-lg leading-tight group-hover:underline group-hover:underline-offset-4"><bdi dir="auto">{title}</bdi></span>
                {excerpt && <span className="dim mt-1 line-clamp-2 block text-sm"><bdi dir="auto">{excerpt}</bdi></span>}
            </span>
        </Link>
    ),

    form: {
        input: 'h-11 min-w-0 flex-1 border border-transparent bg-background px-3 text-sm text-foreground outline-none focus-visible:border-foreground',
        button: 'ink-field h-11 px-5 text-sm font-bold uppercase tracking-wide',
    },
    panelTitleClass: 'display text-2xl sm:text-3xl',
    proseClass: 'prose-issue',
    summaryClass: 'font-bold',
};

function HeroSection({ctx, section}: SectionRenderProps) {
    const model = heroModel(section);
    return <Hero slides={slidesAsBanners(section.items)} storeName={model.heading ?? ctx.store.name} lines={[]}
                 actionHref={model.cta?.href} actionLabel={model.cta?.label}
                 autoplay={model.autoplay ? model.interval : false}/>;
}

async function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const t = await getTranslations('PAGE.HOME');
    const model = productsModel(section, data);
    if (model.count === 0) return null;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {model.title && (
                <SectionHeading title={model.title} subtitle={model.subtitle}
                                action={<span className="dim figure text-sm">{t('ITEMS_COUNT', {count: model.count})}</span>}/>
            )}
            {rail
                ? <ProductRail products={model.products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={model.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

export const layoutSections = sectionsFromChrome(chrome, {
    // The cover absorbs every staging: without slides it is the type-only cover (minimal).
    hero: {classic: HeroSection, carousel: HeroSection, split: HeroSection, minimal: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
});
