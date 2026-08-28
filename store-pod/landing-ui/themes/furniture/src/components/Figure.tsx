'use client'
import {useState} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/**
 * Every figure on the site lives in the same tabular slot — counts, prices, quantities, floor numbers —
 * and rolls in place the moment it changes. This is the theme's one authored motion; nothing else moves.
 * Derived state (no effect) so the roll replays exactly once per real change, never on mount.
 */
export function Figure({value, className, label}: { value: string | number; className?: string; label?: string }) {
    const [seen, setSeen] = useState<string | number>(value);
    const [animate, setAnimate] = useState(false);
    if (seen !== value) {
        setSeen(value);
        setAnimate(true);
    }
    return (
        <span className={cn('figure roll-slot', className)} aria-label={label}>
            <span key={String(value)} className={animate ? 'rolling' : undefined}>{value}</span>
        </span>
    );
}
