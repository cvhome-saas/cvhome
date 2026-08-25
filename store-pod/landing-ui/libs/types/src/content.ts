/** A CMS page link for navs and footers, derived from the site document. */
export interface NavPage {
    /** The page slug — a stable key. */
    code: string
    name: string
    /** Locale-less storefront path (`/content/<slug>`). */
    href: string
    /** A main-menu page; the rest are footer pages. */
    inMenu: boolean
}

/** The announcement strip's view model: a live STRIP banner, else the `header-message` snippet. */
export interface AnnouncementData {
    /** The per-session dismissal key. */
    code: string
    /** Decoded HTML. */
    html: string
}

/** The checkout agreement (the live TERMS policy), decoded and ready to render. */
export interface AgreementText {
    title: string
    /** Decoded HTML. */
    html: string
}

/* ------------------------------------------------------------------------------------------------ */
/* Storefront read API (`/api/v1/storefront/**`) — the content platform's public surface.          */
/* Every translated object names the locale that served it (`servedLocale`).                       */
/* ------------------------------------------------------------------------------------------------ */

export type MenuTargetKind = 'PAGE' | 'CATEGORY' | 'PRODUCT' | 'POLICY' | 'BLOG_INDEX' | 'FAQ_INDEX' | 'URL'

export interface MenuNode {
    label: string
    kind: MenuTargetKind
    value: string | null
    /** Locale-less storefront path (`/content/<slug>`, `/blog`, `/help`, `/policies/<type>`, `/category/<handle>`) or an absolute URL. */
    href: string
    openInNewTab: boolean
    children: MenuNode[]
}

export interface StorefrontLink {
    slug: string
    title: string
    href: string
    type: string
}

export interface StorefrontSeo {
    metaTitle: string | null
    metaDescription: string | null
    keywords: string | null
    canonicalUrl: string | null
    noindex: boolean
    ogImageUrl: string | null
}

export type BannerPlacement = 'HERO' | 'CAROUSEL' | 'COLLECTION' | 'STRIP'

export interface Banner {
    id: number
    placement: BannerPlacement
    position: number
    servedLocale: string | null
    title: string | null
    subtitle: string | null
    /** Rich text; only the announcement strip carries one. */
    body: string | null
    ctaLabel: string | null
    target: { kind: 'COLLECTION' | 'PRODUCT' | 'PAGE' | 'URL'; value: string } | null
    desktopUrl: string | null
    mobileUrl: string | null
    altText: string | null
    theme: { textColor?: string | null; overlayOpacity?: number | null; alignment?: string | null } | null
    startsAt: string | null
    endsAt: string | null
}

export interface SiteContent {
    servedLocale: string | null
    snippets: Record<string, string>
    announcement: Banner | null
    menus: { main: MenuNode[]; footer: MenuNode[] }
    footerPages: StorefrontLink[]
    policies: StorefrontLink[]
}

export interface StorefrontPage {
    id: number
    slug: string
    servedLocale: string
    title: string
    body: string
    template: 'STANDARD' | 'LANDING' | 'CONTACT' | 'FAQ_PAGE' | null
    /** Reserved for the page builder. */
    blocks: unknown[]
    seo: StorefrontSeo
    breadcrumbs: StorefrontLink[]
    updatedAt: string | null
}

export interface PostSummary {
    id: number
    slug: string
    servedLocale: string
    title: string
    excerpt: string | null
    body?: string | null
    heroImageUrl: string | null
    publishedAt: string | null
    authorName: string | null
    readingMinutes: number
    featured: boolean
    categories: StorefrontLink[]
    tags: string[]
    seo?: StorefrontSeo | null
    related?: PostSummary[] | null
}

export interface PostList {
    totalPages: number
    size: number
    totalElements: number
    pageNumber: number
    content: PostSummary[]
}

export interface FaqEntry {
    id: number
    slug: string
    question: string
    answer: string
    showInCheckoutHelp: boolean
}

export interface FaqGroup {
    key: string
    name: string
    entries: FaqEntry[]
}

export interface FaqDocument {
    servedLocale: string | null
    groups: FaqGroup[]
    /** `FAQPage` structured data, ready for a `<script type="application/ld+json">`. */
    jsonLd: string
}

export type PolicyType = 'TERMS' | 'PRIVACY' | 'RETURNS' | 'SHIPPING' | 'COOKIES' | 'CUSTOM'

export interface Policy {
    id: number
    type: PolicyType
    slug: string
    version: number
    servedLocale: string
    heading: string
    body: string
    effectiveFrom: string | null
    requiresAcceptance: boolean
}

export interface SitemapEntry {
    loc: string
    lastmod: string | null
    changefreq: string
    type: string
}
