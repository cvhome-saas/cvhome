import {ProductPageParams} from "@/types/params";
import {ProductService} from "@/services/product-service";
import {Breadcrumb} from "@/app/_themes/DEFAULT/componantes/Breadcrumb/Breadcrumb";
import {ProductDetails} from "@/app/_themes/DEFAULT/componantes/ProductDetails/ProductDetails";
import {getTranslations} from "next-intl/server";
import {SectionTitle} from "@/app/_themes/DEFAULT/componantes/common/SectionTitle";
import ProductSwiperGrid from "@/app/_themes/DEFAULT/componantes/ProductGrid/ProductSwiperGrid";
import React from "react";
import {BreadcrumbItem} from "@/types/bread-crumb";


export default async function Page({params}: { params: ProductPageParams }) {
    const t = await getTranslations('PAGE.PRODUCT');
    const p = await ProductService.getProductByUrl(params.url, params.storeContext);
    const related = p ? await ProductService.getRelatedProductGroup(params.storeContext, p.id) : undefined;

    const current: BreadcrumbItem | undefined = p && p.description ? {
        id: "0",
        name: p.description.name,
        href: `/product/${p.description.friendlyUrl}`
    } : undefined;
    return <div className="flex-grow bg-background">
        <div className="p-6">
            {p && <>
                <Breadcrumb breadcrumbs={{
                    prev: [
                        {id: "1", name: t('HOME_TITLE'), href: '/'},
                    ],
                    current: current
                }}/>
                <div className="lg:max-w-6xl max-w-xl mx-auto pt-10">
                    <ProductDetails storeContext={params.storeContext} p={p} t={t}/>
                </div>
                {
                    related && related.content && related.productGroup && related.productGroup.active && related.content.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('RELATED_PRODUCTS')}/>
                        <ProductSwiperGrid storeContext={params.storeContext} products={related.content}/>
                    </div>
                }
            </>}
        </div>

    </div>


}