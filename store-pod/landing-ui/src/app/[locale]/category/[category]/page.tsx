import {BreadCrumb} from "@/componants/product/sub-componants/BreadCrumb";
import React from "react";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {cookies, headers} from "next/headers";
import {CategoryService} from "@/services/category-service";
import {Category} from "@/types/category";
import {ProductCategory} from "@/services/product-category";
import {Manufacturer, ProductGroupPage} from "@/types/product-groups";
import {TabProductContent} from "@/componants/product/TabProductContent";
import {getTranslations} from "next-intl/server";
import {Link} from "@/navigation";

export default async function CategoryPage({params}: { params: { locale: string, category: string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(), params.locale);
    const category: Category = await CategoryService.getCategory(storeContext, params.category);
    const manufacturer: Manufacturer[] = await ProductCategory.getManufacturers(storeContext, category.id);
    const productPage: ProductGroupPage = await ProductCategory.getProducts(storeContext, category.id, manufacturer[0].id);
    const tp = await getTranslations('Product');
    const tc = await getTranslations('Category');
    return <>
        <BreadCrumb name={category.description.name} t={{'Home': 'Home'}}/>
        <div className="shop-area pt-95 pb-100">
            <div className="container">
                <div className="row">
                    <div className="col-lg-3 order-2 order-lg-1">
                        <div className="sidebar-style mr-30">
                            {
                                category && category.children && category.children.length > 0 &&
                                <div className="sidebar-widget">
                                    <h4 className="pro-sidebar-title">
                                        {tp('Categories')}
                                    </h4>
                                    <div className="sidebar-widget-list mt-20">
                                        <ul>

                                            {
                                                category.children.map((it,index) => {
                                                    return <>
                                                        <li key={index}>
                                                            <div>
                                                                <Link prefetch={false}
                                                                      href={`/category/${it.description.friendlyUrl}`}>{it.description.name}</Link>
                                                            </div>
                                                        </li>
                                                    </>

                                                })

                                            }
                                        </ul>
                                    </div>
                                </div>
                            }
                            <div className="sidebar-widget mt-30">
                                <h4 className="pro-sidebar-title">
                                    {tc("Styles")}
                                </h4>
                                <div className="sidebar-widget-list mt-20">
                                    <ul>
                                        <li>
                                            <div className="sidebar-widget-list-left">
                                                {
                                                    manufacturer.map(it =>
                                                        <label>
                                                            <input type="checkbox" name="manufacture"
                                                                   value={it.id}/>
                                                            <span className="checkmark"></span>
                                                            {it.description.name}
                                                        </label>
                                                    )
                                                }
                                            </div>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="col-lg-9 order-1 order-lg-2">
                        <div className="shop-top-bar mb-35">
                            <div className="select-shoing-wrap"><p>
                                {tc('Showing')}{' '}{productPage.number}{' '}{tc('of')}{' '}{productPage.recordsTotal}{' '}{tc('result')}
                            </p>
                            </div>
                            <div className="shop-tab">
                                <button><i className="fa fa-th-large"></i></button>
                                <button><i className="fa fa-th"></i></button>
                                <button><i className="fa fa-list-ul"></i></button>
                            </div>
                        </div>

                        <div className="shop-bottom-area mt-35">
                            <div className="row grid three-column">
                                <TabProductContent storeContext={storeContext} group={productPage} t={{
                                    'SKU': tp('SKU'),
                                    'Add to cart': tp('Add to cart'),
                                    'Categories': tp('Categories')
                                }}/>
                            </div>
                        </div>


                        <div className="pro-pagination-style text-center mt-30">
                            <ul className="mb-0 mt-0">
                                <li className="previous disabled">
                                    <a tabIndex="0" role="button" aria-disabled="true" aria-label="Previous page"
                                       rel="prev">
                                        «
                                    </a>
                                </li>
                                <li className="page-item active">
                                    <a role="button" tabIndex="0" aria-label="Page 1 is your current page"
                                       aria-current="page">
                                        1
                                    </a>
                                </li>
                                <li className="next disabled">
                                    <a tabIndex="0" role="button" aria-disabled="true" aria-label="Next page"
                                       rel="next">
                                        »
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </>
}