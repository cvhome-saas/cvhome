import {ProductService} from "@/services/product-service";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {cookies, headers} from "next/headers";
import {Product} from "@/types/product-groups";
import {ProductItem} from "@/componants/product/sub-componants/ProductItem";
import {ProductDescriptionReview} from "@/componants/product/sub-componants/ProductDescriptionReview";
import {BreadCrumb} from "@/componants/product/sub-componants/BreadCrumb";

export default async function ProductPage({params}: { params: { locale: string, 'friendly-url': string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(), params.locale);
    const product: Product = await ProductService.getProductByFriendlyUrl(storeContext, params["friendly-url"]);
    return <>
        <BreadCrumb name={product.description.name} t={{'Home': 'Home'}}/>
        <ProductItem storeContext={storeContext} product={product}/>
        <ProductDescriptionReview product={product}/>
    </>
}

