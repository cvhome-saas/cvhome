import {useTranslations} from "next-intl";

export default function CartPage() {
    const t = useTranslations();
    return <h1>{t('Metadata.Cookie Consent')}</h1>;
}