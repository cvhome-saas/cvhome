import type {ThemeLayoutConfig} from '@store-front/theme';

export const layoutConfig: ThemeLayoutConfig = {
    header: {sticky: true, heightPx: {base: 52, lg: 60}},
    cart: 'drawer',
    mobileNav: 'fullscreen',
    productGrid: {base: 2, sm: 2, lg: 3, xl: 4},
    productImageAspect: '1/1',
    container: 'wide',
    search: 'header',
};
