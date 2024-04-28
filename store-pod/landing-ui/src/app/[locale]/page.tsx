import {NewsLetter} from "@/componants/news-letter/NewsLetter";

export default function Home({params}) {
    return (
        <>
            <div className="site-blocks-cover">
                <div className="container">
                    <div className="row">
                        <div className="col-md-4 ml-auto order-md-2 align-self-start">
                            <div className="site-block-cover-content"><h2 className="sub-title">#New Collection
                                2022</h2><h1>Imports from the world</h1><p><a href="!#"
                                                                              className="btn btn-black rounded-0">Shop
                                Now</a></p></div>
                        </div>
                        <div className="col-md-8 order-1 align-self-end"><img src="assets/img/banner/table.png"
                                                                              alt="banner" className="img-fluid"/></div>
                    </div>
                </div>
            </div>

            <div className="support-area hm9-section-padding pt-50 pb-40 ">
                <div>
                    <div className="row">
                        <div className="col-lg-2"></div>
                        <div className="col-lg-8"><img src="/assets/img/promo/promo.png" alt="promo20" width="1200"/>
                        </div>
                        <div className="col-lg-2"></div>
                    </div>
                </div>
            </div>

            <div className="product-area pt-100 pb-100 ">
                <div className="container">
                    <div className="section-title-5 text-center "><h2 className="">Featured Products</h2></div>
                    <div className="product-tab-list pt-30 pb-55 text-center nav nav-pills" role="tablist">
                        <div className="nav-item"><a href="#" role="tab" data-rb-event-key="all" aria-selected="true"
                                                     className="nav-link active"><h4>All</h4></a></div>
                        <div className="nav-item"><a href="#" role="tab" data-rb-event-key="phones" tabIndex="-1"
                                                     aria-selected="false" className="nav-link"><h4>phones</h4></a>
                        </div>
                        <div className="nav-item"><a href="#" role="tab" data-rb-event-key="cars" tabIndex="-1"
                                                     aria-selected="false" className="nav-link"><h4>cars</h4></a></div>
                        <div className="nav-item"><a href="#" role="tab" data-rb-event-key="ncansa" tabIndex="-1"
                                                     aria-selected="false" className="nav-link"><h4>ncansa</h4></a>
                        </div>
                    </div>
                    <div className="tab-content">
                        <div role="tabpanel" aria-hidden="false" className="fade tab-pane active show">
                            <div className="row">
                                <div className="col-xl-3 col-md-6 col-lg-4 col-sm-6 ">
                                    <div className="product-wrap-2 mb-25  ">
                                        <div className="product-img"><a href="/[local]/product/iphone"><img
                                            src="http://localhost:8080/static/products/DEFAULT/iphone/SMALL/Apple_iphone13_hero_09142021_inline.jpg.large.jpg"
                                            alt=""/></a>
                                            <div className="product-action-2"><a title="Select options"
                                                                                 href="/[local]/product/iphone"><i
                                                className="fa fa-cog"></i></a>
                                                <button className="active" title="Add to cart"><i
                                                    className="fa fa-shopping-cart"></i></button>
                                                <button title="Quick View"><i className="fa fa-eye"></i></button>
                                            </div>
                                        </div>
                                        <div className="product-content-2">
                                            <div className="title-price-wrap-2 "><h3><a
                                                href="/[local]/product/iphone">iphone</a></h3>
                                                <div className="price-2"><span>CA$1,500.00 </span></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div className="col-xl-3 col-md-6 col-lg-4 col-sm-6 ">
                                    <div className="product-wrap-2 mb-25  ">
                                        <div className="product-img"><a href="/[local]/product/fiat"><img
                                            src="http://localhost:8080/static/products/DEFAULT/fiat/SMALL/500_333.jpeg"
                                            alt=""/></a>
                                            <div className="product-action-2"><a title="Select options"
                                                                                 href="/[local]/product/fiat"><i
                                                className="fa fa-cog"></i></a>
                                                <button className="active" title="Add to cart"><i
                                                    className="fa fa-shopping-cart"></i></button>
                                                <button title="Quick View"><i className="fa fa-eye"></i></button>
                                            </div>
                                        </div>
                                        <div className="product-content-2">
                                            <div className="title-price-wrap-2 "><h3><a
                                                href="/[local]/product/fiat">fiat</a>
                                            </h3>
                                                <div className="price-2"><span>CA$200.00 </span></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div role="tabpanel" aria-hidden="true" className="fade tab-pane">
                            <div className="row">
                                <div className="col-xl-3 col-md-6 col-lg-4 col-sm-6 ">
                                    <div className="product-wrap-2 mb-25  ">
                                        <div className="product-img"><a href="/[local]/product/iphone"><img
                                            src="http://localhost:8080/static/products/DEFAULT/iphone/SMALL/Apple_iphone13_hero_09142021_inline.jpg.large.jpg"
                                            alt=""/></a>
                                            <div className="product-action-2"><a title="Select options"
                                                                                 href="/[local]/product/iphone"><i
                                                className="fa fa-cog"></i></a>
                                                <button className="active" title="Add to cart"><i
                                                    className="fa fa-shopping-cart"></i></button>
                                                <button title="Quick View"><i className="fa fa-eye"></i></button>
                                            </div>
                                        </div>
                                        <div className="product-content-2">
                                            <div className="title-price-wrap-2 "><h3><a
                                                href="/[local]/product/iphone">iphone</a></h3>
                                                <div className="price-2"><span>CA$1,500.00 </span></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div role="tabpanel" aria-hidden="true" className="fade tab-pane">
                            <div className="row">
                                <div className="col-xl-3 col-md-6 col-lg-4 col-sm-6 ">
                                    <div className="product-wrap-2 mb-25  ">
                                        <div className="product-img"><a href="/[local]/product/iphone"><img
                                            src="http://localhost:8080/static/products/DEFAULT/iphone/SMALL/Apple_iphone13_hero_09142021_inline.jpg.large.jpg"
                                            alt=""/></a>
                                            <div className="product-action-2"><a title="Select options"
                                                                                 href="/[local]/product/iphone"><i
                                                className="fa fa-cog"></i></a>
                                                <button className="active" title="Add to cart"><i
                                                    className="fa fa-shopping-cart"></i></button>
                                                <button title="Quick View"><i className="fa fa-eye"></i></button>
                                            </div>
                                        </div>
                                        <div className="product-content-2">
                                            <div className="title-price-wrap-2 "><h3><a
                                                href="/[local]/product/iphone">iphone</a></h3>
                                                <div className="price-2"><span>CA$1,500.00 </span></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div className="col-xl-3 col-md-6 col-lg-4 col-sm-6 ">
                                    <div className="product-wrap-2 mb-25  ">
                                        <div className="product-img"><a href="/[local]/product/fiat"><img
                                            src="http://localhost:8080/static/products/DEFAULT/fiat/SMALL/500_333.jpeg"
                                            alt=""/></a>
                                            <div className="product-action-2"><a title="Select options"
                                                                                 href="/[local]/product/fiat"><i
                                                className="fa fa-cog"></i></a>
                                                <button className="active" title="Add to cart"><i
                                                    className="fa fa-shopping-cart"></i></button>
                                                <button title="Quick View"><i className="fa fa-eye"></i></button>
                                            </div>
                                        </div>
                                        <div className="product-content-2">
                                            <div className="title-price-wrap-2 "><h3><a
                                                href="/[local]/product/fiat">fiat</a>
                                            </h3>
                                                <div className="price-2"><span>CA$200.00 </span></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div role="tabpanel" aria-hidden="true" className="fade tab-pane">
                            <div className="row">
                                <div className="col-xl-3 col-md-6 col-lg-4 col-sm-6 ">
                                    <div className="product-wrap-2 mb-25  ">
                                        <div className="product-img"><a href="/[local]/product/iphone"><img
                                            src="http://localhost:8080/static/products/DEFAULT/iphone/SMALL/Apple_iphone13_hero_09142021_inline.jpg.large.jpg"
                                            alt=""/></a>
                                            <div className="product-action-2"><a title="Select options"
                                                                                 href="/[local]/product/iphone"><i
                                                className="fa fa-cog"></i></a>
                                                <button className="active" title="Add to cart"><i
                                                    className="fa fa-shopping-cart"></i></button>
                                                <button title="Quick View"><i className="fa fa-eye"></i></button>
                                            </div>
                                        </div>
                                        <div className="product-content-2">
                                            <div className="title-price-wrap-2 "><h3><a
                                                href="/[local]/product/iphone">iphone</a></h3>
                                                <div className="price-2"><span>CA$1,500.00 </span></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <NewsLetter/>
        </>
    );
}
