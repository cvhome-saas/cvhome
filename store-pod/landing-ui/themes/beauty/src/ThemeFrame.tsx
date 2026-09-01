'use client'
import {type ReactNode, useLayoutEffect} from 'react';
import {fonts} from './fonts';

const CLASSES = fonts.variables.split(' ').filter(Boolean);
// Runs while the HTML is parsed, before first paint: the next/font variable classes reach <html> without the
// font CSS ever entering the shared layout entry (it lives in this theme's chunk, with tokens.css).
const APPLY = `document.documentElement.classList.add(${CLASSES.map(c => JSON.stringify(c)).join(',')})`;

/**
 * The theme's client frame — the one component of the lazily loaded theme chunk the shell always renders.
 * tokens.css says `[data-theme="beauty"] { --font-body: var(--font-beauty-…) }`: a custom property resolves on the
 * element that declares it, so the font classes must sit on <html>, next to data-theme, or every portal
 * (drawers, selects, toasts render into <body>) would fall back to the browser font. The inline script does it
 * before paint; the effect repeats it for a client-side mount. <html> carries suppressHydrationWarning for it.
 */
export function ThemeFrame({children}: { children: ReactNode }) {
    useLayoutEffect(() => {
        document.documentElement.classList.add(...CLASSES);
    }, []);
    return (
        <>
            <script dangerouslySetInnerHTML={{__html: APPLY}}/>
            {children}
        </>
    );
}
