import {BreadCrumb} from "@/componants/product/sub-componants/BreadCrumb";
import React from "react";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {cookies, headers} from "next/headers";
import {CategoryService} from "@/services/category-service";
import {Category} from "@/types/category";
import {ProductCategory} from "@/services/product-category";
import {Manufacturer} from "@/types/product-groups";

export default async function CategoryPage({params}: { params: { locale: string, category: string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(), params.locale);
    const category: Category = await CategoryService.getCategory(storeContext, params.category);
    const manufacturer: Manufacturer[] = await ProductCategory.getManufacturers(storeContext, category.id);
    const productPage = await ProductCategory.getProducts(storeContext, category.id, manufacturer[0].id);
    console.log(productPage);
    return <>
        <BreadCrumb name={category.description.name} t={{'Home': 'Home'}}/>
        <div className="container">
            <div className="row">
                <div className="pro-details-list">

                </div>
            </div>
        </div>
    </>
}