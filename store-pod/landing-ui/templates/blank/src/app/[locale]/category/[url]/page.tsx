import {CategoryPageParams} from "@/types/params";
import {CategoryService} from "@/services/category-service";
import {Category} from "@/types/category";
import {extractSsrContext} from "@/services/store-context-ssr-utils"

export default async function Page({params}: { params: Promise<CategoryPageParams> }) {
    const aparams = await params;
    aparams.storeContext = await extractSsrContext();
    const category: Category | undefined = await CategoryService.getCategory(aparams.storeContext, aparams.url);
    return <div className="flex-grow bg-background">
        <div className="p-6">
            category {category?.description?.name}
        </div>
    </div>;
}