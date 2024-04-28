import {StoreCallUs} from "@/componants/header/sub-components/StoreCallUs";
import {StoreOffers} from "@/componants/header/sub-components/StoreOffers";
import {LanguageListSelector} from "@/componants/header/sub-components/LanguageListSelector";
import {CurrentLanguage} from "@/componants/header/sub-components/CurrentLanguage";
import {useTranslations} from "next-intl";
import {locales} from "@/navigation";
import {Store} from "@/types/store";


export const NEXT_LOCALE_KEY = 'NEXT_LOCALE';

export const HeaderTop = ({store}:{store:Store}) => {
    const t = useTranslations('Language');
    const languages: Language[] = locales.map(it => {
        return {
            lang: it,
            name: t(it)
        }
    });
    return (
        <div className="header-top-wap ">
            <div className="language-currency-wrap">
                <div className="same-language-currency language-style">
                    <span>
                        <CurrentLanguage/>
                        <i className="fa fa-angle-down"></i>
                    </span>
                    <div className="lang-car-dropdown">
                        <LanguageListSelector languages={languages}/>
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

