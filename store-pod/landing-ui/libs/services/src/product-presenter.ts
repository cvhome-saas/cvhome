import {Image, Product} from "@store-front/types";

/**
 * Presentation predicates every theme needs and none should re-derive. Pure functions over `Product`.
 */
export const PLACEHOLDER_IMAGE = '/placeholder.png';

export const isOutOfStock = (p: Product): boolean => !p.available || !p.canBePurchased || (p.quantity ?? 0) < 1;

export const isOnSale = (p: Product): boolean =>
    !!(p.productPrice?.discounted && p.productPrice.originalPrice && p.productPrice.originalPrice !== p.productPrice.finalPrice);

export const isLowStock = (p: Product, threshold = 5): boolean => !isOutOfStock(p) && p.quantity <= threshold;

export const hasVariants = (p: Product): boolean => !!p.options?.some(o => o.variant && o.optionValues?.length);

/** "Save 20 %" when both prices parse to numbers, otherwise undefined (prices are pre-formatted strings). */
export function discountPercent(p: Product): number | undefined {
    if (!isOnSale(p)) return undefined;
    const num = (s: string | undefined) => {
        if (!s) return NaN;
        const cleaned = s.replace(/[^\d.,-]/g, '').replace(/,(?=\d{3}(\D|$))/g, '').replace(',', '.');
        return parseFloat(cleaned);
    };
    const original = num(p.productPrice?.originalPrice);
    const final = num(p.productPrice?.finalPrice);
    if (!isFinite(original) || !isFinite(final) || original <= 0 || final >= original) return undefined;
    return Math.round((1 - final / original) * 100);
}

export function sortedImages(p: Product): Image[] {
    const images = [...(p.images ?? [])];
    images.sort((a, b) => Number(b.defaultImage) - Number(a.defaultImage) || (a.order ?? 0) - (b.order ?? 0));
    return images;
}

export function primaryImage(p: Product): { src: string; alt: string } {
    const img = p.image ?? sortedImages(p)[0];
    return {
        src: img?.imageUrl || PLACEHOLDER_IMAGE,
        alt: img?.altText || p.description?.name || '',
    };
}

export function secondaryImage(p: Product): { src: string; alt: string } | undefined {
    const images = sortedImages(p);
    const img = images.find(i => i.imageUrl !== (p.image?.imageUrl ?? images[0]?.imageUrl));
    return img ? {src: img.imageUrl, alt: img.altText || p.description?.name || ''} : undefined;
}

export const productHref = (p: Product): string => p.description ? `/product/${p.description.friendlyUrl}` : '#';

/** The images a listing cell can reach: `primaryImage` takes the first, `secondaryImage` the next distinct one. */
const LISTING_IMAGE_COUNT = 2;

/**
 * A product as a *listing* cell needs it — for the home rails, category and search results, and related
 * products. Everything a card cannot render is dropped before the object crosses the RSC boundary into a
 * client `ProductCard`, where it would otherwise be serialised into the HTML in full.
 *
 * On a measured fashion home page the products were 225 KB of a 399 KB flight payload: 366 image records
 * where the cards show at most two each, plus specifications, attributes and categories that only the
 * product page reads. Every field dropped here is either already `| undefined` on `Product` (so the
 * compiler already forces callers to cope) or unread anywhere in the storefront — `productSpecifications`,
 * `properties`, `variants` and the four `Image` fields have no reader at all.
 *
 * Deliberately kept: `description` in full (hunger's card prints the copy), `options` (`hasVariants` reads
 * it), and every price/stock field. The product page keeps the untouched record — never use this there.
 */
export function toListingProduct(p: Product): Product {
    const {productSpecifications, attributes, properties, variants, categories, ...rest} = p;
    const slim = (img: Image): Image => ({
        id: img.id,
        imageUrl: img.imageUrl,
        altText: img.altText,
        order: img.order,
        defaultImage: img.defaultImage,
    });
    return {
        ...rest,
        productSpecifications: undefined,
        attributes: undefined,
        properties: undefined,
        variants: undefined,
        categories: undefined,
        image: p.image ? slim(p.image) : undefined,
        images: p.images ? sortedImages(p).slice(0, LISTING_IMAGE_COUNT).map(slim) : undefined,
    };
}

/** `toListingProduct` over a list, tolerating the `undefined` every page container allows. */
export const toListingProducts = (products: Product[] | undefined): Product[] | undefined =>
    products?.map(toListingProduct);
