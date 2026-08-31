import {defineTheme} from '@store-front/theme';
import './tokens.css';
import {fonts} from './fonts';
import {DEFAULT_COLORS} from './colors';
import {layoutConfig} from './config';
import {Root} from './layout/Root';
import {Home} from './pages/Home';
import {Category} from './pages/Category';
import {Product} from './pages/Product';
import {Content} from './pages/Content';
import {BlogIndex} from './pages/BlogIndex';
import {BlogPost} from './pages/BlogPost';
import {Faq} from './pages/Faq';
import {Policy} from './pages/Policy';
import {Checkout} from './pages/Checkout';
import {CheckoutResult} from './pages/CheckoutResult';
import {Customer} from './pages/Customer';
import {Order} from './pages/Order';
import {ErrorState} from './states/ErrorState';
import {NotFound} from './states/NotFound';
import {EmptyState} from './states/EmptyState';
import {Redirecting} from './states/Redirecting';
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
    id: 'glasses',
    name: 'Glasses',
    version: '0.1.0',
    description: 'Plain reference theme — copy source for new themes.',
    fonts,
    tokens: {defaultColors: DEFAULT_COLORS},
    layout: {config: layoutConfig, Root},
    pages: {Home, Category, Product, Content, BlogIndex, BlogPost, Faq, Policy, Checkout, CheckoutResult, Customer, Order},
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
