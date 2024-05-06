import {useTranslations} from "next-intl";
import {BreadCrumb} from "@/componants/product/sub-componants/BreadCrumb";
import React from "react";

export default function ContactPage() {
    const t = useTranslations('Contact');
    return <>
        <BreadCrumb name={t('Contact')} t={{'Home': 'Home'}}/>
    </>
}