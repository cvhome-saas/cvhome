'use server'
import {NavMenu} from "@/componants/header/NavMenu";
import {HeaderTop} from "@/componants/header/HeaderTop";
import {getTranslations} from "next-intl/server";
import {Store} from "@/types/store";
import {NavCart} from "@/componants/header/NavCart";
import Image from "next/image";
import {ContentPage} from "@/types/content";
import {CategoryPage} from "@/types/category";

export const Header = async ({store}: { store: Store }) => {

    const categoriesResult: CategoryPage = await fetch('http://localhost:8080/api/v1/category/?count=20&page=0&store=DEFAULT&lang=en')
        .then(it => it.json())

    const contentResult: ContentPage = await fetch('http://localhost:8080/api/v1/content/pages/?page=0&count=20&store=DEFAULT&lang=en')
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
                                    <Image src={store.logo.path} alt={""}/>
                                </a>
                            </div>
                        </div>
                        <div className="col-xl-8 col-lg-8 d-none d-lg-block">
                            <NavMenu categories={categoriesResult.categories} contents={contentResult.items}
                                     home={t('Home')}/>
                        </div>
                        <div className="col-xl-2 col-lg-2 col-md-6 col-8">
                            <div className="header-right-wrap ">
                                <NavCart store={store}/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    </>
}
