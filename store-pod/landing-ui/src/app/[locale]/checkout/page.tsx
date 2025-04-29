import {CheckoutPageParams} from "@/types/params";
import {CHECKOUT_PAGE_PATH} from "@/types/constant";
import {PageLoader} from "@/app/[locale]/_components/PageLoader";


export default async function Page({params}: { params: Promise<CheckoutPageParams> }) {
    return PageLoader({params, path: CHECKOUT_PAGE_PATH});
}