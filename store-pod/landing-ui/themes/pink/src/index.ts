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
 * pink — "Tokyo Girls Issue". The shop as this month's Japanese girls' fashion magazine: a flooded cover,
 * numbered sections, ruled plates of die-cuts with their prices on notched flags, and the pen annotating
 * whatever is on sale or nearly gone. See `themes/pink/DESIGN.md`.
 */
export default defineTheme({
    id: 'pink',
    name: 'Pink',
    version: '1.0.0',
    description: 'Tokyo Girls Issue — a Japanese girls\' magazine for a mixed girly-lifestyle store.',
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
