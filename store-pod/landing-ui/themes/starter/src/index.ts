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

/**
 * starter — the reference implementation of the theme contract.
 * Intentionally undesigned: neutral type, neutral radius, no decoration. Everything a theme must do
 * (every page, every state, RTL, mobile nav, cart drawer, variants, sort/pagination, search capability
 * branch, sale/out-of-stock badges, skeletons, error/empty/not-found) is here to copy and then redesign.
 */
export default defineTheme({
    id: 'starter',
    name: 'Starter',
    version: '0.1.0',
    description: 'Plain reference theme — copy source for new themes.',
    tokens: {defaultColors: DEFAULT_COLORS},
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
