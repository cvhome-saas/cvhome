import {useTranslations} from "next-intl";

export default function ContentPage() {
    const t = useTranslations();
    return <h1>{t('Metadata.Cookie Consent')}</h1>;
}