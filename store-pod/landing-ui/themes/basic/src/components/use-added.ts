'use client'
import {useEffect, useState} from 'react';
import type {CartStatus} from '@store-front/hooks/use-cart';

/**
 * The price flash. `arm()` before calling addToCart; when the cart's total quantity grows the flash switches on
 * for `ms` and switches off; a request that settles without a new quantity (failed) never flashes.
 */
export function useAddedFlash(count: number, status: CartStatus, ms = 1600) {
    const [flashKey, setFlashKey] = useState(0);
    const [pending, setPending] = useState<number | null>(null);
    const [sawBusy, setSawBusy] = useState(false);
    if (pending !== null && count !== pending) {
        setPending(null); setSawBusy(false);
        if (count > pending) setFlashKey(k => k + 1);
    } else if (pending !== null && status === 'busy' && !sawBusy) {
        setSawBusy(true);
    } else if (pending !== null && sawBusy && status === 'idle') {
        setPending(null); setSawBusy(false);
    }
    useEffect(() => {
        if (!flashKey) return;
        const id = setTimeout(() => setFlashKey(0), ms);
        return () => clearTimeout(id);
    }, [flashKey, ms]);
    return {on: flashKey > 0, arm: () => setPending(count)};
}
