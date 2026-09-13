import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import type {ThemeDefinition} from '@store-front/theme';
import {loadPageContext} from '@/shell/loaders/page-context';

/** The checkout page and both of its result pages share it. */
export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.CHECKOUT');
    return {title: t('TITLE'), robots: {index: false}};
}

export function checkoutPage(theme: ThemeDefinition) {
    return async function CheckoutPage() {
        const ctx = await loadPageContext(theme);
        return <theme.pages.Checkout ctx={ctx} data={{requireLogin: ctx.store.requireLoginForOrderPlacement ?? true}}/>;
    };
}

/**
 * Payment gateways redirect to `/checkout/success` or `/checkout/cancel` with ?code=&orderId=. The theme component
 * re-checks the real order status through the API (useOrderStatus) rather than trusting which URL the browser
 * landed on.
 */
export function checkoutResultPage(theme: ThemeDefinition, outcome: 'success' | 'cancel') {
    return async function CheckoutResultPage() {
        const ctx = await loadPageContext(theme);
        return <theme.pages.CheckoutResult ctx={ctx} data={{outcome, requireLogin: ctx.store.requireLoginForOrderPlacement ?? true}}/>;
    };
}
