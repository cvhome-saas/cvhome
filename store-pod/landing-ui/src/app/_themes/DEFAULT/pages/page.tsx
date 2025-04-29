import {HomePageParams} from "@/types/params";
import CoverFlow from "@/app/_themes/DEFAULT/componantes/SlideShow/CoverFlow";
import {ProductService} from "@/services/product-service";
import ProductSwiperGrid from "@/app/_themes/DEFAULT/componantes/ProductGrid/ProductSwiperGrid";
import ProductGrid from "@/app/_themes/DEFAULT/componantes/ProductGrid/ProductGrid";
import {StoreService} from "@/services/store-service";
import React from 'react';
import {SectionTitle} from "@/app/_themes/DEFAULT/componantes/common/SectionTitle";
import {getTranslations} from "next-intl/server";


export default async function Page({params}: { params: HomePageParams }) {
    const t = await getTranslations('PAGE.HOME');
    const [
        featuredItems,
        newlyAdded,
        homePageProduct,
        recommended,
        store
    ] = await Promise.all([
        ProductService.getFeaturedItemsProductGroup(params.storeContext),
        ProductService.getNewlyAddedProductGroup(params.storeContext),
        ProductService.getHomePageProductGroup(params.storeContext),
        ProductService.getRecommendedProductGroup(params.storeContext),
        StoreService.getStore(params.storeContext)
    ]);
    return (
        <div className="flex-grow bg-background">
            <div className="p-6">
                {
                    store && store.sliderImages && store.sliderImages.length > 0 &&
                    <CoverFlow store={store}/>
                }
                {
                    featuredItems && featuredItems.products && featuredItems.productGroup.active && featuredItems.products.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('FEATURED_ITEMS')}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={featuredItems.products}/>
                    </div>
                }
                {
                    newlyAdded && newlyAdded.products && newlyAdded.productGroup.active && newlyAdded.products.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('NEWLY_ADDED')}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={newlyAdded.products}/>
                    </div>
                }
                {
                    homePageProduct && homePageProduct.products && homePageProduct.productGroup.active && homePageProduct.products.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('HOME_PRODUCT')}/>
                        <ProductGrid storeContext={params.storeContext} products={homePageProduct.products}/>
                    </div>
                }
                {
                    recommended && recommended.products && recommended.productGroup.active && recommended.products.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('RECOMMENDED')}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={recommended.products}/>
                    </div>
                }
            </div>
        </div>
    )
}