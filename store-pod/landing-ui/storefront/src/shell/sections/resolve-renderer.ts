import type {ComponentType} from 'react';
import type {LayoutSectionData} from '@store-front/types';
import type {SectionRenderProps, ThemeDefinition} from '@store-front/theme';
import {FALLBACK_SECTIONS} from './fallbacks';

/**
 * Which component draws a section: the theme's exact `kind.variant`, else the theme's default for the kind,
 * else the shell fallback for `kind.variant`, else the fallback kind's first variant. `undefined` only for a
 * kind nothing knows — the section is skipped (with a dev warning) rather than crashing the page.
 */
export function resolveRenderer(theme: ThemeDefinition, section: LayoutSectionData):
    ComponentType<SectionRenderProps> | undefined {
    const variant = section.variant ?? undefined;
    const themed = theme.sections?.[section.kind];
    if (themed) {
        if (variant && themed[variant]) return themed[variant];
        const first = Object.values(themed)[0];
        if (first) return first;
    }
    const fallback = FALLBACK_SECTIONS[section.kind];
    if (fallback) {
        if (variant && fallback[variant]) return fallback[variant];
        const first = Object.values(fallback)[0];
        if (first) return first;
    }
    if (process.env.NODE_ENV !== 'production') {
        console.warn(`No renderer for section kind "${section.kind}" — section ${section.id} skipped.`);
    }
    return undefined;
}
