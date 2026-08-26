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

/** The announcement strip's view model, decoded from the live STRIP banner. */
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
    /** Rich text. Only the announcement strip carries one — it is the strip's message, not a caption. */
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

/** A media asset, resolved for rendering. */
export interface MediaRef {
    id: number
    url: string
    alt: string | null
    width: number | null
    height: number | null
}

/**
 * The store's brand imagery. Every slot is optional — a store that has uploaded nothing renders its name as a
 * wordmark instead.
 */
export interface SiteBranding {
    logo: MediaRef | null
    logoDark: MediaRef | null
    /** Distinct from the logo on purpose: the two used to be the same file, so a wide wordmark went in the tab. */
    favicon: MediaRef | null
    og: MediaRef | null
}

export interface SocialLink {
    provider: string
    url: string
}

/** What a home-page section renders. */
export type HomeSectionKind =
    'PRODUCT_GROUP' | 'CATEGORY_GRID' | 'BANNER_REF' | 'RICH_TEXT' | 'IMAGE' | 'POST_FEED' | 'FAQ_REF'

/**
 * One block of the store's home page. Before these existed the page was a hard-coded list of four product groups
 * in the storefront's loader, so a seller could not reorder it or put anything else on it.
 */
export interface HomeSection {
    id: number
    slug: string
    sortOrder: number
    servedLocale: string | null
    kind: HomeSectionKind
    /** The product group code, category code, banner slug or FAQ group key this block draws. */
    targetValue: string | null
    title: string | null
    subtitle: string | null
    body: string | null
    ctaLabel: string | null
    cta: { kind: MenuTargetKind; value: string | null } | null
    imageUrl: string | null
    itemLimit: number | null
    layout: string | null
}

export interface SiteContent {
    servedLocale: string | null
    /** The store's own title and description, already resolved to the served locale. */
    seo: StorefrontSeo
    branding: SiteBranding
    socialLinks: SocialLink[]
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
