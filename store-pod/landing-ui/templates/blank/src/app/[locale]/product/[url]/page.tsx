import {ProductPageParams} from "@/types/params";
import {ProductService} from "@/services/product-service";
import React from "react";
import {extractSsrContext} from "@/utils/store-context-ssr-utils";


export default async function Page({params}: { params: Promise<ProductPageParams> }) {
    const aparams = await params;
    aparams.storeContext = await extractSsrContext();
    const p = await ProductService.getProductByUrl(aparams.url, aparams.storeContext);
    return <div className="flex-grow bg-background">
        <div className="p-6">
            product {p?.description?.name}
        </div>
    </div>
}