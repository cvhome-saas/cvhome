import {HomePageParams} from "@/types/params";
import React from 'react';
import {getTranslations} from "next-intl/server";
import {extractSsrContext} from "@/services/store-context-ssr-utils"


export default async function Page({params}: { params: HomePageParams }) {
    const t = await getTranslations('PAGE.HOME');
    params.storeContext = await extractSsrContext();
    return (
        <div className="flex-grow bg-background">
            <div className="p-6">
                home
            </div>
        </div>
    )
}