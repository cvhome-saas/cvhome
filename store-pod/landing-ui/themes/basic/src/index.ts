import {defineTheme, mix} from '@store-front/theme';
import {fonts} from './fonts';
import {DEFAULT_COLORS} from './colors';
import {layoutConfig} from './config';
import {Root} from './layout/Root';
import {Home} from './pages/Home';
import {Category} from './pages/Category';
import {Search} from './pages/Search';
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
import {SearchSkeleton} from './states/skeletons/SearchSkeleton';
import {ProductSkeleton} from './states/skeletons/ProductSkeleton';
import {ContentSkeleton} from './states/skeletons/ContentSkeleton';
import {CheckoutSkeleton} from './states/skeletons/CheckoutSkeleton';
import {CustomerSkeleton} from './states/skeletons/CustomerSkeleton';
import {OrderSkeleton} from './states/skeletons/OrderSkeleton';

/** basic — The Catalogue Page. Behaviour from @store-front/hooks; look and structure are this theme's. */
export default defineTheme({
    id: 'basic',
    name: 'Basic',
    version: '1.1.0',
    description: 'The catalogue page for any store: one continuous ruled catalogue of entries with big condensed prices, a thumb-index of categories, and the merchant primary as flat fields on the cover, the active tab and the one action per view.',
    fonts,
    tokens: {
        defaultColors: DEFAULT_COLORS,
        // Paper and ink are the preset's background/foreground. The preset's PRIMARY owns the cover title
        // block, the active index tab, every primary action and the focus ring. Accent and secondary are
        // demoted to faint tonal mixes so hover surfaces, chips and quiet badges never introduce a second
        // hue; sale keeps the preset's error colour for the price flash.
        mapMerchantColors: (schema) => ({
            ring: schema.primary,
            accent: mix(schema.background, schema.foreground, 0.06),
            accentForeground: schema.foreground,
            secondary: mix(schema.background, schema.foreground, 0.1),
            secondaryForeground: schema.foreground,
        }),
    },
    layout: {config: layoutConfig, Root},
    pages: {Home, Category, Search, Product, Content, BlogIndex, BlogPost, Faq, Policy, Checkout, CheckoutResult, Customer, Order},
    states: {
        PageSkeleton: {
            home: HomeSkeleton,
            category: CategorySkeleton,
            search: SearchSkeleton,
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
