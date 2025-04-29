import "./globals.css";
import {DefaultParams} from "@/types/params";
import {StoreService} from "@/services/store-service";
import {extractSsrContext} from "@/utils/store-context-ssr-utils";
import {Store} from "@/types/store";
import {StoreNotFoundLayout} from "@/app/[locale]/_layout/StoreNotFoundLayout";
import {StoreLayout} from "@/app/[locale]/_layout/StoreLayout";

export default async function
    LocaleLayout({children, params}: {
    children: React.ReactNode;
    params: Promise<DefaultParams>;
}) {

    const p = await params;
    p.storeContext = await extractSsrContext();
    const store: Store | undefined = await StoreService.getStore(p.storeContext);
    if (store) {
        p.store = store;
        return <StoreLayout p={p} children={children}/>
    } else {
        return <StoreNotFoundLayout p={p}/>
    }
}


