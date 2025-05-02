import {headers} from "next/headers";
import {getLocale} from "next-intl/server";
import {StoreContext} from "@/types/store-context";
import {FALLBACK_STORE_ID} from "@/types/constant";

export const extractSsrContext = async (): Promise<StoreContext> => {
    const h = await headers();
    const locale = await getLocale();
    const store = h.get("Store-Id") || process.env.FALLBACK_STORE_ID || FALLBACK_STORE_ID;
    return {
        store: store,
        locale,
    }
}