import Image from 'next/image';
import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import {heroModel, productsModel, slidesAsBanners, type SectionRenderProps} from '@store-front/theme';
import {sectionsFromChrome, type SectionChrome} from '@store-front/ui/sections/compose';
import {cn} from '@store-front/ui/lib/utils';
import {Hero} from './Hero';
import {DirectoryBoard, type Department} from '../components/DirectoryBoard';
import {floors} from '../components/floors';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * furniture's section chrome — the home floor directory. State plates print state, enamel fields
 * carry the signs, brass hairlines rule the plates. The composer supplies structure and semantics;
 * the window hero, the numbered plates and the directory board stay bespoke.
 */
const chrome: SectionChrome = {
    Heading: ({title, subtitle, meta}) => <SectionHeading title={title} meta={meta} action={subtitle}/>,

    // State plates are printed words; the badge's icon name is deliberately not drawn.
    Badge: ({title, body}) => (
        <span className="flex max-w-56 flex-col items-center gap-2.5 text-center">
            <span className="state-plate"><bdi dir="auto">{title}</bdi></span>
            {body && <span className="max-w-[26ch] text-sm leading-relaxed text-muted-foreground"><bdi dir="auto">{body}</bdi></span>}
        </span>
    ),

    NavToken: ({label, count, href}) => (
        <Link prefetch={false} href={href}
              className="sign inline-flex items-center gap-2 rounded-(--r-control) border px-4 py-2.5 text-xs transition-colors hover:bg-secondary">
            <bdi dir="auto">{label}</bdi>
            {count !== undefined && <span className="figure text-muted-foreground">{count}</span>}
        </Link>
    ),

    Band: ({message, action, backgroundSrc}) => (
        <div className={cn('relative flex min-h-14 flex-wrap items-center justify-center gap-x-6 gap-y-2 overflow-hidden px-5 py-4 text-center',
            backgroundSrc ? 'rounded-(--r-card) text-white' : 'enamel-flat')}>
            {backgroundSrc && (
                <>
                    <Image src={backgroundSrc} alt="" fill className="object-cover"/>
                    <span aria-hidden className="absolute inset-0 bg-black/50"/>
                </>
            )}
            <span className="sign relative text-base"><bdi dir="auto">{message}</bdi></span>
            {action && (
                <Link prefetch={false} href={action.href} className="relative text-sm font-semibold underline underline-offset-4">
                    <bdi dir="auto">{action.label}</bdi>
                </Link>
            )}
        </div>
    ),

    // The featured surface is the enamel field; everything else is a brass-ruled plate.
    Panel: ({children, center}) => (
        <div className={center
            ? 'enamel-flat p-6 text-center sm:p-8'
            : 'rule-brass divide-y rounded-(--r-card) border px-5 py-2 [&>details]:border-inherit'}>
            {children}
        </div>
    ),

    Quote: ({quote, author}) => (
        <figure className="rule-brass flex h-full flex-col rounded-(--r-card) border p-5">
            <blockquote className="copy text-base leading-relaxed"><bdi dir="auto">“{quote}”</bdi></blockquote>
            {author && (
                <figcaption className="sign mt-auto pt-3 text-xs text-muted-foreground"><bdi dir="auto">{author}</bdi></figcaption>
            )}
        </figure>
    ),

    MediaFigure: ({src, alt, caption, href, contained}) => {
        const figure = (
            <figure className="rule-brass overflow-hidden rounded-(--r-card) border p-1">
                <div className={`relative w-full ${contained ? 'aspect-[4/3]' : 'aspect-[21/9]'}`}>
                    <Image src={src!} alt={alt} fill className={contained ? 'object-contain' : 'object-cover'}/>
                </div>
                {caption && (
                    <figcaption className="sign px-2 py-2 text-[0.65rem] text-muted-foreground"><bdi dir="auto">{caption}</bdi></figcaption>
                )}
            </figure>
        );
        return href ? <Link prefetch={false} href={href} className="block">{figure}</Link> : figure;
    },

    BrandLabel: ({src, name, href}) => {
        const label = (
            <span className="rule-brass flex w-32 flex-col items-center gap-1.5 rounded-(--r-card) border p-3">
                {src && (
                    <span className="relative block h-10 w-full">
                        <Image src={src} alt={name ?? ''} fill className="object-contain"/>
                    </span>
                )}
                {name && <span className="sign text-[0.65rem]"><bdi dir="auto">{name}</bdi></span>}
            </span>
        );
        return href ? <Link prefetch={false} href={href} className="block">{label}</Link> : label;
    },

    VideoFrame: ({player}) => (
        <div className="rule-brass rounded-(--r-card) border p-1">
            <div className="relative aspect-video w-full overflow-hidden rounded-[calc(var(--r-card)-4px)]">{player}</div>
        </div>
    ),

    PostCard: ({href, title, excerpt, imageSrc}) => (
        <Link prefetch={false} href={href} className="rule-brass group block h-full overflow-hidden rounded-(--r-card) border">
            {imageSrc && (
                <span className="relative block aspect-[3/2] overflow-hidden bg-muted">
                    <Image src={imageSrc} alt={title} fill className="object-cover"/>
                </span>
            )}
            <span className="block p-4">
                <span className="sign text-sm group-hover:underline"><bdi dir="auto">{title}</bdi></span>
                {excerpt && <span className="copy mt-1 line-clamp-2 block text-sm text-muted-foreground"><bdi dir="auto">{excerpt}</bdi></span>}
            </span>
        </Link>
    ),

    form: {
        input: 'h-11 min-w-0 flex-1 rounded-(--r-control) border border-transparent bg-background px-3 text-sm text-foreground outline-none focus-visible:border-foreground',
        button: 'sign h-11 rounded-(--r-control) bg-background px-5 text-xs text-foreground shadow-sm transition-transform duration-(--motion-fast) hover:translate-y-px',
    },
    panelTitleClass: 'sign text-xl sm:text-2xl',
    proseClass: 'copy',
    summaryClass: 'sign text-sm',
};

async function HeroSection({ctx, section}: SectionRenderProps) {
    const tc = await getTranslations('COMMON');
    const model = heroModel(section);
    return (
        <div className="min-h-[15rem]">
            <Hero slides={slidesAsBanners(section.items)} caption={model.heading ?? ctx.store.name}
                  planCaption={tc('DEPARTMENT')}/>
        </div>
    );
}

async function CategoriesSection({ctx, section, data}: SectionRenderProps) {
    const tc = await getTranslations('COMMON');
    const t = await getTranslations('PAGE.HOME');
    const departments: Department[] = floors(data?.categories ? [...data.categories] : undefined)
        .map(c => ({code: c.code, name: c.description.name, href: `/category/${c.description.friendlyUrl}`, count: c.productCount ?? 0}));
    if (departments.length === 0) return null;
    const {store} = ctx;
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : NaN;
    const facts = [store.address?.city, Number.isNaN(year) ? undefined : t('SINCE', {year})].filter(Boolean) as string[];
    return (
        <DirectoryBoard
            title={section.text.title ?? store.name}
            facts={facts}
            departments={departments}
            headings={{floor: tc('FLOOR'), department: tc('DEPARTMENT'), items: tc('ITEMS')}}
            className="min-w-0"/>
    );
}

async function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const t = await getTranslations('PAGE.HOME');
    const model = productsModel(section, data);
    if (model.count === 0) return null;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {model.title && <SectionHeading title={model.title} meta={t('ITEMS_COUNT', {count: model.count})}/>}
            {rail
                ? <ProductRail products={model.products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={model.products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

export const layoutSections = sectionsFromChrome(chrome, {
    // The window shows the drawn plate without slides — the directory's own minimal.
    hero: {classic: HeroSection, carousel: HeroSection, split: HeroSection, minimal: HeroSection},
    categories: {grid: CategoriesSection, pills: CategoriesSection},
    products: {rail: ProductsSection, grid: ProductsSection},
});
