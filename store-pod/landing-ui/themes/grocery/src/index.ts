import {defineTheme} from '@store-front/theme';
import './tokens.css';
import {fonts} from './fonts';
import {DEFAULT_COLORS} from './colors';
import {layoutConfig} from './config';
import {Root} from './layout/Root';
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

/** grocery — Cash & Carry. Behaviour from @store-front/hooks; look and structure are this theme's. */
export default defineTheme({
    id: 'grocery',
    name: 'Grocery',
    version: '1.0.0',
    description: 'Cash & Carry for food and consumables: the shop as a working warehouse floor — products in molded crates with stepper quick-add, prices as printed signage on the merchant primary, categories as an aisle-board strip, state as stickers, and a basket drawer that shows the load.',
    fonts,
    tokens: {
        defaultColors: DEFAULT_COLORS,
        // The preset rides as given: PRIMARY is the price-board colour (hero board, active aisle, every
        // primary action); only the focus ring is pinned to it so the shelf light always matches the boards.
        mapMerchantColors: (schema) => ({ring: schema.primary}),
    },
    layout: {config: layoutConfig, Root},
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
