import {defineTheme} from '@store-front/theme';
import {DEFAULT_COLORS} from './colors';
import {layoutConfig} from './config';
import {Root} from './layout/Root';
import {layoutSections} from './sections/LayoutSections';
import {Category} from './pages/Category';
import {Product} from './pages/Product';
import {Content} from './pages/Content';
import {BlogIndex} from './pages/BlogIndex';
import {BlogPost} from './pages/BlogPost';
import {Faq} from './pages/Faq';
import {Policy} from './pages/Policy';
import {Checkout} from './pages/Checkout';
import {CheckoutResult, ErrorState, EmptyState, Redirecting, ThemeFrame} from './client';
import {Customer} from './pages/Customer';
import {Order} from './pages/Order';
import {NotFound} from './states/NotFound';
import {HomeSkeleton} from './states/skeletons/HomeSkeleton';
import {CategorySkeleton} from './states/skeletons/CategorySkeleton';
import {ProductSkeleton} from './states/skeletons/ProductSkeleton';
import {ContentSkeleton} from './states/skeletons/ContentSkeleton';
import {CheckoutSkeleton} from './states/skeletons/CheckoutSkeleton';
import {CustomerSkeleton} from './states/skeletons/CustomerSkeleton';
import {OrderSkeleton} from './states/skeletons/OrderSkeleton';

/** beauty — Industrial Quote Grammar. Behaviour from @store-front/hooks; look and structure are this theme's. */
export default defineTheme({
    id: 'beauty',
    name: 'Beauty',
    version: '1.1.0',
    description: 'Industrial quote grammar for a beauty + fashion boutique: ink plates, hazard stripes, the merchant primary as the zip-tie tag; labels stay on.',
    tokens: {
        defaultColors: DEFAULT_COLORS,
        // Monochrome world: the preset's background/foreground stay; the preset's PRIMARY is the only accent
        // (the zip-tie tag). Secondary/accent are demoted to ink so nothing competes with the tag.
        mapMerchantColors: (schema) => ({
            secondary: schema.foreground,
            accent: schema.foreground,
            ring: schema.primary,
            border: schema.foreground,
            input: schema.foreground,
        }),
    },
    layout: {config: layoutConfig, Root, Frame: ThemeFrame},
    sections: layoutSections,
    pages: {Category, Product, Content, BlogIndex, BlogPost, Faq, Policy, Checkout, CheckoutResult, Customer, Order},
    states: {
        PageSkeleton: {
            home: HomeSkeleton,
            category: CategorySkeleton,
            product: ProductSkeleton,
            content: ContentSkeleton,
            checkout: CheckoutSkeleton,
            customer: CustomerSkeleton,
            order: OrderSkeleton,
        },
        ErrorState,
        NotFound,
        EmptyState,
        Redirecting,
    },
});
