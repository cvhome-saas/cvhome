import {ReadonlyHeaders} from "next/dist/server/web/spec-extension/adapters/headers";
import type {ReadonlyRequestCookies} from "next/dist/server/web/spec-extension/adapters/request-cookies";

export interface StoreContext {
    store: string,
    host: string
    schema: string,
    local: string | undefined,
    baseUrl: string,
}

export const extractStoreContext = (headers: ReadonlyHeaders, cookie: ReadonlyRequestCookies, local: string): StoreContext => {
    const proto = headers.get("x-forwarded-proto") || "";
    const host = headers.get("x-forwarded-host") || "";
    const schema= proto.includes("https") ? "https" : "http"
    const sc: StoreContext = {
        store: headers.get("store") || "",
        host: host,
        schema: schema.includes("https") ? "https" : "http",
        local: local ? local : cookie.get('NEXT_LOCALE' as any)?.value,
        baseUrl: schema + "://" + host,
    };

    console.log("********************** headers *****************")
    console.log(JSON.stringify(sc))
    return sc
}
export const baseServiceUrl = (storeContext: StoreContext, service: string): string => {
    return storeContext.schema + "://" + storeContext.host + "/" + service;
}

export const storeBaseServiceUrl = (storeContext: StoreContext): string => {
    return baseServiceUrl(storeContext, "store");
}