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
    return {
        store: headers.get("store") || "",
        host: headers.get("x-forwarded-host") || "",
        schema: headers.get("x-forwarded-proto") || "",
        local: local ? local : cookie.get('NEXT_LOCALE' as any)?.value,
        baseUrl: headers.get("x-forwarded-proto") || "" + "://" + headers.get("x-forwarded-host") || "",
    }
}
export const baseServiceUrl = (storeContext: StoreContext, service: string): string => {
    return  "https://store-pod-1.asrevo.com/" + service;
}

export const storeBaseServiceUrl = (storeContext: StoreContext): string => {
    return baseServiceUrl(storeContext, "store");
}