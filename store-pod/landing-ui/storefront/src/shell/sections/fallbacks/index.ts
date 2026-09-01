import type {ComponentType} from 'react';
import type {SectionKind} from '@store-front/types';
import type {SectionRenderProps} from '@store-front/theme';
import {sectionsFromChrome} from '@store-front/ui/sections/compose';
import {neutralChrome} from './neutral';
import {heroFallback} from './hero';
import {productsFallback} from './products';

/**
 * The shell's renderer for every kind and variant in the catalogue — the section composer run with
 * the neutral chrome, exactly the way a theme runs it with its own. This is why the builder can
 * promise that any layout renders on any theme, and why a theme can only re-voice a section, never
 * change what it means. `hero` and `products` are the composer's two override slots here as
 * everywhere: they are identity pieces even in the neutral voice.
 */
export const FALLBACK_SECTIONS = sectionsFromChrome(neutralChrome, {
    hero: heroFallback,
    products: productsFallback,
}) as Record<SectionKind, Record<string, ComponentType<SectionRenderProps>>>;
