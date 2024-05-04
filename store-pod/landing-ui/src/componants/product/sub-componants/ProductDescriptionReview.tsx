import {Product} from "@/types/product-groups";
import {useTranslations} from "next-intl";

export const ProductDescriptionReview = ({product}: { product: Product }) => {

    const t = useTranslations('Product');
    return <div className="description-review-area pb-90">
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
}