import {DEFAULT_STORE_POD_GATEWAY} from "@/types/constant";

export interface StoreContext {
    store: string,
    locale: string,
}

export const storeBaseServiceUrl = (service: string): string => {
    return typeof window === 'undefined' ? handleInternalServiceCall(service) : handleBrowserServiceCall(service)
}

const handleInternalServiceCall = (service: string): string => {
    const STORE_POD_GATEWAY = process.env.INTERNAL_STORE_POD_GATEWAY;
    if (STORE_POD_GATEWAY) {
        // GATEWAY env Passed
        return STORE_POD_GATEWAY + "/" + service;
    } else {
        // local or fallback if no env provided
        return DEFAULT_STORE_POD_GATEWAY + "/" + service
    }
}

const handleBrowserServiceCall = (service: string): string => {
    const STORE_POD_GATEWAY = process.env.EXTERNAL_STORE_POD_GATEWAY;
    if (STORE_POD_GATEWAY) {
        // with custom domain
        return STORE_POD_GATEWAY + "/" + service;
    } else {
        // with default domain
        return "/" + service
    }
}