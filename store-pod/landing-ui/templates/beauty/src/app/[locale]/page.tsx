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
                    featuredItems && featuredItems.active && featuredItems.products && featuredItems.description && featuredItems.products.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={featuredItems.description.name}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={featuredItems.products}/>
                    </div>
                }
                {
                    newlyAdded && newlyAdded.active && newlyAdded.products && newlyAdded.description && newlyAdded.products.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={newlyAdded.description.name}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={newlyAdded.products}/>
                    </div>
                }
                {
                    homePageProduct && homePageProduct.active && homePageProduct.products && homePageProduct.description && homePageProduct.products.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={homePageProduct.description.name}/>
                        <ProductGrid storeContext={params.storeContext} products={homePageProduct.products}/>
                    </div>
                }
                {
                    recommended && recommended.active && recommended.products && recommended.description && recommended.products.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={recommended.description.name}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={recommended.products}/>
                    </div>
                }
            </div>
        </div>
    )
}