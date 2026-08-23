import type {ThemeDefinition} from './contract';

const PAGES = ['Home', 'Category', 'Product', 'Content', 'BlogIndex', 'BlogPost', 'Faq', 'Policy', 'Checkout', 'CheckoutResult', 'Customer', 'Order'] as const;
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
    if (missing.length) {
        throw new Error(`Theme "${def.id}" does not satisfy the ThemeDefinition contract. Missing/invalid: ${missing.join(', ')}`);
    }
    return def;
}
