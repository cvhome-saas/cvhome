import 'server-only';
import {cache} from 'react';
import {CategoryService} from '@store-front/services/category-service';
import {ContentService} from '@store-front/services/content-service';
import {ProductService} from '@store-front/services/product-service';
import {ProductSearchService} from '@store-front/services/product-search-service';
import {toListingProducts} from '@store-front/services/product-presenter';
import {defaultSearchQuery} from '@store-front/types/search';
import type {LayoutSectionData, PageLayoutData, Product, ProductSourceRef} from '@store-front/types';
import type {SectionResolvedData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

/**
 * The home page: the store's layout document plus, per section, whatever catalog or content data the section
 * references — resolved here in one parallel fan-out so section renderers stay synchronous and dumb. Every
 * store has a layout (the content service materializes a starter default), so there is no legacy branch.
 */
export interface HomeLayoutData {
    layout: PageLayoutData;
    resolved: Record<string, SectionResolvedData>;
    /** Set when a preview token rendered the draft — the builder's canvas. */
    preview: boolean;
}

const asNumber = (value: unknown, fallback: number): number =>
    typeof value === 'number' && Number.isFinite(value) ? value : fallback;

/** One products section's source, resolved. Degrades to an empty list — the section collapses, not the page. */
const productsFor = async (ctx: Awaited<ReturnType<typeof getStoreContext>>, section: LayoutSectionData):
    Promise<SectionResolvedData> => {
    const source = section.props.source as ProductSourceRef | undefined;
    const limit = asNumber(section.props.limit, 8);
    try {
        if (source?.type === 'group' && source.code) {
            const group = await ProductService.getProductByGroup(ctx, source.code);
            const products = toListingProducts(group?.products)?.slice(0, limit) ?? [];
            return {products: {title: group?.description?.name, products}};
        }
        if (source?.type === 'category' && source.code) {
            const category = await CategoryService.getCategory(ctx, source.code);
            const page = await ProductSearchService.search(ctx,
                {...defaultSearchQuery(), categoryIds: [category.id], count: limit, sort: 'newest'}, false);
            return {products: {products: toListingProducts(page.content as Product[]) ?? []}};
        }
        if (source?.type === 'newest') {
            const page = await ProductSearchService.search(ctx,
                {...defaultSearchQuery(), count: limit, sort: 'newest'}, false);
            return {products: {products: toListingProducts(page.content as Product[]) ?? []}};
        }
    } catch (error) {
        // one section's source failing must not cost the landing page
        console.warn('products source failed', source?.type, error);
    }
    return {products: {products: []}};
};

const resolveSection = async (ctx: Awaited<ReturnType<typeof getStoreContext>>, section: LayoutSectionData):
    Promise<SectionResolvedData | undefined> => {
    switch (section.kind) {
        case 'products':
            return productsFor(ctx, section);
        case 'categories': {
            const page = await CategoryService.getCategories(ctx);
            const limit = asNumber(section.props.limit, 6);
            return {categories: (page?.content ?? []).filter(c => c.visible !== false).slice(0, limit)};
        }
        case 'faq': {
            const group = typeof section.props.group === 'string' ? section.props.group : undefined;
            return {faq: await ContentService.getFaq(ctx, group)};
        }
        case 'posts': {
            const category = typeof section.props.category === 'string' ? section.props.category : undefined;
            return {posts: await ContentService.getPosts(ctx, {count: asNumber(section.props.limit, 3), category})};
        }
        default:
            // inline kinds (hero, richtext, usp, …) carry everything in props/items/text
            return undefined;
    }
};

/** The guard around every section's data: a failing source collapses that section, never the page. */
const resolveSectionSafely = async (ctx: Awaited<ReturnType<typeof getStoreContext>>,
                                    section: LayoutSectionData): Promise<SectionResolvedData | undefined> => {
    try {
        return await resolveSection(ctx, section);
    } catch (error) {
        console.warn('section data failed', section.kind, section.id, error);
        return undefined;
    }
};

export const loadHome = cache(async (previewToken?: string): Promise<HomeLayoutData> => {
    const ctx = await getStoreContext();
    const layout = await ContentService.getPageLayout(ctx, 'HOME', previewToken);
    const entries = await Promise.all(layout.sections.map(async section =>
        [section.id, await resolveSectionSafely(ctx, section)] as const));
    const resolved: Record<string, SectionResolvedData> = {};
    for (const [id, data] of entries) {
        if (data) {
            resolved[id] = data;
        }
    }
    return {layout, resolved, preview: !!previewToken};
});
