import 'server-only';
import {cache} from 'react';
import {ProductService} from '@store-front/services/product-service';
import type {ProductGroup, ProductGroupCode} from '@store-front/types';
import type {HomeData} from '@store-front/theme';
import {getStore, getStoreContext} from '@/shell/request/store-context';

const GROUPS: ProductGroupCode[] = ['FEATURED_ITEMS', 'NEWLY_ADDED', 'HOME_PAGE', 'RECOMMENDED'];

const renderable = (g: ProductGroup | undefined) => !!(g && g.active && g.description && g.products && g.products.length > 0);

export const loadHome = cache(async (): Promise<HomeData> => {
    const ctx = await getStoreContext();
    const [store, ...groups] = await Promise.all([
        getStore(),
        ...GROUPS.map(code => ProductService.getProductByGroup(ctx, code)),
    ]);
    return {
        hero: {
            slides: [...(store.sliderImages ?? [])].sort((a, b) => a.priority - b.priority),
            banner: store.banner,
        },
        groups: groups.flatMap((g, i) => renderable(g)
            ? [{code: GROUPS[i], title: g!.description!.name, products: g!.products!}]
            : []),
    };
});
