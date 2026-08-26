import type {ComponentType, ReactNode} from 'react';
import type {
    AnnouncementData, Banner, BreadcrumbItem, Category, ColorSchema, FaqDocument, HomeSection, ListingFacets,
    ListingQuery, MenuNode, NavPage, Policy, PostList, PostSummary, Product, ProductGroupCode, ProductListingPage,
    SearchCapabilities, SiteBranding, SocialLink, Store, StoreContext, StorefrontLink, StorefrontSeo,
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
    search: 'header' | 'overlay' | 'hidden';
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

export interface HomeData {
    /**
     * What goes at the top of the page. The slides are CMS banners now — merchant's separate slider is gone, so
     * the two competing hero concepts the storefront used to merge are one.
     */
    hero: { slides: Banner[]; banner?: Banner };
    /** CMS banners that win each placement right now. */
    banners: { hero: Banner[]; carousel: Banner[]; strip?: Banner };
    /** The merchant-ordered blocks of the page, replacing a hard-coded list of four product groups. */
    sections: HomeSection[];
    groups: { code: ProductGroupCode; title: string; products: Product[] }[];
}

export interface CategoryData {
    category: Category;
    breadcrumbs: BreadcrumbItem[];
    /** First page for `query`, loaded server-side so the listing never first-paints empty. */
    initial: ProductListingPage;
    query: ListingQuery;
    facets: ListingFacets;
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
    Home: ComponentType<PageProps<HomeData>>;
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
}

export type PageSkeletonKind = 'home' | 'category' | 'product' | 'content' | 'checkout' | 'customer' | 'order';
export type NotFoundKind = 'product' | 'category' | 'page' | 'route';
export type EmptyKind = 'cart' | 'listing' | 'orders' | 'search';
export type RedirectReason = 'login' | 'callback';

/**
 * State components. `ErrorState`, `EmptyState` and `Redirecting` are rendered from client components
 * (error boundaries, hooks) and MUST therefore be `'use client'` modules themselves.
 */
export interface ThemeStates {
    PageSkeleton: Record<PageSkeletonKind, ComponentType>;
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
     * CSS served at `/css/login.css` for the customer auth pages rendered by `cua` (which link that path).
     * Optional; the shell ships a neutral default.
     */
    loginCss?: string;
}
