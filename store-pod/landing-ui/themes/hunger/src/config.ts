import type {ThemeLayoutConfig} from '@store-front/theme';

/**
 * A menu is one column of lines, not a gallery of cards: every price on the sheet falls in the same
 * tabular column at every width, which is the whole point of a printed menu and what a second column
 * would break. `productImageAspect` is the small printed thumb beside each dish.
 */
export const layoutConfig: ThemeLayoutConfig = {
    header: {sticky: true, heightPx: {base: 56, lg: 68}},
    cart: 'drawer',
    mobileNav: 'drawer',
    productGrid: {base: 1, sm: 1, lg: 1, xl: 1},
    productImageAspect: '4/3',
    container: 'content',
    search: 'header',
};
