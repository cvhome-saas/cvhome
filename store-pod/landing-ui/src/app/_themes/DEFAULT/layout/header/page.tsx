import {LayoutParams} from "@/types/params";
import {CategoryService} from "@/services/category-service";
import {ContentService} from "@/services/content-service";
import {NavHeader} from "@/app/_themes/DEFAULT/componantes/Nav/NavHeader";
import {HeaderTop} from "@/app/_themes/DEFAULT/componantes/Nav/HeaderTop";
import {Box} from "@/types/content";

export default async function Page({params}: { params: LayoutParams }) {
    params.categories = await CategoryService.getCategories(params.storeContext);
    params.contents = await ContentService.getContents(params.storeContext);
    const box: Box | undefined = await ContentService.getBox(params.storeContext, "header-message");
    return (
        <header className="bg-background">
            <HeaderTop store={params.store} box={box} locale={params.locale}/>
            <NavHeader params={params}/>
        </header>
    )
}