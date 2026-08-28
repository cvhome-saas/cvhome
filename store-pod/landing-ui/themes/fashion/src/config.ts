import type {ThemeLayoutConfig} from '@store-front/theme';

export const layoutConfig: ThemeLayoutConfig = {
    header: {sticky: true, heightPx: {base: 56, lg: 64}},
    cart: 'drawer',
    mobileNav: 'drawer',
    productGrid: {base: 2, sm: 3, lg: 4, xl: 6},
    productImageAspect: '4/5',
    container: 'wide',
    search: 'header',
};
