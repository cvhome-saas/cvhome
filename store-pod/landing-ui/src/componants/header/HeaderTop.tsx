'use server'
import {StoreCallUs} from "@/componants/header/sub-components/StoreCallUs";
import {StoreOffers} from "@/componants/header/sub-components/StoreOffers";
import {LanguageListSelector} from "@/componants/header/sub-components/LanguageListSelector";
import {CurrentLanguage} from "@/componants/header/sub-components/CurrentLanguage";
import {useTranslations} from "next-intl";
import {Store} from "@/types/store";


export const HeaderTop = async ({store}: { store: Store }) => {
    const t = useTranslations('Language');
    const languages: Language[] = store.supportedLanguages.map(it => {
        return {
            lang: it.code,
            name: t(it.code)
        }
    });
    return (
        <div className="header-top-wap ">
            <div className="language-currency-wrap">
                <div className="same-language-currency language-style">
                    <span>
                        <CurrentLanguage store={store}/>
                        <i className="fa fa-angle-down"></i>
                    </span>
                    <div className="lang-car-dropdown">
                        <LanguageListSelector languages={languages} store={store}/>
                    </div>
                </div>
                <StoreCallUs store={store}/>
            </div>
            <StoreOffers/>
        </div>
    );
}

export interface Language {
    lang: string,
    name: string
}

