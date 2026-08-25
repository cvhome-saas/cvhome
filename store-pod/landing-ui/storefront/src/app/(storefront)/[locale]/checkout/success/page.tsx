import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {getTheme} from '@/shell/theme/get-theme';
import {loadPageContext} from '@/shell/loaders/page-context';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.CHECKOUT');
    return {title: t('TITLE'), robots: {index: false}};
}

/**
 * Payment gateways redirect here with ?code=&orderId=. The theme component re-checks the real order
 * status through the API (useOrderStatus) rather than trusting which URL the browser landed on.
 */
export default async function CheckoutResultPage() {
    const [theme, ctx] = await Promise.all([getTheme(), loadPageContext()]);
    return <theme.pages.CheckoutResult ctx={ctx} data={{outcome: 'success', requireLogin: ctx.store.requireLoginForOrderPlacement ?? true}}/>;
}
