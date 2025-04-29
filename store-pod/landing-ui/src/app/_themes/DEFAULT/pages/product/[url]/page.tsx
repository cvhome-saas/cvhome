import {ProductPageParams} from "@/types/params";
import {ProductService} from "@/services/product-service";
import {Breadcrumb} from "@/app/_themes/DEFAULT/componantes/Breadcrumb/Breadcrumb";
import {ProductDetails} from "@/app/_themes/DEFAULT/componantes/ProductDetails/ProductDetails";
import {getTranslations} from "next-intl/server";


export default async function Page({params}: { params: ProductPageParams }) {
    const t = await getTranslations('PAGE.PRODUCT');
    const p = await ProductService.getProductByUrl(params.url, params.storeContext);
    return <div className="flex-grow bg-background">
        <div className="p-6">
            {p && <>
                <Breadcrumb breadcrumbs={{
                    prev: [
                        {id: "1", name: t('HOME_TITLE'), href: '/'},
                    ],
                    current: {id: "0", name: p.description.name, href: `/product/${p.description.friendlyUrl}`}
                }}/>
                <div className="lg:max-w-6xl max-w-xl mx-auto pt-10">
                    <ProductDetails storeContext={params.storeContext} p={p} t={t}/>
                </div>
            </>}
        </div>

    </div>


}