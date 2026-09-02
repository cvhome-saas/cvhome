import type {ThemeDefinition} from './contract';

// `Search`, `Login` and `Register` are deliberately absent: they are optional in ThemePages, and the shell renders
// a fallback for any theme that has not implemented them. Adding one here would break the build for every theme
// that has not.
// `Home` is gone from ThemePages entirely: the shell composes the home page from the store's layout document
// and the theme's `sections` registry (with shell fallbacks for anything unregistered).
const PAGES = ['Category', 'Product', 'Content', 'BlogIndex', 'BlogPost', 'Faq', 'Policy', 'Checkout', 'CheckoutResult', 'Customer', 'Order'] as const;
const SKELETONS = ['home', 'category', 'product', 'content', 'checkout', 'customer', 'order'] as const;
const STATES = ['ErrorState', 'NotFound', 'EmptyState', 'Redirecting'] as const;
const ID = /^[a-z][a-z0-9-]*$/;

/**
 * Identity function with a contract check. A theme that forgets a page or a state fails the moment its
 * module is imported — i.e. at `next build` — instead of rendering a blank region in production.
 */
export function defineTheme(def: ThemeDefinition): ThemeDefinition {
    const missing: string[] = [];
    if (!ID.test(def.id)) missing.push(`id "${def.id}" must be kebab-case (it is the folder name and the data-theme value)`);
    if (!def.name) missing.push('name');
    if (!def.version) missing.push('version');
    if (!def.fonts || typeof def.fonts.variables !== 'string') missing.push('fonts.variables');
    if (!def.tokens) missing.push('tokens');
    else if (!def.tokens.defaultColors?.background || !def.tokens.defaultColors?.primary) missing.push('tokens.defaultColors (generated src/colors.ts — see libs/types THEME_DEFAULTS)');
    if (!def.layout?.config) missing.push('layout.config');
    if (typeof def.layout?.Root !== 'function') missing.push('layout.Root');
    for (const p of PAGES) if (typeof def.pages?.[p] !== 'function') missing.push(`pages.${p}`);
    for (const s of SKELETONS) if (typeof def.states?.PageSkeleton?.[s] !== 'function') missing.push(`states.PageSkeleton.${s}`);
    for (const s of STATES) if (typeof def.states?.[s] !== 'function') missing.push(`states.${s}`);
    if (def.sections) {
        for (const [kind, variants] of Object.entries(def.sections)) {
            for (const [variant, component] of Object.entries(variants ?? {})) {
                if (typeof component !== 'function') missing.push(`sections.${kind}.${variant}`);
            }
        }
    }
    if (missing.length) {
        throw new Error(`Theme "${def.id}" does not satisfy the ThemeDefinition contract. Missing/invalid: ${missing.join(', ')}`);
    }
    return def;
}
