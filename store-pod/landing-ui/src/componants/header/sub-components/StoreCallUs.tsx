import {useTranslations} from "next-intl";
import {Store} from "@/types/store";

export const StoreCallUs = ({store}: { store: Store }) => {
    const t = useTranslations('Nav');
    return <div className="same-language-currency"><p>{t('Call-Us')} : {store.phone}</p></div>
}
