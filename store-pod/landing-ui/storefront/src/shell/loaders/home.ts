import 'server-only';
import {cache} from 'react';
import {ContentService} from '@store-front/services/content-service';
import {ProductService} from '@store-front/services/product-service';
import type {HomeSection, ProductGroup, ProductGroupCode} from '@store-front/types';
import type {HomeData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

/**
 * The product groups a `PRODUCT_GROUP` section may draw.
 *
 * The home page used to be exactly this list, in this order, for every store. It is a whitelist now: the page is
 * the merchant's `SECTION` rows, and this only says which group codes exist to point at.
 */
const GROUPS: ProductGroupCode[] = ['FEATURED_ITEMS', 'NEWLY_ADDED', 'HOME_PAGE', 'RECOMMENDED'];

const renderable = (g: ProductGroup | undefined) => !!(g && g.active && g.description && g.products && g.products.length > 0);

/** The group codes the page actually asks for, so a store with no sections still shows its rails. */
const requested = (sections: HomeSection[]): ProductGroupCode[] => {
    const named = sections
        .filter(s => s.kind === 'PRODUCT_GROUP' && s.targetValue)
        .map(s => s.targetValue as ProductGroupCode)
        .filter(code => GROUPS.includes(code));
    return named.length > 0 ? [...new Set(named)] : GROUPS;
};

export const loadHome = cache(async (): Promise<HomeData> => {
    const ctx = await getStoreContext();
    const [banners, sections] = await Promise.all([
        ContentService.getBanners(ctx),
        ContentService.getHomeSections(ctx),
    ]);
    const codes = requested(sections);
    const groups = await Promise.all(codes.map(code => ProductService.getProductByGroup(ctx, code)));

    const hero = banners.filter(b => b.placement === 'HERO');
    const carousel = banners.filter(b => b.placement === 'CAROUSEL');
    return {
        // The slides used to come from merchant's own slider and the banner from a separate column, so a store
        // could have two heroes that knew nothing about each other. Both are CMS banners now.
        hero: {slides: carousel.length > 0 ? carousel : hero, banner: hero[0]},
        banners: {
            hero,
            carousel,
            strip: banners.find(b => b.placement === 'STRIP'),
        },
        sections,
        groups: groups.flatMap((g, i) => renderable(g)
            ? [{code: codes[i], title: g!.description!.name, products: g!.products!}]
            : []),
    };
});
