import type {ThemeLayoutConfig} from '@store-front/theme';

export const layoutConfig: ThemeLayoutConfig = {
    header: {sticky: true, heightPx: {base: 56, lg: 64}},
    cart: 'drawer',
    mobileNav: 'drawer',
    productGrid: {base: 2, sm: 2, lg: 3, xl: 4},
    productImageAspect: '1/1',
    container: 'content',
    search: 'header',
};
