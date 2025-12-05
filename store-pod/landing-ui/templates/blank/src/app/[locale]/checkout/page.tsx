import {CheckoutPageParams} from "@/types/params";
import {extractSsrContext} from "@/services/store-context-ssr-utils"

export default async function Page({params}: { params: Promise<CheckoutPageParams> }) {
    const aparams = await params;
    aparams.storeContext = await extractSsrContext();
    return <div className="flex-grow bg-background">
        <div className="p-6">
            checkout
        </div>
    </div>;

}