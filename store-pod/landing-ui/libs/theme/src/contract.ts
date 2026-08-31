import type {ComponentType, ReactNode} from 'react';
import type {
    AnnouncementData, BreadcrumbItem, Category, ColorSchema, FaqDocument, LayoutSectionData, ListingFacets,
    ListingQuery, MenuNode, NavPage, Policy, PostList, PostSummary, Product, ProductListingPage,
    ProductSearchPage, ProductSearchQuery, SearchCapabilities, SearchFacets, SectionKind, SiteBranding,
    SocialLink, Store, StoreContext, StorefrontLink, StorefrontSeo,
} from '@store-front/types';
import type {ColorRoleTokens} from './tokens';

export type ThemeId = string;
export type Dir = 'ltr' | 'rtl';

/* ------------------------------------------------------------------------------------------------ */
/* Theme-level configuration                                                                         */
/* ------------------------------------------------------------------------------------------------ */

export interface ThemeFonts {
    /** Space-joined `next/font` `.variable` class names; the shell puts them on `<html>`. */
    variables: string;
    /** Which CSS variables the theme's `tokens.css` maps onto those fonts (documentation + QA). */
    roles: { sans: string; display?: string; mono?: string };
}

export interface ThemeTokenPolicy {
    /**
     * The theme's own palette — rendered when the merchant's colour theme is `DEFAULT` (or unset/unknown).
     * Generated into `src/colors.ts` by `libs/types/scripts/build-color-schemas.mjs` (`THEME_DEFAULTS`), so
     * it obeys the same contrast rules as the fixed presets. A preset the merchant picks replaces it whole.
     */
    defaultColors: ColorSchema;
    /** Re-map the merchant preset into the theme's colour roles (e.g. force ink, mute primary). */
    mapMerchantColors?: (schema: ColorSchema, meta: { isDark: boolean }) => Partial<ColorRoleTokens>;
    /** Minimum contrast the bridge enforces for every `*-foreground` pair. Default 4.5. */
    minContrast?: number;
}

export interface ThemeLayoutConfig {
    header: { sticky: boolean; heightPx: { base: number; lg: number } };
    cart: 'drawer' | 'page';
    mobileNav: 'drawer' | 'fullscreen' | 'bottom-bar';
    productGrid: { base: 1 | 2; sm: number; lg: number; xl: number };
    productImageAspect: '1/1' | '3/4' | '4/5' | '4/3';
    container: 'narrow' | 'content' | 'wide' | 'fluid';
    /** `page` sends the box straight to /search instead of opening a dropdown under it. */
    search: 'header' | 'overlay' | 'page' | 'hidden';
}

/* ------------------------------------------------------------------------------------------------ */
/* Data the shell hands to the theme                                                                 */
/* ------------------------------------------------------------------------------------------------ */

export interface PageContext {
    store: Store;
    storeContext: StoreContext;
    locale: string;
    dir: Dir;
    layout: ThemeLayoutConfig;
}

export interface LayoutData {
    store: Store;
    /** Category tree roots (children populated). */
    categories: Category[];
    /**
     * CMS page links; `inMenu` marks main-menu pages, the rest are footer pages.
     * Kept so themes that have not adopted `menus` keep rendering; derived from the site document.
     */
    pages: NavPage[];
    /** The announcement strip: the live STRIP banner's message. The `header-message` snippet it replaced is gone. */
    announcement?: AnnouncementData;
    /** The merchant-managed menus, with resolved hrefs. Empty arrays when the store has not configured them. */
    menus: { main: MenuNode[]; footer: MenuNode[] };
    /** Live legal policies, for the footer and checkout. */
    policies: StorefrontLink[];
    /**
     * The store's brand imagery, from the content service's media library. Not on `store`: keeping it there
     * would have the merchant record still claiming to own appearance.
     */
    branding: SiteBranding;
    socialLinks: SocialLink[];
    search: SearchCapabilities;
}

export interface RootLayoutProps {
    ctx: PageContext;
    data: LayoutData;
    children: ReactNode;
}

/**
 * What the shell resolved for one layout section before rendering it: the referenced catalog or content data,
 * fetched in one batched fan-out. Kinds whose content is entirely inline (`richtext`, `usp`, …) get none.
 */
export interface SectionResolvedData {
    /** For `products`: the source's products, already presented for cards. */
    products?: { title?: string; products: Product[] };
    /** For `categories`: visible category tree roots. */
    categories?: Category[];
    /** For `faq`: the referenced FAQ document. */
    faq?: FaqDocument;
    /** For `posts`: the latest posts. */
    posts?: PostList;
}

export interface SectionRenderProps {
    ctx: PageContext;
    section: LayoutSectionData;
    data: SectionResolvedData | undefined;
    /** True inside the builder's canvas iframe — empty sections show hints instead of collapsing. */
    preview: boolean;
}

/**
 * The renderers a theme brings per section kind, keyed by variant id. Partial on purpose: any kind or variant
 * a theme does not cover renders through the shell's fallback set, so every layout renders on every theme.
 * A theme may add its own variant ids beyond the catalogue's — the builder offers them on that theme only.
 */
export type ThemeSectionRegistry = Partial<Record<SectionKind, Record<string, ComponentType<SectionRenderProps>>>>;

export interface CategoryData {
    category: Category;
    breadcrumbs: BreadcrumbItem[];
    /** First page for `query`, loaded server-side so the listing never first-paints empty. */
    initial: ProductListingPage;
    query: ListingQuery;
    facets: ListingFacets;
}

/**
 * What the search results page renders.
 *
 * `initial` is loaded server-side so results never first-paint empty, exactly like the category listing. The
 * facet counts come back with it, over the same predicate, so a filter the rail offers always leads somewhere.
 */
export interface SearchData {
    query: ProductSearchQuery;
    initial: ProductSearchPage;
    facets: SearchFacets;
    /** Set only when the query matched nothing and a close product name was found. */
    didYouMean?: string;
    /** The language the results came from, when it is not the one the shopper is browsing in. */
    fallbackLanguage?: string;
}

export interface ProductData {
    product: Product;
    breadcrumbs: BreadcrumbItem[];
    /** Loaded in parallel with the product; `[]` when the relationship call degrades. */
    related: Product[];
}

export interface ContentData {
    page: { slug: string; title: string };
    /** Decoded HTML of the page body — render with `dangerouslySetInnerHTML`. */
    html: string;
    breadcrumbs: BreadcrumbItem[];
    seo?: StorefrontSeo;
}

export interface BlogIndexData {
    posts: PostList;
    categories: StorefrontLink[];
    /** Active filters, for the heading and the pager links. */
    category?: string;
    tag?: string;
    breadcrumbs: BreadcrumbItem[];
}

export interface BlogPostData {
    post: PostSummary;
    /** Decoded HTML of the body. */
    html: string;
    breadcrumbs: BreadcrumbItem[];
}

export interface FaqData {
    faq: FaqDocument;
    breadcrumbs: BreadcrumbItem[];
}

export interface PolicyData {
    policy: Policy;
    html: string;
    breadcrumbs: BreadcrumbItem[];
}

export interface CheckoutData {
    requireLogin: boolean;
}

export interface CheckoutResultData {
    /** Which redirect URL the gateway used. The component still verifies the real status via the API. */
    outcome: 'success' | 'cancel';
    requireLogin: boolean;
}

export type CustomerData = Record<string, never>;

export interface OrderData {
    orderId: number;
}

export interface PageProps<D> {
    ctx: PageContext;
    data: D;
}

/* ------------------------------------------------------------------------------------------------ */
/* What a theme implements                                                                           */
/* ------------------------------------------------------------------------------------------------ */

export interface ThemePages {
    Category: ComponentType<PageProps<CategoryData>>;
    Product: ComponentType<PageProps<ProductData>>;
    Content: ComponentType<PageProps<ContentData>>;
    BlogIndex: ComponentType<PageProps<BlogIndexData>>;
    BlogPost: ComponentType<PageProps<BlogPostData>>;
    Faq: ComponentType<PageProps<FaqData>>;
    Policy: ComponentType<PageProps<PolicyData>>;
    Checkout: ComponentType<PageProps<CheckoutData>>;
    CheckoutResult: ComponentType<PageProps<CheckoutResultData>>;
    Customer: ComponentType<PageProps<CustomerData>>;
    Order: ComponentType<PageProps<OrderData>>;
    /**
     * Optional, unlike every other page.
     *
     * A theme that does not implement it gets a plain results page from the shell, composed from that theme's
     * own product grid and states — so search works everywhere from the day the endpoint lands, and a theme
     * adopts a designed page when someone designs one. Making it required would have meant twelve bespoke
     * pages before any shopper could search at all.
     */
    Search?: ComponentType<PageProps<SearchData>>;
}

export type PageSkeletonKind = 'home' | 'category' | 'product' | 'content' | 'checkout' | 'customer' | 'order';

/** Skeletons a theme may add beyond the required set; the shell falls back to a related one when absent. */
export type OptionalPageSkeletonKind = 'search';
export type NotFoundKind = 'product' | 'category' | 'page' | 'route';
export type EmptyKind = 'cart' | 'listing' | 'orders' | 'search';
export type RedirectReason = 'login' | 'callback';

/**
 * State components. `ErrorState`, `EmptyState` and `Redirecting` are rendered from client components
 * (error boundaries, hooks) and MUST therefore be `'use client'` modules themselves.
 */
export interface ThemeStates {
    PageSkeleton: Record<PageSkeletonKind, ComponentType> & Partial<Record<OptionalPageSkeletonKind, ComponentType>>;
    ErrorState: ComponentType<{ error: Error & { digest?: string }; reset: () => void }>;
    NotFound: ComponentType<{ kind: NotFoundKind }>;
    EmptyState: ComponentType<{ kind: EmptyKind; action?: ReactNode }>;
    Redirecting: ComponentType<{ reason: RedirectReason }>;
}

export interface ThemeDefinition {
    id: ThemeId;
    name: string;
    /** Bump when DESIGN.md changes materially; surfaced in the HTML as `data-theme-version`. */
    version: string;
    description?: string;
    fonts: ThemeFonts;
    tokens: ThemeTokenPolicy;
    layout: { config: ThemeLayoutConfig; Root: ComponentType<RootLayoutProps> };
    pages: ThemePages;
    states: ThemeStates;
    /**
     * Optional, like {@code pages.Search}: the home page is composed by the shell from the store's layout
     * document, and any kind/variant missing here renders through the shell's fallback renderers — so a theme
     * with no registry at all still renders every merchant layout. A theme adopts designed sections (and may
     * add exclusive variants) kind by kind.
     */
    sections?: ThemeSectionRegistry;
    /**
     * CSS served at `/css/login.css` for the customer auth pages rendered by `cua` (which link that path).
     * Optional; the shell ships a neutral default.
     */
    loginCss?: string;
}
