import 'server-only';
import type {PageContext, ThemeDefinition} from '@store-front/theme';
import {loadPageContext} from '@/shell/loaders/page-context';

/**
 * Where a route gets its theme. A per-theme route tree binds it statically (`async () => fashion`), which is
 * what keeps every other theme out of that tree's CSS and JS; `getTheme()` resolves it per request.
 */
export type ThemeSource = () => Promise<ThemeDefinition>;

/** The theme and the page context built from it, so a page can load its own data alongside both. */
export async function themed(theme: ThemeSource): Promise<{ theme: ThemeDefinition; ctx: PageContext }> {
    const resolved = await theme();
    return {theme: resolved, ctx: await loadPageContext(resolved)};
}
