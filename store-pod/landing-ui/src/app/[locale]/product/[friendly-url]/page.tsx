import {ProductService} from "@/services/product-service";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {cookies, headers} from "next/headers";
import {Product} from "@/types/product-groups";
import {getTranslations} from "next-intl/server";
import {ProductItem} from "@/componants/product/sub-componants/ProductItem";
import {ProductDescriptionReview} from "@/componants/product/sub-componants/ProductDescriptionReview";
import {ProductBreadCrumb} from "@/componants/product/sub-componants/ProductBreadCrumb";

export default async function ProductPage({params}: { params: { locale: string, 'friendly-url': string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(), params.locale);
    const product: Product = await ProductService.getProductByFriendlyUrl(storeContext, params["friendly-url"]);
    const t = await getTranslations('Product');
    return (
        <>
            <ProductBreadCrumb product={product}/>
            <ProductItem product={product}/>
            <ProductDescriptionReview product={product}/>
        </>
    )
}

