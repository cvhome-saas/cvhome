import type {ThemeDefinition} from '@store-front/theme';

/**
 * Static map of dynamic imports: each theme becomes its own server chunk and only the resolved theme's
 * client components reach the browser. Managed by `scripts/new-theme.mjs` — keep one line per theme
 * between the markers.
 */
export const themeLoaders = {
    // @themes:start
    starter: () => import('@store-front/theme-starter'),
    'beauty': () => import('@store-front/theme-beauty'),
    'fashion': () => import('@store-front/theme-fashion'),
    // @themes:end
} satisfies Record<string, () => Promise<{ default: ThemeDefinition }>>;

export type RegisteredThemeId = keyof typeof themeLoaders;

export const isRegisteredTheme = (id: string): id is RegisteredThemeId => Object.hasOwn(themeLoaders, id);
