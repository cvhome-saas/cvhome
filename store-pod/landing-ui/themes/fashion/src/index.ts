import {defineTheme, mix} from '@store-front/theme';
import {fonts} from './fonts';
import {layoutConfig} from './config';
import {Root} from './layout/Root';
import {Home} from './pages/Home';
import {Category} from './pages/Category';
import {Product} from './pages/Product';
import {Content} from './pages/Content';
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

/** fashion — The Wheatpaste Wall. Behaviour from @store-front/hooks; look and structure are this theme's. */
export default defineTheme({
    id: 'fashion',
    name: 'Fashion',
    version: '1.0.0',
    description: 'The wheatpaste wall for streetwear and drops: every product a pasted poster on a rendered wall, the merchant primary as day-glo paper, state as rubber stamps.',
    fonts,
    tokens: {
        // Paper and ink come from the preset's background/foreground; the preset's PRIMARY is the day-glo
        // paper and owns every live state (ring included). Accent is demoted to a faint paper tint so hover
        // surfaces and skeletons never introduce a second hue; secondary is ink for stamps and chips.
        mapMerchantColors: (schema) => ({
            ring: schema.primary,
            accent: mix(schema.background, schema.foreground, 0.08),
            accentForeground: schema.foreground,
            secondary: schema.foreground,
        }),
    },
    layout: {config: layoutConfig, Root},
    pages: {Home, Category, Product, Content, Checkout, CheckoutResult, Customer, Order},
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
