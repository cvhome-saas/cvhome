import Image from 'next/image';
import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import {heroModel, productsModel, slidesAsBanners, type SectionRenderProps} from '@store-front/theme';
import {EmptyOrHint, sectionsFromChrome, type SectionChrome} from '@store-front/ui/sections/compose';
import {cn} from '@store-front/ui/lib/utils';
import {Hero, ProductRail} from '../client';
import {ProductGrid} from '../components/ProductGrid';
import {SectionHeading} from '../components/SectionHeading';

/**
 * grocery's section chrome — the warehouse floor. Stickers print state, aisle tiles hang the
 * destinations, the price board is the featured surface, crates box everything else. The composer
 * supplies structure and semantics; the hero (entrance board) and products (aisles) stay bespoke.
 */
const chrome: SectionChrome = {
    Heading: ({title, subtitle, meta}) => <SectionHeading title={title} subtitle={subtitle} meta={meta}/>,

    // Stickers are typographic prints; the badge's icon name is deliberately not drawn.
    Badge: ({title, body}) => (
        <span className="flex max-w-56 flex-col items-center gap-2.5 text-center">
            <span className="sticker sticker-outline"><bdi dir="auto">{title}</bdi></span>
            {body && <span className="max-w-[26ch] text-sm leading-relaxed text-muted-foreground"><bdi dir="auto">{body}</bdi></span>}
        </span>
    ),

    NavToken: ({label, count, href}) => (
        <Link prefetch={false} href={href} className="aisle-tile">
            <bdi dir="auto">{label}</bdi>
            {count !== undefined && <span className="count">{count}</span>}
        </Link>
    ),

    Band: ({message, action, backgroundSrc}) => (
        <div className={cn('relative flex min-h-14 flex-wrap items-center justify-center gap-x-6 gap-y-2 overflow-hidden px-5 py-4 text-center',
            backgroundSrc ? 'rounded-(--r-card) text-white' : 'board')}>
            {backgroundSrc && (
                <>
                    <Image src={backgroundSrc} alt="" fill className="object-cover"/>
                    <span aria-hidden className="absolute inset-0 bg-black/50"/>
                </>
            )}
            <span className="signage relative text-lg uppercase"><bdi dir="auto">{message}</bdi></span>
            {action && (
                <Link prefetch={false} href={action.href} className="relative text-sm font-semibold underline underline-offset-4">
                    <bdi dir="auto">{action.label}</bdi>
                </Link>
            )}
        </div>
    ),

    // The featured surface (centered panels like the newsletter coupon) is the price board itself;
    // everything else ships in a crate.
    Panel: ({children, center}) => (
        <div className={center ? 'board p-6 text-center sm:p-8' : 'crate divide-y divide-(--line) px-5 py-2'}>
            {children}
        </div>
    ),

    Quote: ({quote, author}) => (
        <figure className="crate flex h-full flex-col p-5">
            <blockquote className="text-sm leading-relaxed"><bdi dir="auto">“{quote}”</bdi></blockquote>
            {author && (
                <figcaption className="mt-auto pt-3"><span className="sticker sticker-outline text-[0.6rem]"><bdi dir="auto">{author}</bdi></span></figcaption>
            )}
        </figure>
    ),

    MediaFigure: ({src, alt, caption, href, contained}) => {
        const figure = (
            <figure className="crate overflow-hidden">
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
            <span className="crate flex w-32 flex-col items-center gap-1.5 p-3">
                {src && (
                    <span className="relative block h-10 w-full">
                        <Image src={src} alt={name ?? ''} fill className="object-contain"/>
                    </span>
                )}
                {name && <span className="text-xs font-semibold uppercase"><bdi dir="auto">{name}</bdi></span>}
            </span>
        );
        return href ? <Link prefetch={false} href={href} className="block">{label}</Link> : label;
    },

    VideoFrame: ({player}) => (
        <div className="crate p-2">
            <div className="relative aspect-video w-full overflow-hidden">{player}</div>
        </div>
    ),

    PostCard: ({href, title, excerpt, imageSrc}) => (
        <Link prefetch={false} href={href} className="crate group block h-full overflow-hidden">
            {imageSrc && (
                <span className="relative block aspect-[3/2] overflow-hidden bg-muted">
                    <Image src={imageSrc} alt={title} fill className="object-cover"/>
                </span>
            )}
            <span className="block p-4">
                <span className="signage text-base uppercase group-hover:underline"><bdi dir="auto">{title}</bdi></span>
                {excerpt && <span className="mt-1 line-clamp-2 block text-sm text-muted-foreground"><bdi dir="auto">{excerpt}</bdi></span>}
            </span>
        </Link>
    ),

    form: {
        input: 'h-11 min-w-0 flex-1 rounded-(--r-control) border-2 border-transparent bg-background px-3 text-sm text-foreground outline-none focus-visible:border-foreground',
        button: 'h-11 rounded-(--r-control) bg-foreground px-5 text-sm font-bold uppercase tracking-wide text-background',
    },
    panelTitleClass: 'signage text-2xl uppercase',
    proseClass: 'prose-grocery',
    summaryClass: 'font-semibold',
};

async function HeroSection({ctx, section}: SectionRenderProps) {
    const t = await getTranslations('PAGE.HOME');
    const {store} = ctx;
    const model = heroModel(section);
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : NaN;
    const facts = [store.address?.city, Number.isNaN(year) ? undefined : t('SINCE', {year})].filter(Boolean) as string[];
    return <Hero slides={slidesAsBanners(section.items)} storeName={model.heading ?? store.name} facts={facts}
                 cta={model.cta} autoplay={model.autoplay ? model.interval : false}/>;
}

function ProductsSection({ctx, section, data, preview}: SectionRenderProps) {
    const model = productsModel(section, data);
    if (model.count === 0) return <EmptyOrHint preview={preview} label="Products — pick a source that has products"/>;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {model.title && <SectionHeading title={model.title} subtitle={model.subtitle} meta={model.count}/>}
            {rail
                ? <ProductRail products={model.products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={model.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

export const layoutSections = sectionsFromChrome(chrome, {
    // The entrance board handles every staging: no slides = the price board alone (minimal).
    hero: {classic: HeroSection, carousel: HeroSection, split: HeroSection, minimal: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
});
