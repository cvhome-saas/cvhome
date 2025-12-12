import {HomePageParams} from "@/types/params";
import CoverFlow from "@/shared/SlideShow/CoverFlow";
import {ProductService} from "@/services/product-service";
import ProductSwiperGrid from "@/shared/ProductGrid/ProductSwiperGrid";
import ProductGrid from "@/shared/ProductGrid/ProductGrid";
import {StoreService} from "@/services/store-service";
import React from 'react';
import {SectionTitle} from "@/shared/Common/SectionTitle";
import {getTranslations} from "next-intl/server";
import {extractSsrContext} from "@/services/store-context-ssr-utils";


export default async function Page({params}: { params: HomePageParams }) {
    const t = await getTranslations('PAGE.HOME');
    params.storeContext = await extractSsrContext();
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
                    featuredItems && featuredItems.content && featuredItems.productGroup && featuredItems.productGroup.active && featuredItems.content.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('FEATURED_ITEMS')}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={featuredItems.content}/>
                    </div>
                }
                {
                    newlyAdded && newlyAdded.content && newlyAdded.productGroup && newlyAdded.productGroup.active && newlyAdded.content.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('NEWLY_ADDED')}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={newlyAdded.content}/>
                    </div>
                }
                {
                    homePageProduct && homePageProduct.content && homePageProduct.productGroup && homePageProduct.productGroup.active && homePageProduct.content.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('HOME_PRODUCT')}/>
                        <ProductGrid storeContext={params.storeContext} products={homePageProduct.content}/>
                    </div>
                }
                {
                    recommended && recommended.content && recommended.productGroup && recommended.productGroup.active && recommended.content.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('RECOMMENDED')}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={recommended.content}/>
                    </div>
                }
            </div>
        </div>
    )
}