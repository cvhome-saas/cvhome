import {BreadCrumb} from "@/componants/product/sub-componants/BreadCrumb";
import React from "react";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {cookies, headers} from "next/headers";
import {CategoryService} from "@/services/category-service";
import {Category} from "@/types/category";
import {ProductCategory} from "@/services/product-category";
import {Manufacturer, ProductGroupPage} from "@/types/product-groups";
import {getTranslations} from "next-intl/server";
import {CategoryBox} from "@/componants/category/CategoryBox";


export default async function CategoryPage({params}: { params: { locale: string, category: string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(), params.locale);
    const category: Category = await CategoryService.getCategory(storeContext, params.category);
    const manufacturers: Manufacturer[] = await ProductCategory.getManufacturers(storeContext, category.id);
    const productPage: ProductGroupPage = await ProductCategory.getProducts(storeContext, category.id);
    const tp = await getTranslations('Product');
    const tc = await getTranslations('Category');
    return <>
        <BreadCrumb name={category.description.name} t={{'Home': 'Home'}}/>
        <CategoryBox storeContext={storeContext} category={category} manufacturers={manufacturers}
                     productPage={productPage} t={{
            'SKU': tp('SKU'),
            'Add to cart': tp('Add to cart'),
            'Categories': tp('Categories'),
            'Styles': tc('Styles'),
            'Showing': tc('Showing'),
            'of': tc('of'),
            'result': tc('result'),
        }}/>
    </>
}
