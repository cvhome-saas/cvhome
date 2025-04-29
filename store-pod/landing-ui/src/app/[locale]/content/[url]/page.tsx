import {ContentPageParams} from "@/types/params";
import {CONTENT_PAGE_PATH} from "@/types/constant";
import {PageLoader} from "@/app/[locale]/_components/PageLoader";


export default async function Page({params}: { params: Promise<ContentPageParams> }) {
    return PageLoader({params, path: CONTENT_PAGE_PATH});
}