import {ContentService} from "@/services/content-service";
import {ContentPageParams} from "@/types/params";
import {extractSsrContext} from "@/services/store-context-ssr-utils"

export default async function Page({params}: { params: Promise<ContentPageParams> }) {
    const aparams = await params;
    aparams.storeContext = await extractSsrContext();
    const c = await ContentService.getPage(aparams.storeContext, aparams.url);
    return <div className="flex-grow bg-background">
        <div className="p-6">
            content {c?.description.name}
        </div>
    </div>
}

