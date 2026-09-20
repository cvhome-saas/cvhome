import {
    FaqDocument, Policy, PolicyType, PostList, PostSummary, SiteContent, SitemapEntry,
    StorefrontPage, Banner, BannerPlacement, MenuNode,
} from "@store-front/types/content";
import {PageLayoutData} from "@store-front/types/layout";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get, orUndefined, publicCachedGet} from "./http-utils";

const sf = (ctx: StoreContext, path: string, query = '') =>
    `${storeBaseServiceUrl('content', ctx)}/api/v1/storefront/${path}?store=${ctx.store}&lang=${ctx.locale}${query}`;

/**
 * The content platform's public read API (`/api/v1/storefront/**`): one `site` document for the layout, and
 * pages, posts, banners, FAQ, menus, policies, sitemap and redirects.
 */
export class ContentService {

    /** Degrades to an empty site: the layout must render even if the CMS is down. */
    public static getSite = async (ctx: StoreContext): Promise<SiteContent | undefined> => {
        return orUndefined(apiFetch<SiteContent>(sf(ctx, 'site'), publicCachedGet('site')));
    }

    /** Must fail: the page is what the route is for. `preview` is the editor's draft token, never cached. */
    public static getStorefrontPage = async (ctx: StoreContext, slug: string, preview?: string): Promise<StorefrontPage> => {
        return apiFetch<StorefrontPage>(sf(ctx, `pages/${encodeURIComponent(slug)}`, preview ? `&preview=${encodeURIComponent(preview)}` : ''),
            preview ? get() : publicCachedGet('page'));
    }

    public static getPosts = async (ctx: StoreContext, q: { page?: number; count?: number; category?: string; tag?: string } = {}): Promise<PostList | undefined> => {
        const query = `&page=${q.page ?? 0}&count=${q.count ?? 12}` +
            (q.category ? `&category=${encodeURIComponent(q.category)}` : '') +
            (q.tag ? `&tag=${encodeURIComponent(q.tag)}` : '');
        return orUndefined(apiFetch<PostList>(sf(ctx, 'posts', query), publicCachedGet('posts')));
    }

    public static getPost = async (ctx: StoreContext, slug: string, preview?: string): Promise<PostSummary> => {
        return apiFetch<PostSummary>(sf(ctx, `posts/${encodeURIComponent(slug)}`, preview ? `&preview=${encodeURIComponent(preview)}` : ''),
            preview ? get() : publicCachedGet('post'));
    }

    public static getBanners = async (ctx: StoreContext, placement?: BannerPlacement): Promise<Banner[]> => {
        return (await orUndefined(apiFetch<Banner[]>(sf(ctx, 'banners', placement ? `&placement=${placement}` : ''), publicCachedGet('banners')))) ?? [];
    }

    /**
     * The page's layout document, render-ready. `preview` (the builder's token) serves the draft, uncached.
     * Degrades to an empty page: the shell still renders header and footer if the CMS is down.
     */
    public static getPageLayout = async (ctx: StoreContext, page: 'HOME', preview?: string): Promise<PageLayoutData> => {
        return (await orUndefined(apiFetch<PageLayoutData>(
            sf(ctx, `layout/${page}`, preview ? `&preview=${encodeURIComponent(preview)}` : ''), preview ? get() : publicCachedGet('layout'))))
            ?? {page, servedLocale: ctx.locale, sections: []};
    }

    public static getFaq = async (ctx: StoreContext, group?: string): Promise<FaqDocument | undefined> => {
        return orUndefined(apiFetch<FaqDocument>(sf(ctx, 'faq', group ? `&group=${encodeURIComponent(group)}` : ''), publicCachedGet('faq')));
    }

    public static getMenu = async (ctx: StoreContext, handle: 'MAIN' | 'FOOTER'): Promise<MenuNode[]> => {
        return (await orUndefined(apiFetch<MenuNode[]>(sf(ctx, `menus/${handle}`), publicCachedGet('menu')))) ?? [];
    }

    /** Must fail (404 → not found page). `version` reads an archived cut. */
    public static getPolicy = async (ctx: StoreContext, type: PolicyType, version?: number): Promise<Policy> => {
        return apiFetch<Policy>(sf(ctx, `policies/${type}`, version ? `&v=${version}` : ''), publicCachedGet('policy'));
    }

    public static getSitemap = async (ctx: StoreContext): Promise<SitemapEntry[]> => {
        return (await orUndefined(apiFetch<SitemapEntry[]>(sf(ctx, 'sitemap'), publicCachedGet('sitemap')))) ?? [];
    }

    /** `undefined` when the path never moved. */
    public static getRedirect = async (ctx: StoreContext, path: string): Promise<{ from: string; to: string } | undefined> => {
        return orUndefined(apiFetch<{ from: string; to: string }>(sf(ctx, 'redirects', `&path=${encodeURIComponent(path)}`), publicCachedGet('redirect')));
    }
}
