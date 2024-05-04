'use server'
import {NavMenu} from "@/componants/header/NavMenu";
import {HeaderTop} from "@/componants/header/HeaderTop";
import {getTranslations} from "next-intl/server";
import {Store} from "@/types/store";
import {NavCart} from "@/componants/header/NavCart";
import {ContentPage} from "@/types/content";
import {CategoryPage} from "@/types/category";
import {StoreContext} from "@/types/store-context";
import {CategoryService} from "@/services/category-service";
import {ContentService} from "@/services/content-service";
import {Link} from "@/navigation";

export const Header = async ({storeContext, store}: { storeContext: StoreContext, store: Store }) => {
    const categoriesResult: CategoryPage = await CategoryService.getCategories(storeContext);

    const contentResult: ContentPage = await ContentService.getContents(storeContext);

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
                                <Link href={"/"}>
                                    <img src={store.logo.path} alt={""}/>
                                </Link>
                            </div>
                        </div>
                        <div className="col-xl-8 col-lg-8 d-none d-lg-block">
                            <NavMenu categories={categoriesResult.categories} contents={contentResult.items}
                                     home={t('Home')}/>
                        </div>
                        <div className="col-xl-2 col-lg-2 col-md-6 col-8">
                            <div className="header-right-wrap ">
                                <NavCart storeContext={storeContext} store={store}/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    </>
}
