import { DefaultPageParams } from "@/types/params"
import { OrderDetails } from "@/shared/Customer/OrderDetails"
import { extractSsrContext } from "@/services/store-context-ssr-utils"

interface OrderPageParams extends DefaultPageParams {
    id: string;
}

export default async function Page({ params }: { params: Promise<OrderPageParams> }) {
    const aparams = await params
    aparams.storeContext = await extractSsrContext()

    return (
        <div className="flex-grow bg-background">
            <OrderDetails storeContext={aparams.storeContext} orderId={Number(aparams.id)} />
        </div>
    )
}
