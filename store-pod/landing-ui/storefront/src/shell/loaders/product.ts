import 'server-only';
import {cache} from 'react';
import {notFound} from 'next/navigation';
import {getTranslations} from 'next-intl/server';
import {ProductService} from '@store-front/services/product-service';
import {toListingProducts} from '@store-front/services/product-presenter';
import {isApiError} from '@store-front/types';
import type {ProductData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

export const loadProduct = cache(async (url: string): Promise<ProductData> => {
    const ctx = await getStoreContext();
    let product;
    try {
        product = await ProductService.getProductByUrl(url, ctx);
    } catch (e) {
        if (isApiError(e) && e.category === 'NOT_FOUND') notFound();
        throw e;
    }
    if (!product) notFound();
    const [t, related] = await Promise.all([
        getTranslations('COMMON'),
        ProductService.getRelatedProductGroup(ctx, product.id),
    ]);
    const crumbs = [{id: 'home', name: t('HOME'), href: '/'}];
    const cat = product.categories?.[0];
    if (cat?.description) crumbs.push({id: String(cat.id), name: cat.description.name, href: `/category/${cat.description.friendlyUrl}`});
    if (product.description) crumbs.push({id: String(product.id), name: product.description.name, href: `/product/${product.description.friendlyUrl}`});
    return {
        product,
        breadcrumbs: crumbs,
        // `product` stays whole — the buy box needs the gallery, options and attributes. The related rail
        // is cards, so it gets the listing projection.
        related: related?.active && related.products ? toListingProducts(related.products)! : [],
    };
});
