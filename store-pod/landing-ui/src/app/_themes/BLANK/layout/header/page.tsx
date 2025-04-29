import {LayoutParams} from "@/types/params";
import {CategoryService} from "@/services/category-service";
import {ContentService} from "@/services/content-service";

export default async function Page({params}: { params: LayoutParams }) {
    params.categories = await CategoryService.getCategories(params.storeContext);
    params.contents = await ContentService.getContents(params.storeContext);
    return (<>header</>)
}