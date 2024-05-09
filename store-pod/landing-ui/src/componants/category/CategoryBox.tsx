import {Category} from "@/types/category";
import {Link} from "@/navigation";
import {Manufacturer, ProductGroupPage} from "@/types/product-groups";
import React from "react";
import {TabProductContent} from "@/componants/product/TabProductContent";
import {StoreContext} from "@/types/store-context";

export const CategoryBox = ({storeContext, category, manufacturers, productPage, t}: {
    storeContext: StoreContext,
    category: Category,
    manufacturers: Manufacturer[],
    productPage: ProductGroupPage,
    t: { [key: string]: string }
}) => {
    return <div className="shop-area pt-95 pb-100">
        <div className="container">
            <div className="row">
                <div className="col-lg-3 order-2 order-lg-1">
                    <div className="sidebar-style mr-30">
                        <CategoryList category={category} t={t}/>
                        <ManufacturerList manufacturers={manufacturers} t={t}/>
                    </div>
                </div>
                <div className="col-lg-9 order-1 order-lg-2">
                    <TabHeading n={productPage.number} recordsTotal={productPage.number} t={t}/>
                    <div className="shop-bottom-area mt-35">
                        <div className="row grid three-column">
                            <TabProductContent storeContext={storeContext} group={productPage} t={{
                                'SKU': t['SKU'],
                                'Add to cart': t['Add to cart'],
                                'Categories': t['Categories']
                            }}/>
                        </div>
                    </div>
                    <Paginator/>
                </div>
            </div>
        </div>
    </div>

}

const CategoryList = ({category, t}: { category: Category, t: { [key: string]: string } }) => {
    return <>
        {category && category.children && category.children.length > 0 &&
            <div className="sidebar-widget">
                <h4 className="pro-sidebar-title">
                    {t['Categories']}
                </h4>
                <div className="sidebar-widget-list mt-20">
                    <ul>
                        {
                            category.children.map((it, index) => {
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
    </>

}
const ManufacturerList = ({manufacturers, t}: { manufacturers: Manufacturer[], t: { [key: string]: string } }) => {
    return <>
        <div className="sidebar-widget mt-30">
            <h4 className="pro-sidebar-title">
                {t["Styles"]}
            </h4>
            <div className="sidebar-widget-list mt-20">
                <ul>
                    <li>
                        <div className="sidebar-widget-list-left">
                            {
                                manufacturers.map(it =>
                                    <label>
                                        <input type="checkbox" name="manufacture" value={it.id}/>
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
    </>
}
const Paginator = () => {
    return <div className="pro-pagination-style text-center mt-30">
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
}
const TabHeading = ({n, recordsTotal, t}: { n: number, recordsTotal: number, t: { [key: string]: string } }) => {
    return <div className="shop-top-bar mb-35">
        <div className="select-shoing-wrap"><p>
            {t['Showing']}{' '}{n}{' '}{t['of']}{' '}{recordsTotal}{' '}{t['result']}
        </p>
        </div>
        <div className="shop-tab">
            <button><i className="fa fa-th-large"></i></button>
            <button><i className="fa fa-th"></i></button>
            <button><i className="fa fa-list-ul"></i></button>
        </div>
    </div>

}