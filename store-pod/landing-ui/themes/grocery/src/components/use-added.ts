'use client'
import {useEffect, useState} from 'react';
import type {CartStatus} from '@store-front/hooks/use-cart';

/**
 * The ADDED stamp. `arm()` before calling addToCart; when the cart's total quantity grows the stamp
 * switches on for `ms` and off; a request that settles without a new quantity (failed) never stamps.
 */
export function useAddedStamp(count: number, status: CartStatus, ms = 1400) {
    const [stampKey, setStampKey] = useState(0);
    const [pending, setPending] = useState<number | null>(null);
    const [sawBusy, setSawBusy] = useState(false);
    if (pending !== null && count !== pending) {
        setPending(null); setSawBusy(false);
        if (count > pending) setStampKey(k => k + 1);
    } else if (pending !== null && status === 'busy' && !sawBusy) {
        setSawBusy(true);
    } else if (pending !== null && sawBusy && status === 'idle') {
        setPending(null); setSawBusy(false);
    }
    useEffect(() => {
        if (!stampKey) return;
        const id = setTimeout(() => setStampKey(0), ms);
        return () => clearTimeout(id);
    }, [stampKey, ms]);
    return {on: stampKey > 0, arm: () => setPending(count)};
}
