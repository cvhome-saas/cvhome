import {Product} from "@/types/product-groups";
import ProductImageGallery from "@/componants/product/sub-componants/ProductImageGallery";
import {ProductRating} from "@/componants/product/sub-componants/ProductRating";
import {Suspense} from "react";
import {Link} from "@/navigation";
import {useTranslations} from "next-intl";

export const ProductItem = ({product}: { product: Product }) => {
    const t = useTranslations('Product');

    return <div className="shop-area pt-100 pb-100">
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

}