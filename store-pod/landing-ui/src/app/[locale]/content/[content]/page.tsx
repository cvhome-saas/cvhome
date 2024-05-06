import {BreadCrumb} from "@/componants/product/sub-componants/BreadCrumb";
import React, {Suspense} from "react";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {cookies, headers} from "next/headers";
import {ContentService} from "@/services/content-service";
import {Page} from "@/types/content";

export default async function ContentPage({params}: { params: { locale: string, content: string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(), params.locale);
    const contentPage: Page = await ContentService.getPage(storeContext, params.content);
    return <>
        <BreadCrumb name={contentPage.description.name} t={{'Home': 'Home'}}/>
        <div className="container">
            <div className="row">
                <div className="pro-details-list">
                    <Suspense>
                        <div dangerouslySetInnerHTML={{__html: contentPage.description.description}}></div>
                    </Suspense>
                </div>
            </div>
        </div>
    </>
}