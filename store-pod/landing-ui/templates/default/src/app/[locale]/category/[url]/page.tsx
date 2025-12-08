import {CategoryPageParams} from "@/types/params";
import {CategoryService} from "@/services/category-service";
import {Category} from "@/types/category";
import {Breadcrumb} from "@/componantes/Common/Breadcrumb";
import {ProductCategoryFilter} from "@/componantes/Category/ProductCategoryFilter";
import {getTranslations} from "next-intl/server";
import {BreadcrumbItem} from "@/types/bread-crumb";
import {extractSsrContext} from "@/services/store-context-ssr-utils";

export default async function Page({params}: { params: Promise<CategoryPageParams> }) {
    const aparams = await params;
    aparams.storeContext = await extractSsrContext();
    const category: Category | undefined = await CategoryService.getCategory(aparams.storeContext, aparams.url);
    const t = await getTranslations('PAGE.CATEGORY');


    const current: BreadcrumbItem | undefined = category && category.description ? {
        id: "0",
        name: category.description.name,
        href: `/category/${category.description.friendlyUrl}`
    } : undefined;

    return <div className="flex-grow bg-background">
        <div className="p-6">
            {category && <>
                <Breadcrumb breadcrumbs={{
                    prev: [
                        {id: "1", name: t('HOME_TITLE'), href: '/'},
                    ],
                    current: current
                }}/>
                <div className="lg:max-w-6xl max-w-xl mx-auto pt-10">
                    <ProductCategoryFilter storeContext={aparams.storeContext} category={category}/>
                </div>
            </>}
        </div>
    </div>;
}