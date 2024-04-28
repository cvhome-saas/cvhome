import {useTranslations} from "next-intl";
import {NewsLetterForm} from "@/componants/news-letter/sub-componants/NewsLetterForm";

export const NewsLetter = () => {
    const t = useTranslations('NewsLetter');
    return (
        <div className="subscribe-area-3  pt-100 pb-100 ">
            <div className="container-fluid">
                <div className="row">
                    <div className="col-xl-5 col-lg-7 col-md-10 ml-auto mr-auto">
                        <div className="subscribe-style-3 text-center ">
                            <h2>{t('Subscribe to our newsletter')} </h2>
                            <p>{t('Subscribe to our newsletter to receive news on update')}</p>
                            <NewsLetterForm t={(it) => t(it)}/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}