import {DefaultPageParams} from "@/types/params"
import {CustomerDashboard} from "@/shared/Customer/CustomerDashboard"
import {extractSsrContext} from "@/services/store-context-ssr-utils"
import {Secured} from "@/shared/Common/Secured"

export default async function Page({params}: { params: Promise<DefaultPageParams> }) {
    const aparams = await params
    aparams.storeContext = await extractSsrContext()

    return (
        <div className="flex-grow bg-background">
            <Secured storeContext={aparams.storeContext}>
                <CustomerDashboard storeContext={aparams.storeContext}/>
            </Secured>
        </div>
    )
}
