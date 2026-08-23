import type {RegisteredThemeId} from './registry';

/**
 * Every value of the `Theme` enum (`libs/types/src/store.ts`), lowercased, mapped to the theme that serves
 * it today. A merchant's stored enum value never breaks the storefront: unknown → FALLBACK_THEME_ID.
 *
 * When a replacement theme from `themes/README.md` ships, point its enum values here
 * (e.g. `jewelery: 'atelier'`). `scripts/new-theme.mjs` adds a same-name entry for new themes.
 */
export const FALLBACK_THEME_ID: RegisteredThemeId = (process.env.STOREFRONT_FALLBACK_THEME as RegisteredThemeId | undefined) ?? 'starter';

export const LEGACY_THEME_MAP: Readonly<Record<string, RegisteredThemeId>> = {
    basis: 'starter',
    modern: 'starter',
    jewelery: 'starter',
    beauty: 'beauty',
    default: 'starter',
    fashion: 'fashion',
    furniture: 'starter',
    sports: 'starter',
    electronics: 'starter',
    food: 'starter',
    glasses: 'starter',
    cosmetics: 'starter',
    watches: 'starter',
    baby: 'starter',
    tools: 'starter',
    'basic': 'basic',
    // @legacy-themes:end
};
