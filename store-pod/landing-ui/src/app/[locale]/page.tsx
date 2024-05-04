import {NewsLetter} from "@/componants/news-letter/NewsLetter";
import {TabProduct} from "@/wrappers/product/TabProduct";
import {cookies, headers} from "next/headers";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {getTranslations} from "next-intl/server";
import {Link} from "@/navigation";
import {StoreService} from "@/services/store-service";
import {Store} from "@/types/store";

export default async function Home({params}: { params: { locale: string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(), params.locale);
    const t = await getTranslations("Home");
    return (
        <>
            <div className="site-blocks-cover">
                <div className="container">
                    <div className="row">
                        <div className="col-md-4 ml-auto order-md-2 align-self-start">
                            <div className="site-block-cover-content">
                                <h2 className="sub-title">
                                    {t('Pitch1')}

                                </h2>
                                <h1>
                                    {t('Pitch2')}

                                </h1>
                                <p>
                                    <Link href={"/"} className="btn btn-black rounded-0">
                                        {t('Shop Now')}
                                    </Link>
                                </p>
                            </div>
                        </div>
                        <div className="col-md-8 order-1 align-self-end">
                            <img src="assets/img/banner/table.png" alt="banner" className="img-fluid"/>
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
