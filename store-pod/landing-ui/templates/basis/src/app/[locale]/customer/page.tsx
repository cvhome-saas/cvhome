import { DefaultPageParams } from "@/types/params"
import { CustomerDashboard } from "@/shared/Customer/CustomerDashboard"
import { extractSsrContext } from "@/services/store-context-ssr-utils"
import { AuthService } from "@store-front/services/auth-service"

export default async function Page({ params }: { params: Promise<DefaultPageParams> }) {
    const aparams = await params
    aparams.storeContext = await extractSsrContext()

    return (
        <div className="flex-grow bg-background">
            <CustomerDashboard storeContext={aparams.storeContext} />
        </div>
    )
}
