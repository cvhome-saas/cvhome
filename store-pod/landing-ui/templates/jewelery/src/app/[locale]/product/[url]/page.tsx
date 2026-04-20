import {ProductPageParams} from "@/types/params";
import {ProductService} from "@/services/product-service";
import {Breadcrumb} from "@/shared/Common/Breadcrumb";
import {ProductDetails} from "@/shared/ProductDetails/ProductDetails";
import {getTranslations} from "next-intl/server";
import React from "react";
import {BreadcrumbItem} from "@/types/bread-crumb";
import {extractSsrContext} from "@/services/store-context-ssr-utils";
import {RelatedProducts} from "@/shared/ProductDetails/RelatedProducts";


export default async function Page({params}: { params: Promise<ProductPageParams> }) {
    const aparams = await params;
    aparams.storeContext = await extractSsrContext();
    const t = await getTranslations('PAGE.PRODUCT');
    const p = await ProductService.getProductByUrl(aparams.url, aparams.storeContext);

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
                <RelatedProducts storeContext={aparams.storeContext} productId={p.id}/>
            </>}
        </div>

    </div>


}