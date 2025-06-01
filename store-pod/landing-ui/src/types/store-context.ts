import {DEFAULT_STORE_POD_GATEWAY} from "@/types/constant";

export interface StoreContext {
    store: string
    locale: string
    externalGateway: string | undefined
    internalGateway: string | undefined
}

export const storeBaseServiceUrl = (service: string, storeContext: StoreContext): string => {
    return typeof window === 'undefined' ? handleInternalServiceCall(service, storeContext) : handleBrowserServiceCall(service, storeContext)
}

const handleInternalServiceCall = (service: string, storeContext: StoreContext): string => {
    if (storeContext.internalGateway) {
        return storeContext.internalGateway + "/" + service;
    } else {
        return DEFAULT_STORE_POD_GATEWAY + "/" + service
    }
}

const handleBrowserServiceCall = (service: string, storeContext: StoreContext): string => {
    if (storeContext.externalGateway) {
        return storeContext.externalGateway + "/" + service;
    } else {
        return "/" + service
    }
}