import {LayoutParams} from "@/types/params";
import {ContentService} from "@/services/content-service";
import * as React from "react";
import {FooterBox} from "@/componantes/Layout/FooterBox";

export default async function Footer({params}: { params: LayoutParams }) {
    params.contents = await ContentService.getContents(params.storeContext);
    return <FooterBox params={params}/>
}