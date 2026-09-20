/**
 * How long each anonymous read is kept in Next's data cache, by name.
 *
 * Every server-side read of a storefront page is a function of the store, the language and the URL, and every
 * visitor of a store shares it, so it can be fetched once and served to the next render of any page for a while.
 * How long differs by what the data is: the store record and the category tree change rarely, a product's copy
 * changes when a merchant edits it, a listing changes with every product added. The table below is the default; an
 * environment can set any entry without a rebuild through `STOREFRONT_DATA_CACHE_<NAME>_SECONDS` (`productGroup`
 * becomes `PRODUCT_GROUP`), and `0` turns that read's cache off. Nothing here applies to a read that carries a
 * credential, a preview token or a shopper's cart: those never go through {@link publicCachedGet}.
 *
 * Price and stock (the inventory service) are never cached: a sold-out sku must show as sold out on the next render.
 *
 * The page cache in `storefront/scripts/server/cache/` keeps whole documents by the same convention
 * (`STOREFRONT_CACHE_*`); this table is what a render behind a page-cache miss pays.
 */
export const DATA_CACHE_DEFAULT_SECONDS = {
    /** The store record: name, currency, languages, login requirement. */
    store: 30,
    /** The category tree behind every page's navigation. */
    categories: 30,
    /** One category by its URL, the category page's subject. */
    category: 30,
    /** The `site` document: menus, footer, branding, announcement. */
    site: 30,
    /** A CMS page. */
    page: 30,
    /** The blog index, one page of posts. */
    posts: 30,
    /** One blog post. */
    post: 30,
    banners: 30,
    /** The home page's section layout. */
    layout: 30,
    faq: 30,
    menu: 30,
    policy: 30,
    sitemap: 300,
    /** A moved path's new home. */
    redirect: 30,
    /** A product by its URL, the product page's subject. */
    product: 10,
    /** A named group of products (the home page's strips) and a product's related products. */
    productGroup: 30,
    /** A page of a category listing or of a search. */
    listing: 10,
    /** The filter rail's manufacturers and option facets. */
    facets: 30,
    /** The search box's suggestions. */
    suggest: 30,
} as const;

export type DataCacheName = keyof typeof DATA_CACHE_DEFAULT_SECONDS;

/** The variable that overrides `name`: `STOREFRONT_DATA_CACHE_PRODUCT_GROUP_SECONDS` for `productGroup`. */
export function dataCacheVariable(name: DataCacheName): string {
    return `STOREFRONT_DATA_CACHE_${name.replace(/([A-Z])/g, '_$1').toUpperCase()}_SECONDS`;
}

/**
 * How long the read called `name` is kept, in seconds: the environment's value when it is a whole number of seconds,
 * zero included, else the default. Reads the process environment where there is one (the browser has none, and
 * ignores the cache option anyway).
 */
export function dataCacheSeconds(name: DataCacheName, env: Record<string, string | undefined> = processEnv()): number {
    const value = env[dataCacheVariable(name)];
    if (value !== undefined && value.trim() !== '') {
        const seconds = Number(value);
        if (Number.isInteger(seconds) && seconds >= 0) {
            return seconds;
        }
    }
    return DATA_CACHE_DEFAULT_SECONDS[name];
}

function processEnv(): Record<string, string | undefined> {
    return typeof process !== 'undefined' && process.env ? process.env : {};
}
