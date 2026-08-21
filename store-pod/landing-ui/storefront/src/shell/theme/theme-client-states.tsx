'use client'
import {createContext, type ReactNode, useContext} from 'react';
import type {ThemeStates} from '@store-front/theme';

/**
 * Client-side access to the theme's state components. `error.tsx` is a client boundary and cannot call
 * `getTheme()`, so the root layout hands the (client) state components down through this context.
 */
export type ThemeClientStateSet = Pick<ThemeStates, 'ErrorState' | 'EmptyState' | 'Redirecting'>;

const Ctx = createContext<ThemeClientStateSet | null>(null);

export function ThemeClientStates({states, children}: { states: ThemeClientStateSet; children: ReactNode }) {
    return <Ctx.Provider value={states}>{children}</Ctx.Provider>;
}

export function useThemeStates(): ThemeClientStateSet {
    const v = useContext(Ctx);
    if (!v) throw new Error('useThemeStates must be used inside the storefront root layout');
    return v;
}
