import {LayoutParams} from "@/types/params";
import {CategoryService} from "@/services/category-service";
import {ContentService} from "@/services/content-service";
import {HeaderBox} from "@/componantes/Layout/HeaderBox";
import {Box} from "@/types/content";

export default async function Header({params}: { params: LayoutParams }) {
    params.categories = await CategoryService.getCategories(params.storeContext);
    params.contents = await ContentService.getContents(params.storeContext);
    const headerBox: Box | undefined = await ContentService.getBox(params.storeContext, "header-message");
    return <HeaderBox params={params} headerBox={headerBox}/>
}