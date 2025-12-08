import {ProductPageParams} from "@/types/params";
import {ProductService} from "@/services/product-service";
import {Breadcrumb} from "@/componantes/Common/Breadcrumb";
import {ProductDetails} from "@/componantes/ProductDetails/ProductDetails";
import {getTranslations} from "next-intl/server";
import {SectionTitle} from "@/componantes/Common/SectionTitle";
import ProductSwiperGrid from "@/componantes/ProductGrid/ProductSwiperGrid";
import React from "react";
import {BreadcrumbItem} from "@/types/bread-crumb";
import {extractSsrContext} from "@/services/store-context-ssr-utils";


export default async function Page({params}: { params: Promise<ProductPageParams> }) {
    const aparams = await params;
    aparams.storeContext = await extractSsrContext();
    const t = await getTranslations('PAGE.PRODUCT');
    const p = await ProductService.getProductByUrl(aparams.url, aparams.storeContext);
    const related = p ? await ProductService.getRelatedProductGroup(aparams.storeContext, p.id) : undefined;

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
                    <ProductDetails storeContext={aparams.storeContext} p={p} t={t}/>
                </div>
                {
                    related && related.content && related.productGroup && related.productGroup.active && related.content.length > 0 &&
                    <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                        <SectionTitle title={t('RELATED_PRODUCTS')}/>
                        <ProductSwiperGrid storeContext={aparams.storeContext} products={related.content}/>
                    </div>
                }
            </>}
        </div>

    </div>


}