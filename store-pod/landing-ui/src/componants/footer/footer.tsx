import {Store} from "@/types/store";
import {useTranslations} from "next-intl";
import {ScrollToTop} from "@/componants/footer/sub-componants/ScrollToTop";
import {Subscribe} from "@/componants/footer/sub-componants/Subscribe";
import {UsefulLInks} from "@/componants/footer/sub-componants/UsefulLInks";
import {Address} from "@/componants/footer/sub-componants/Address";
import {Copyright} from "@/componants/footer/sub-componants/Copyright";

export const Footer = ({store}: { store: Store }) => {
    const t = useTranslations('Footer');
    return (
        <>
            <footer className="footer-area bg-gray pt-100 pb-70   ">
                <div className="container">
                    <div className="row">
                        <div className="col-lg-2 col-sm-3">
                            <Copyright store={store} t={(it) => t(it)}/>
                        </div>
                        <div className="col-lg-3 col-sm-3">
                            <div className="footer-widget mb-30">
                                <Address store={store} t={(it) => t(it)}/>
                            </div>
                        </div>
                        <div className="col-lg-3 col-sm-3">
                            <div className="footer-widget mb-30">
                                <UsefulLInks t={(it) => t(it)}/>
                            </div>
                        </div>
                        <div className="col-lg-4 col-sm-3">
                            <div className="footer-widget mb-30 ">
                                <Subscribe t={(it) => t(it)}/>
                            </div>
                        </div>
                    </div>
                </div>
                <ScrollToTop/>
            </footer>
        </>
    )
}