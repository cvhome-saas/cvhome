import {PageLoader} from "@/app/[locale]/_components/PageLoader";
import {HomePageParams} from "@/types/params";
import {HOME_PAGE_PATH} from "@/types/constant";

export default async function Page({params}: { params: Promise<HomePageParams> }) {
    return PageLoader({params, path: HOME_PAGE_PATH});
}