import {ProductPageParams} from "@/types/params";
import {PRODUCT_PAGE_PATH} from "@/types/constant";
import {PageLoader} from "@/app/[locale]/_components/PageLoader";


export default async function Page({params}: { params: Promise<ProductPageParams> }) {
    return PageLoader({params, path: PRODUCT_PAGE_PATH});
}