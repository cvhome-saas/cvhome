import {NewsLetter} from "@/componants/news-letter/NewsLetter";
import {TabProduct} from "@/wrappers/product/TabProduct";
import {cookies, headers} from "next/headers";
import {extractStoreContext, StoreContext} from "@/types/store-context";

export default async function Home({params}: { params: { locale: string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(),params.locale);

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
                                                                              alt="banner" className="img-fluid"/>
                        </div>
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
            <TabProduct storeContext={storeContext}/>
            <NewsLetter/>
        </>
    );
}
