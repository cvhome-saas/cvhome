import type {ThemeLayoutConfig} from '@store-front/theme';

/**
 * Two-row header from lg up: the thin utility rail (language · account · basket ticket) sits above the
 * masthead, so `heightPx.lg` matches `--header-h-lg` in tokens.css. Products are shot portrait (4/5)
 * because a whole-home retailer sells objects that stand in a room.
 */
export const layoutConfig: ThemeLayoutConfig = {
    header: {sticky: true, heightPx: {base: 56, lg: 104}},
    cart: 'drawer',
    mobileNav: 'drawer',
    productGrid: {base: 2, sm: 2, lg: 3, xl: 4},
    productImageAspect: '4/5',
    container: 'content',
    search: 'header',
};
