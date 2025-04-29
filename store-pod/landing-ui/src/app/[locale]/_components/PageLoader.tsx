import {extractSsrContext} from "@/utils/store-context-ssr-utils";
import {Store} from "@/types/store";
import {StoreService} from "@/services/store-service";
import {ComponentType} from "react";
import {DefaultPageParams} from "@/types/params";
import dynamic from "next/dynamic";

export async function PageLoader<T extends DefaultPageParams>({params, path}: { params: Promise<T>; path: string }) {
    const p = await params;
    p.storeContext = await extractSsrContext();
    const store: Store | undefined = await StoreService.getStore(p.storeContext);

    if (store) {
        const Theme: ComponentType<{ params: T }> = dynamic(() => import(`@/app/_themes/${store.theme}/${path}`), {
            ssr: true,
        });
        return <Theme params={p}/>;
    } else {
        return <></>;
    }
}