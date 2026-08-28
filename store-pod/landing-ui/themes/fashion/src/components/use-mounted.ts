'use client'
import {useSyncExternalStore} from 'react';

const subscribe = () => () => {};

/** True after hydration. Cart-derived numbers live in the browser only, so they render after mount to keep SSR and client HTML identical. */
export function useMounted(): boolean {
    return useSyncExternalStore(subscribe, () => true, () => false);
}
