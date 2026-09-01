import type {ReactNode} from 'react';
import {
    brandsModel, categoriesModel, faqModel, imageModel, newsletterModel, postsModel, promoModel,
    richtextModel, testimonialsModel, uspModel, videoModel,
    type BrandModel, type ImageModel, type PostCardModel, type PromoModel, type SectionAction,
    type SectionRenderProps, type ThemeSectionRegistry,
} from '@store-front/theme';
import {getTranslations} from 'next-intl/server';
import {NewsletterForm} from './newsletter-form';

/**
 * The section composer: ONE implementation of every composable section kind, built from the shared
 * section models and a theme's `SectionChrome` primitives. Structure, empty behavior, field
 * support, `<bdi>` isolation and page rhythm are decided here once; a theme contributes only its
 * voice. The shell fallbacks are the same composer with a neutral chrome — so a theme can never
 * drift from the fallback semantics, only re-skin them.
 *
 * `hero` and `products` are deliberately NOT composed: they are a theme's identity pieces and come
 * in through `overrides` — but they consume `heroModel`/`productsModel`, keeping their semantics
 * (CTA dedupe, height, autoplay, lead image) centralized too.
 */
export interface SectionChrome {
    /** The theme's section heading (usually wraps its own SectionHeading component). */
    Heading(props: {title: ReactNode; subtitle?: ReactNode; meta?: ReactNode}): ReactNode;
    /** One trust badge: stamp / sticker / flag / mark / state plate / chip. May deliberately not
     *  draw `icon` when the theme's badges are typographic. */
    Badge(props: {icon: string; title: string; body?: string}): ReactNode;
    /** One navigation token: strip / aisle tile / contents line / menu band / index tab.
     *  `index` is the token's position for chromes that number their lines. */
    NavToken(props: {label: string; count?: number; href: string; index: number}): ReactNode;
    /** The promo band across the page. `backgroundSrc` is the merchant's optional artwork. */
    Band(props: PromoModel & {message: string}): ReactNode;
    /** The theme's boxed surface: sheet / crate / plate / enamel field / bordered card. */
    Panel(props: {children: ReactNode; center?: boolean}): ReactNode;
    /** One testimonial, set in the theme's display voice on its own surface. */
    Quote(props: {quote: string; author?: string}): ReactNode;
    /** The themed image treatment — must honor src, alt, caption and link. */
    MediaFigure(props: ImageModel): ReactNode;
    /** One brand label; must render `name` so a label reads even without artwork. */
    BrandLabel(props: BrandModel): ReactNode;
    /** The paper around the video player; must keep the player visible and the title readable. */
    VideoFrame(props: {player: ReactNode; title?: string}): ReactNode;
    /** One blog post card. */
    PostCard(props: PostCardModel): ReactNode;
    /** Class strings for the shared newsletter form controls. */
    form: {input: string; button: string};
    /** The heading set inside a Panel (newsletter): the theme's display voice. */
    panelTitleClass: string;
    /** The theme's long-copy voice (prose-*). */
    proseClass: string;
    /** The FAQ question's type voice. */
    summaryClass: string;
    /** How nav tokens flow: a pasted/wrapping row (default) or a vertical contents list. */
    navLayout?: 'wrap' | 'list';
    /** Container for testimonial quotes; defaults to a three-up grid. */
    quoteGridClass?: string;
    /** Long copy sits on a Panel (fashion's printed sheet) instead of open flow. */
    proseOnPanel?: boolean;
}

/** What an empty section renders: nothing live, a labelled hint inside the builder's canvas. */
export function EmptyOrHint({preview, label}: {preview: boolean; label: string}) {
    if (!preview) return null;
    return (
        <div className="flex min-h-24 items-center justify-center rounded-md border border-dashed border-muted-foreground/40 p-6 text-sm text-muted-foreground">
            {label}
        </div>
    );
}

type Renderer = (props: SectionRenderProps) => ReactNode;

const variants = (ids: string[], renderer: Renderer): Record<string, Renderer> =>
    Object.fromEntries(ids.map(id => [id, renderer]));

export function sectionsFromChrome(chrome: SectionChrome,
                                   overrides: ThemeSectionRegistry = {}): ThemeSectionRegistry {
    const {Heading, Badge, NavToken, Band, Panel, Quote, MediaFigure, BrandLabel, VideoFrame, PostCard} = chrome;

    const Usp: Renderer = ({section, preview}) => {
        const badges = uspModel(section);
        if (badges.length === 0) return <EmptyOrHint preview={preview} label="Trust badges — add one"/>;
        // Even columns, not a floating cluster: the row reads as a designed band at every count.
        const columns = badges.length >= 4 ? 'sm:grid-cols-2 lg:grid-cols-4'
            : badges.length === 3 ? 'sm:grid-cols-3' : 'sm:grid-cols-2';
        return (
            <section className="min-w-0">
                {section.text.title && <Heading title={<bdi dir="auto">{section.text.title}</bdi>}/>}
                <ul className={`mx-auto grid max-w-4xl grid-cols-1 justify-items-center gap-x-8 gap-y-8 py-2 ${columns}`}>
                    {badges.map(badge => <li key={badge.id} className="flex w-full min-w-0 justify-center">{Badge(badge)}</li>)}
                </ul>
            </section>
        );
    };

    const Categories: Renderer = ({section, data, preview}) => {
        const links = categoriesModel(data);
        if (links.length === 0) return <EmptyOrHint preview={preview} label="Categories — none to show yet"/>;
        const list = chrome.navLayout === 'list';
        return (
            <section className={list ? 'mx-auto min-w-0 max-w-2xl' : 'min-w-0'}>
                {section.text.title && <Heading title={<bdi dir="auto">{section.text.title}</bdi>}/>}
                <ul className={list ? 'flex flex-col gap-2.5' : 'flex flex-wrap gap-3'}>
                    {links.map((link, index) => (
                        <li key={link.id} className="min-w-0">
                            {NavToken({label: link.name, count: link.count, href: link.href, index})}
                        </li>
                    ))}
                </ul>
            </section>
        );
    };

    const Promo: Renderer = ({section, preview}) => {
        const model = promoModel(section);
        if (!model.message) return <EmptyOrHint preview={preview} label="Promo — write the message"/>;
        return <>{Band({...model, message: model.message})}</>;
    };

    const Faq: Renderer = ({section, data, preview}) => {
        const entries = faqModel(section, data);
        if (entries.length === 0) return <EmptyOrHint preview={preview} label="FAQ — no published questions in this group"/>;
        return (
            <section className="mx-auto min-w-0 max-w-2xl">
                {section.text.title && <Heading title={<bdi dir="auto">{section.text.title}</bdi>}/>}
                <Panel>
                    {entries.map((entry, index) => (
                        <details key={index} className="group py-3">
                            <summary className={`flex cursor-pointer list-none items-baseline justify-between gap-3 marker:hidden [&::-webkit-details-marker]:hidden ${chrome.summaryClass}`}>
                                <bdi dir="auto">{entry.question}</bdi>
                                <span aria-hidden className="text-muted-foreground transition-transform group-open:rotate-45">+</span>
                            </summary>
                            <div className={`pt-2 text-sm text-muted-foreground ${chrome.proseClass}`}
                                 dangerouslySetInnerHTML={{__html: entry.answerHtml}}/>
                        </details>
                    ))}
                </Panel>
            </section>
        );
    };

    const Newsletter = async ({section}: SectionRenderProps) => {
        const t = await getTranslations('COMPONENTS.NEWSLETTER');
        const model = newsletterModel(section);
        const copy = (
            <>
                <h2 className={chrome.panelTitleClass}><bdi dir="auto">{model.heading ?? t('HEADING')}</bdi></h2>
                {/* inherits the surface's ink: a featured surface may be a primary field, where the
                    muted role would fall under contrast */}
                {model.body && <p className="mt-1 text-sm opacity-85"><bdi dir="auto">{model.body}</bdi></p>}
                <NewsletterForm cta={model.cta} inputClassName={chrome.form.input} buttonClassName={chrome.form.button}/>
            </>
        );
        // boxed sits on the theme's panel surface; inline is the same copy set open on the page
        return (
            <div className="mx-auto min-w-0 max-w-xl">
                {model.boxed ? <Panel center>{copy}</Panel> : <div className="text-center">{copy}</div>}
            </div>
        );
    };

    const RichText: Renderer = ({section, preview}) => {
        const model = richtextModel(section);
        if (!model.html && !model.title) return <EmptyOrHint preview={preview} label="Rich text — write something"/>;
        const body = (
            <>
                {model.title && <Heading title={<bdi dir="auto">{model.title}</bdi>}/>}
                {model.html && (
                    // CMS-authored HTML, sanitized by the content service on write.
                    <div className={`text-sm [&_a]:underline [&_a]:underline-offset-4 ${chrome.proseClass}`}
                         dangerouslySetInnerHTML={{__html: model.html}}/>
                )}
            </>
        );
        const align = model.centered ? 'mx-auto max-w-prose text-center' : 'max-w-prose';
        return chrome.proseOnPanel
            ? <div className={align}><Panel center={model.centered}>{body}</Panel></div>
            : <div className={align}>{body}</div>;
    };

    const Testimonials: Renderer = ({section, preview}) => {
        const quotes = testimonialsModel(section);
        if (quotes.length === 0) return <EmptyOrHint preview={preview} label="Testimonials — add a quote"/>;
        return (
            <section className="min-w-0">
                {section.text.title && <Heading title={<bdi dir="auto">{section.text.title}</bdi>}/>}
                <div className={chrome.quoteGridClass ?? 'grid gap-4 md:grid-cols-3'}>
                    {quotes.map(quote => <div key={quote.id} className="min-w-0">{Quote(quote)}</div>)}
                </div>
            </section>
        );
    };

    const Brands: Renderer = ({section, preview}) => {
        const brands = brandsModel(section);
        if (brands.length === 0) return <EmptyOrHint preview={preview} label="Brand logos — add one"/>;
        return (
            <section className="min-w-0">
                {section.text.title && <Heading title={<bdi dir="auto">{section.text.title}</bdi>}/>}
                <div className="flex flex-wrap items-center justify-center gap-4">
                    {brands.map(brand => <span key={brand.id} className="min-w-0">{BrandLabel(brand)}</span>)}
                </div>
            </section>
        );
    };

    const ImageSection: Renderer = ({section, preview}) => {
        const model = imageModel(section);
        if (!model.src) return <EmptyOrHint preview={preview} label="Image — pick one from the media library"/>;
        return <>{MediaFigure(model)}</>;
    };

    const Video: Renderer = ({section, preview}) => {
        const model = videoModel(section);
        if (!model.embedSrc) return <EmptyOrHint preview={preview} label="Video — paste a YouTube or Vimeo link"/>;
        const player = (
            <iframe src={model.embedSrc} title={model.title ?? 'Video'} className="absolute inset-0 size-full"
                    allow="accelerometer; encrypted-media; picture-in-picture" allowFullScreen
                    loading="lazy" referrerPolicy="no-referrer"/>
        );
        return (
            <section className="mx-auto min-w-0 max-w-3xl">
                {model.title && <Heading title={<bdi dir="auto">{model.title}</bdi>}/>}
                {VideoFrame({player, title: model.title})}
            </section>
        );
    };

    const Posts: Renderer = ({section, data, preview}) => {
        const posts = postsModel(data);
        if (posts.length === 0) return <EmptyOrHint preview={preview} label="Blog posts — nothing published yet"/>;
        return (
            <section className="min-w-0">
                {section.text.title && <Heading title={<bdi dir="auto">{section.text.title}</bdi>}/>}
                <div className="grid gap-6 sm:grid-cols-2 md:grid-cols-3">
                    {posts.map(post => <div key={post.id} className="min-w-0">{PostCard(post)}</div>)}
                </div>
            </section>
        );
    };

    return {
        usp: variants(['row'], Usp),
        categories: variants(['grid', 'pills'], Categories),
        promo: variants(['strip', 'card'], Promo),
        faq: variants(['accordion'], Faq),
        newsletter: variants(['inline', 'boxed'], Newsletter),
        richtext: variants(['default', 'centered'], RichText),
        testimonials: variants(['cards', 'quotes'], Testimonials),
        brands: variants(['row'], Brands),
        image: variants(['full', 'contained'], ImageSection),
        video: variants(['embed'], Video),
        posts: variants(['cards'], Posts),
        ...overrides,
    };
}

export type {SectionAction};
