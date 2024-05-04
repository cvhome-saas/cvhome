import {useTranslations} from "next-intl";

export default function CategoryPage() {
    const t = useTranslations();
    return <h1>{t('Metadata.Cookie Consent')}</h1>;
}