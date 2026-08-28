'use client'
import {useSyncExternalStore} from 'react';

const subscribe = () => () => {};

/** True after hydration. Cart-derived state lives in the browser only, so it renders after mount to keep SSR and client HTML identical. */
export function useMounted(): boolean {
    return useSyncExternalStore(subscribe, () => true, () => false);
}
