import {extractSsrContext} from "@/services/store-context-ssr-utils";
import {StoreService} from "@/services/store-service";
import {CheckoutResult} from "@/shared/Checkout/CheckoutResult";

export default async function Page() {
    const storeContext = await extractSsrContext();
    const store = await StoreService.getStore(storeContext);

    return <CheckoutResult storeContext={storeContext}
                           requireLoginForOrderPlacement={store?.requireLoginForOrderPlacement ?? true}/>;
}
