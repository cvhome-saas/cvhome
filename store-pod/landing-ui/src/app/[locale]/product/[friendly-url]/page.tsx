import {ProductService} from "@/services/product-service";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {cookies, headers} from "next/headers";
import {Link} from "@/navigation";
import {Product} from "@/types/product-groups";
import {ProductRating} from "@/componants/product/sub-componants/ProductRating";
import {getTranslations} from "next-intl/server";
import ProductImageGallery from "@/componants/product/sub-componants/ProductImageGallery";
import {Suspense} from "react";

export default async function ProductPage({params}: { params: { locale: string, 'friendly-url': string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(),params.locale);
    const product: Product = await ProductService.getProductByFriendlyUrl(storeContext, params["friendly-url"]);
    const t = await getTranslations('Product');
    return (
        <>
            <div className="breadcrumb-area pt-35 pb-35 bg-gray-3">
                <div className="container">
                    <div className="breadcrumb-content text-center"><span>
                        <span>
                            <Link href={"/"} aria-current="page" className="active">{t('Home')}</Link>
                            <span>/</span>
                        </span>
                        <span>
                        <Link href={`/product/${product.description.friendlyUrl}`}>
                            {product.description.name}
                        </Link>
                        </span>
                    </span></div>
                </div>
            </div>


            <div className="shop-area pt-100 pb-100">
                <div className="container">
                    <div className="row">
                        <div className="col-lg-6 col-md-6">
                            <ProductImageGallery images={product.images}/>
                            <div className="product-small-image-wrapper mt-15"></div>
                        </div>
                        <div className="col-lg-6 col-md-6">
                            <div className="product-details-content ml-70">
                                <h2>
                                    {product.description.name}
                                </h2>
                                <div className="product-details-price">
                                    <span>
                                        {product.productPrice.finalPrice}
                                    </span>
                                </div>
                                <div className="pro-details-rating-wrap">
                                    <div className="pro-details-rating">
                                        <ProductRating ratingValue={product.rating}/>
                                    </div>
                                </div>
                                <div className="pro-details-list">
                                    <Suspense>
                                        <div dangerouslySetInnerHTML={{__html: product.description.description}}></div>
                                    </Suspense>
                                </div>
                                <div className="pro-details-size-color"></div>
                                <div className="pro-details-quality">
                                    <div className="cart-plus-minus">
                                        <button className="dec qtybutton">-</button>
                                        <input className="cart-plus-minus-box" type="number"/>
                                        <button className="inc qtybutton">+</button>
                                    </div>
                                    <div className="pro-details-cart btn-hover">
                                        <button> {t('Add to cart')}</button>
                                    </div>
                                </div>
                                <div className="pro-details-meta"><span>{t('SKU')} :</span>
                                    <ul>
                                        <li key={"friendlyUrl"}>
                                            <Link href={`/product/${product.description.friendlyUrl}`}>
                                                {product.description.name}
                                            </Link>
                                        </li>
                                    </ul>
                                </div>
                                <div className="pro-details-meta"><span>{t('Categories')} :</span>
                                    <ul>
                                        {
                                            product.categories.map(it => {
                                                return (<li key={it.id}>
                                                    <Link href={`/category/${it.description.friendlyUrl}`}>
                                                        {it.description.name}
                                                    </Link>
                                                </li>)
                                            })
                                        }
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="description-review-area pb-90">
                <div className="container">
                    <div className="description-review-wrapper">
                        <div className="description-review-topbar nav nav-pills" role="tablist">
                            <div className="nav-item">
                                <a href="#" role="tab" data-rb-event-key="productDescription" aria-selected="true"
                                   className="nav-link active">
                                    {t('Description')}
                                </a>
                            </div>
                            <div className="nav-item">
                                <a href="#" role="tab" data-rb-event-key="productReviews" tabIndex={-1}
                                   aria-selected="false" className="nav-link">
                                    {t('Reviews')}(0)
                                </a>
                            </div>
                        </div>
                        <div className="description-review-bottom tab-content">
                            <div role="tabpanel" aria-hidden="false" className="fade tab-pane active show"><p></p>
                                <div className="product-anotherinfo-wrapper">
                                    <ul></ul>
                                </div>
                            </div>
                            <div role="tabpanel" aria-hidden="true" className="fade tab-pane">
                                <div className="row">
                                    <div className="col-lg-7">
                                        <div className="item-empty-area text-center">
                                            <div className="item-empty-area__icon mb-30"><i className="pe-7s-star"></i>
                                            </div>
                                            <div className="item-empty-area__text">{t('No items found in reviews')}<br/>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-lg-5">
                                        <div className="checkout-heading">
                                            <a href="/login">{t('Returning customer ? Click here to login')}</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}

