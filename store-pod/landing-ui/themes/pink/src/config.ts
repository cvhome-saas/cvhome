import type {ThemeLayoutConfig} from '@store-front/theme';

/** A spread runs dense: five entries across on the widest plate, portrait die-cuts, the cart as a drawer. */
export const layoutConfig: ThemeLayoutConfig = {
    header: {sticky: true, heightPx: {base: 56, lg: 104}},
    cart: 'drawer',
    mobileNav: 'drawer',
    productGrid: {base: 2, sm: 3, lg: 4, xl: 5},
    productImageAspect: '4/5',
    container: 'content',
    search: 'header',
};
