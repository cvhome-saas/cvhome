import {DEFAULT_SPG} from "./constant";

export interface StoreContext {
    store: string
    locale: string
    externalGateway: string | undefined
    internalGateway: string | undefined
}

export const storeBaseServiceUrl = (service: string, storeContext: StoreContext): string => {
    const isBrowser = typeof globalThis !== 'undefined' && typeof (globalThis as any).window !== 'undefined';
    return isBrowser
        ? handleBrowserServiceCall(service, storeContext)
        : handleInternalServiceCall(service, storeContext);
}

const handleInternalServiceCall = (service: string, storeContext: StoreContext): string => {
    if (storeContext.internalGateway) {
        return storeContext.internalGateway + "/" + service;
    } else {
        return DEFAULT_SPG + "/" + service
    }
}

const handleBrowserServiceCall = (service: string, storeContext: StoreContext): string => {
    if (storeContext.externalGateway) {
        return storeContext.externalGateway + "/" + service;
    } else {
        return "/" + service
    }
}
