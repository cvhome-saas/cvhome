import {CategoryPageParams} from "@/types/params";
import {CATEGORY_PAGE_PATH} from "@/types/constant";
import {PageLoader} from "@/app/[locale]/_components/PageLoader";


export default async function Page({params}: { params: Promise<CategoryPageParams> }) {
    return PageLoader({params, path: CATEGORY_PAGE_PATH});
}