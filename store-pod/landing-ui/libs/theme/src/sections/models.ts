import type {Category, LayoutSectionData, Product, SectionItem} from '@store-front/types';
import type {SectionResolvedData} from '../contract';
import {linkHref} from './links';

/**
 * Section models: the one place that reads a section's props/text/data and applies its semantics.
 * Every renderer — shell fallback or theme — consumes these, so every field the catalogue declares
 * is honored in exactly one implementation, and rules like CTA dedupe or empty-collapse cannot
 * drift between themes. Models are pure and unit-tested; anything visual stays out of them.
 */

export interface SectionAction {
    label: string;
    href: string;
}

const text = (value: string | undefined | null): string | undefined => {
    const trimmed = value?.trim();
    return trimmed ? trimmed : undefined;
};

export const mediaUrl = (props: Record<string, unknown> | undefined): string | undefined =>
    typeof props?.mediaUrl === 'string' ? props.mediaUrl : undefined;

export const num = (value: unknown, fallback: number): number =>
    typeof value === 'number' && Number.isFinite(value) ? value : fallback;

const action = (label: string | undefined | null, link: unknown): SectionAction | undefined => {
    const clean = text(label);
    const href = linkHref(link);
    return clean && href !== '#' ? {label: clean, href} : undefined;
};

export const sectionItems = (section: LayoutSectionData): SectionItem[] => section.items ?? [];

// ------------------------------------------------------------------------------------------- hero

export interface HeroSlide {
    id: string;
    src?: string;
    heading?: string;
    subheading?: string;
    cta?: string;
    href?: string;
}

export interface HeroModel {
    heading?: string;
    subheading?: string;
    /** The section's own CTA — the primary action. */
    cta?: SectionAction;
    height: 'sm' | 'md' | 'lg';
    autoplay: boolean;
    /** Seconds between slides when autoplaying. */
    interval: number;
    slides: HeroSlide[];
    /**
     * The slides' own CTAs as secondary actions, deduped by label (case-insensitive) against the
     * primary CTA and each other — the same label never appears twice in one hero.
     */
    strips: SectionAction[];
}

export function heroModel(section: LayoutSectionData): HeroModel {
    const cta = action(section.text.cta, section.props.link);
    const slides: HeroSlide[] = sectionItems(section).map(item => {
        const href = linkHref(item.props.link);
        return {
            id: item.id,
            src: mediaUrl(item.props),
            heading: text(item.text.heading),
            subheading: text(item.text.subheading),
            cta: text(item.text.cta),
            href: href === '#' ? undefined : href,
        };
    });
    const taken = new Set([cta?.label.toLowerCase() ?? '']);
    const strips: SectionAction[] = [];
    for (const slide of slides) {
        if (!slide.cta || !slide.href || taken.has(slide.cta.toLowerCase())) continue;
        taken.add(slide.cta.toLowerCase());
        strips.push({label: slide.cta, href: slide.href});
    }
    const height = section.props.height;
    return {
        heading: text(section.text.heading),
        subheading: text(section.text.subheading),
        cta,
        height: height === 'sm' || height === 'lg' ? height : 'md',
        autoplay: section.props.autoplay !== false,
        // clamped to the catalogue's declared range: a stored 0 must not become a strobing hero
        interval: Math.min(12, Math.max(2, num(section.props.interval, 5))),
        slides,
        strips,
    };
}

// --------------------------------------------------------------------------------------- products

export interface ProductsModel {
    title?: string;
    subtitle?: string;
    products: Product[];
    count: number;
    /** The optional lead image a theme may stage as a poster/band inside the run. */
    leadSrc?: string;
}

export function productsModel(section: LayoutSectionData, data: SectionResolvedData | undefined): ProductsModel {
    const products = data?.products?.products ?? [];
    return {
        title: text(section.text.title) ?? text(data?.products?.title),
        subtitle: text(section.text.subtitle),
        products,
        count: products.length,
        leadSrc: mediaUrl(section.props),
    };
}

// ------------------------------------------------------------------------------------- categories

export interface CategoryLink {
    id: number;
    name: string;
    href: string;
    count?: number;
}

export function categoriesModel(data: SectionResolvedData | undefined): CategoryLink[] {
    return (data?.categories ?? []).map((category: Category) => ({
        id: category.id,
        name: category.description?.name ?? category.code,
        href: `/category/${category.description?.friendlyUrl ?? category.code}`,
        count: typeof category.productCount === 'number' && category.productCount > 0 ? category.productCount : undefined,
    }));
}

// -------------------------------------------------------------------------------------------- usp

export interface UspBadge {
    id: string;
    /** One of the catalogue's icon names (truck, shield, refresh, star, headset, gift). A chrome
     *  whose badges are typographic (stamps, stickers) may deliberately not draw it. */
    icon: string;
    title: string;
    body?: string;
}

export function uspModel(section: LayoutSectionData): UspBadge[] {
    return sectionItems(section)
        .filter(item => text(item.text.title))
        .map(item => ({
            id: item.id,
            icon: typeof item.props.icon === 'string' ? item.props.icon : '',
            title: item.text.title!.trim(),
            body: text(item.text.body),
        }));
}

// ------------------------------------------------------------------------------------------ promo

export interface PromoModel {
    message?: string;
    action?: SectionAction;
    backgroundSrc?: string;
}

export function promoModel(section: LayoutSectionData): PromoModel {
    return {
        message: text(section.text.message),
        action: action(section.text.cta, section.props.link),
        backgroundSrc: mediaUrl(section.props),
    };
}

// ------------------------------------------------------------------------------------------ image

export interface ImageModel {
    src?: string;
    alt: string;
    caption?: string;
    href?: string;
    contained: boolean;
}

export function imageModel(section: LayoutSectionData): ImageModel {
    const href = linkHref(section.props.link);
    return {
        src: mediaUrl(section.props),
        // alt is localized copy (text map); older documents stored it as a plain prop — honor both
        alt: text(section.text.alt)
            ?? (typeof section.props.alt === 'string' ? text(section.props.alt) : undefined)
            ?? text(section.text.caption) ?? '',
        caption: text(section.text.caption),
        href: href === '#' ? undefined : href,
        contained: section.variant === 'contained',
    };
}

// --------------------------------------------------------------------------------------- richtext

export interface RichTextModel {
    title?: string;
    /** CMS-authored HTML, sanitized by the content service on write. */
    html?: string;
    centered: boolean;
}

export function richtextModel(section: LayoutSectionData): RichTextModel {
    return {
        title: text(section.text.title),
        html: text(section.text.body),
        centered: section.variant === 'centered',
    };
}

// -------------------------------------------------------------------------------------------- faq

export interface FaqEntryModel {
    question: string;
    /** Sanitized HTML answer. */
    answerHtml: string;
}

export function faqModel(section: LayoutSectionData, data: SectionResolvedData | undefined): FaqEntryModel[] {
    return (data?.faq?.groups ?? [])
        .flatMap(group => group.entries)
        .slice(0, num(section.props.limit, 5))
        .map(entry => ({question: entry.question, answerHtml: entry.answer}));
}

// ------------------------------------------------------------------------------------- newsletter

export interface NewsletterModel {
    heading?: string;
    body?: string;
    cta?: string;
    boxed: boolean;
}

export function newsletterModel(section: LayoutSectionData): NewsletterModel {
    return {
        heading: text(section.text.heading),
        body: text(section.text.body),
        cta: text(section.text.cta),
        boxed: section.variant === 'boxed',
    };
}

// ----------------------------------------------------------------------------------- testimonials

export interface QuoteModel {
    id: string;
    quote: string;
    author?: string;
}

export function testimonialsModel(section: LayoutSectionData): QuoteModel[] {
    return sectionItems(section)
        .filter(item => text(item.text.quote))
        .map(item => ({id: item.id, quote: item.text.quote!.trim(), author: text(item.text.author)}));
}

// ----------------------------------------------------------------------------------------- brands

export interface BrandModel {
    id: string;
    src?: string;
    name?: string;
    href?: string;
}

export function brandsModel(section: LayoutSectionData): BrandModel[] {
    return sectionItems(section)
        .map(item => {
            const href = linkHref(item.props.link);
            return {
                id: item.id,
                src: mediaUrl(item.props),
                name: text(item.text.name),
                href: href === '#' ? undefined : href,
            };
        })
        .filter(brand => brand.src || brand.name);
}

// ------------------------------------------------------------------------------------------ posts

export interface PostCardModel {
    id: number;
    href: string;
    title: string;
    excerpt?: string;
    imageSrc?: string;
}

export function postsModel(data: SectionResolvedData | undefined): PostCardModel[] {
    return (data?.posts?.content ?? []).map(post => ({
        id: post.id,
        href: `/blog/${post.slug}`,
        title: post.title,
        excerpt: text(post.excerpt ?? undefined),
        imageSrc: post.heroImageUrl ?? undefined,
    }));
}

// ------------------------------------------------------------------------------------------ video

/** YouTube/Vimeo page URL → privacy-friendly embed URL; undefined for anything else. */
export function embedUrl(raw: unknown): string | undefined {
    if (typeof raw !== 'string' || !raw) return undefined;
    try {
        const url = new URL(raw);
        const host = url.hostname.replace(/^www\./, '');
        if (host === 'youtube.com' && url.searchParams.get('v')) {
            return `https://www.youtube-nocookie.com/embed/${url.searchParams.get('v')}`;
        }
        if (host === 'youtu.be' && url.pathname.length > 1) {
            return `https://www.youtube-nocookie.com/embed/${url.pathname.slice(1)}`;
        }
        if (host === 'vimeo.com' && /^\/\d+/.test(url.pathname)) {
            return `https://player.vimeo.com/video/${url.pathname.slice(1)}`;
        }
    } catch {
        return undefined;
    }
    return undefined;
}

export interface VideoModel {
    title?: string;
    embedSrc?: string;
}

export function videoModel(section: LayoutSectionData): VideoModel {
    return {
        title: text(section.text.title),
        embedSrc: embedUrl(section.props.url),
    };
}
