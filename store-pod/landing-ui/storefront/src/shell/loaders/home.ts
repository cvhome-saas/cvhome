import 'server-only';
import {cache} from 'react';
import {ContentService} from '@store-front/services/content-service';
import {ProductService} from '@store-front/services/product-service';
import type {ProductGroup, ProductGroupCode} from '@store-front/types';
import type {HomeData} from '@store-front/theme';
import {getStore, getStoreContext} from '@/shell/request/store-context';

const GROUPS: ProductGroupCode[] = ['FEATURED_ITEMS', 'NEWLY_ADDED', 'HOME_PAGE', 'RECOMMENDED'];

const renderable = (g: ProductGroup | undefined) => !!(g && g.active && g.description && g.products && g.products.length > 0);

export const loadHome = cache(async (): Promise<HomeData> => {
    const ctx = await getStoreContext();
    const [store, banners, ...groups] = await Promise.all([
        getStore(),
        ContentService.getBanners(ctx),
        ...GROUPS.map(code => ProductService.getProductByGroup(ctx, code)),
    ]);
    return {
        hero: {
            slides: [...(store.sliderImages ?? [])].sort((a, b) => a.priority - b.priority),
            banner: store.banner,
        },
        banners: {
            hero: banners.filter(b => b.placement === 'HERO'),
            carousel: banners.filter(b => b.placement === 'CAROUSEL'),
            strip: banners.find(b => b.placement === 'STRIP'),
        },
        groups: groups.flatMap((g, i) => renderable(g)
            ? [{code: GROUPS[i], title: g!.description!.name, products: g!.products!}]
            : []),
    };
});
