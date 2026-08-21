'use client'
import {useLocale} from 'next-intl';
import {type Dir, getDirection} from './direction';

/** Reading direction of the active locale, for the rare component that must branch on it (Swiper, Tabs). */
export function useDir(): Dir {
    return getDirection(useLocale());
}
