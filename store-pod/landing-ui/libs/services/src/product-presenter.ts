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
        alt: p.description?.name || img?.imageName || '',
    };
}

export function secondaryImage(p: Product): { src: string; alt: string } | undefined {
    const images = sortedImages(p);
    const img = images.find(i => i.imageUrl !== (p.image?.imageUrl ?? images[0]?.imageUrl));
    return img ? {src: img.imageUrl, alt: p.description?.name || img.imageName || ''} : undefined;
}

export const productHref = (p: Product): string => p.description ? `/product/${p.description.friendlyUrl}` : '#';
