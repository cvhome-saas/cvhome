import type {ThemeLayoutConfig} from '@store-front/theme';

export const layoutConfig: ThemeLayoutConfig = {
    header: {sticky: true, heightPx: {base: 64, lg: 72}},
    cart: 'drawer',
    mobileNav: 'drawer',
    productGrid: {base: 2, sm: 3, lg: 4, xl: 5},
    productImageAspect: '1/1',
    container: 'content',
    search: 'header',
};
