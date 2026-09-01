/**
 * The page layout document the storefront renders — the wire mirror of the content service's
 * `GET /api/v1/storefront/layout/{page}` response. Sections arrive render-ready: hidden ones already dropped,
 * localized copy flattened to the served locale, and every `mediaId` prop joined by a resolved `mediaUrl`.
 */

export const SECTION_KINDS = [
    'hero', 'products', 'categories', 'promo', 'image', 'richtext', 'faq', 'posts', 'testimonials',
    'newsletter', 'usp', 'video', 'brands',
] as const;

export type SectionKind = typeof SECTION_KINDS[number];

export type SectionSpacing = 'none' | 'sm' | 'md' | 'lg';
export type SectionWidth = 'content' | 'wide' | 'full';
export type SectionTone = 'default' | 'muted' | 'inverse';
export type SectionDevice = 'desktop' | 'tablet' | 'mobile';

export interface SectionStyle {
    spacing?: SectionSpacing;
    width?: SectionWidth;
    tone?: SectionTone;
}

/** One repeatable block inside a section — a hero slide, a USP badge, a testimonial quote. */
export interface SectionItem {
    id: string;
    props: Record<string, unknown>;
    text: Record<string, string>;
}

export interface LayoutSectionData {
    id: string;
    kind: SectionKind;
    /** Renderer variant; unknown values fall back to the kind's default. */
    variant?: string | null;
    props: Record<string, unknown>;
    items?: SectionItem[] | null;
    /** Copy in the served locale, keyed by field (`title`, `heading`, `body`, …). */
    text: Record<string, string>;
    style?: SectionStyle | null;
    anchor?: string | null;
    /** Device classes the section shows on; null/empty means all. */
    devices?: SectionDevice[] | null;
    /** Builder-only: a locked section keeps its place; the canvas suppresses its toolbar. */
    locked?: boolean | null;
}

export interface PageLayoutData {
    page: string;
    servedLocale?: string | null;
    sections: LayoutSectionData[];
}

/** Where a `products` section draws from. `manual` is reserved until catalog exposes a by-ids read. */
export interface ProductSourceRef {
    type: 'group' | 'category' | 'newest' | 'manual';
    code?: string;
    limit?: number;
}
