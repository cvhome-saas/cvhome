import {createNavigation} from 'next-intl/navigation';
import {routing} from './routing';

// Locale-aware wrappers around Next.js navigation. Themes import `Link`, `useRouter`, `usePathname` from
// here — never from `next/link` / `next/navigation` — so every href keeps the active locale.
export const {Link, redirect, usePathname, useRouter, getPathname} = createNavigation(routing);
