import {headers} from "next/headers";
import {getLocale} from "next-intl/server";
import {StoreContext} from "@/types/store-context";

export const extractSsrContext = async (): Promise<StoreContext> => {
    const h = await headers();
    const locale = await getLocale();
    const store = h.get("store") || "";
    return {
        store: store,
        locale,
    }
}