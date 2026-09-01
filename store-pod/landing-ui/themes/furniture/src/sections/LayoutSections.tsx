import {getTranslations} from 'next-intl/server';
import {slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Hero} from './Hero';
import {DirectoryBoard, type Department} from '../components/DirectoryBoard';
import {floors} from '../components/floors';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * furniture's layout-section registry — the building. The hero is the window onto the current floor;
 * `categories` sections are the building's directory board (the theme's signature piece), one row per
 * department in the merchant's own order; product sections are the numbered plates.
 */

async function HeroSection({ctx, section}: SectionRenderProps) {
    const tc = await getTranslations('COMMON');
    const slides = slidesAsBanners(section.items);
    const caption = section.text.heading ?? ctx.store.name;
    return (
        <div className="min-h-[15rem]">
            <Hero slides={slides} caption={caption} planCaption={tc('DEPARTMENT')}/>
        </div>
    );
}

async function CategoriesSection({ctx, section, data}: SectionRenderProps) {
    const tc = await getTranslations('COMMON');
    const t = await getTranslations('PAGE.HOME');
    const departments: Department[] = floors(data?.categories ? [...data.categories] : undefined)
        .map(c => ({code: c.code, name: c.description.name, href: `/category/${c.description.friendlyUrl}`, count: c.productCount ?? 0}));
    if (departments.length === 0) return null;
    const {store} = ctx;
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : NaN;
    const facts = [store.address?.city, Number.isNaN(year) ? undefined : t('SINCE', {year})].filter(Boolean) as string[];
    return (
        <DirectoryBoard
            title={section.text.title ?? store.name}
            facts={facts}
            departments={departments}
            headings={{floor: tc('FLOOR'), department: tc('DEPARTMENT'), items: tc('ITEMS')}}
            className="min-w-0"/>
    );
}

async function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const t = await getTranslations('PAGE.HOME');
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section className="min-w-0">
            {title && <SectionHeading title={title} meta={t('ITEMS_COUNT', {count: products.length})}/>}
            {rail
                ? <ProductRail products={products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroSection, carousel: HeroSection},
    categories: {grid: CategoriesSection, pills: CategoriesSection},
    products: {rail: ProductsSection, grid: ProductsSection},
};
