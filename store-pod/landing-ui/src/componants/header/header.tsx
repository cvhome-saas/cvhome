import {NavMenu} from "@/componants/header/NavMenu";
import {HeaderTop} from "@/componants/header/HeaderTop";
import {getTranslations} from "next-intl/server";
import {Store} from "@/types/store";
import {NavCart} from "@/componants/header/NavCart";

export const Header = async ({store}:{store:Store}) => {

    const categoriesResult = await fetch('http://localhost:8080/api/v1/category/?count=20&page=0&store=DEFAULT&lang=en')
        .then(it => it.json())

    const contentResult = await fetch('http://localhost:8080/api/v1/content/pages/?page=0&count=20&store=DEFAULT&lang=en\n')
        .then(it => it.json())

    const t = await getTranslations('Nav');
    return <>
        <header className="header-area clearfix  ">
            <div className="header-padding-2 d-none d-lg-block header-top-area ">
                <div className="container-fluid">
                    <HeaderTop store={store}/>
                </div>
            </div>
            <div className="header-padding-2 sticky-bar header-res-padding clearfix ">
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-xl-2 col-lg-2 col-md-6 col-4">
                            <div className="logo">
                                <a href="/">
                                    <img src={store.logo.path}/>
                                </a>
                            </div>
                        </div>
                        <div className="col-xl-8 col-lg-8 d-none d-lg-block">
                            <NavMenu categories={categoriesResult.categories} contents={contentResult.items}
                                     home={t('Home')} />
                        </div>
                        <div className="col-xl-2 col-lg-2 col-md-6 col-8">
                            <div className="header-right-wrap ">
                                <div className="same-style account-setting d-none d-lg-block">
                                    <button className="account-setting-active"><i className="pe-7s-user-female"></i>
                                    </button>
                                    <div className="account-dropdown">
                                        <ul>
                                            <div>
                                                <li><a href="/login">Login</a></li>
                                                <li><a href="/register">Register</a></li>
                                            </div>
                                        </ul>
                                    </div>
                                </div>
                                <div className="same-style cart-wrap d-none d-lg-block">
                                    <button className="icon-cart"><i className="pe-7s-shopbag"></i><span
                                        className="count-style">1</span></button>
                                    <NavCart store={store}/>
                                </div>
                                <div className="same-style cart-wrap d-block d-lg-none"><a className="icon-cart"
                                                                                           href="/cart"><i
                                    className="pe-7s-shopbag"></i><span className="count-style">1</span></a></div>
                                <div className="same-style mobile-off-canvas d-block d-lg-none">
                                    <button className="mobile-aside-button"><i className="pe-7s-menu"></i></button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    </>
}
