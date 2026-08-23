import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {getTheme} from '@/shell/theme/get-theme';
import {loadPageContext} from '@/shell/loaders/page-context';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.CHECKOUT');
    return {title: t('TITLE'), robots: {index: false}};
}

export default async function CheckoutPage() {
    const [theme, ctx] = await Promise.all([getTheme(), loadPageContext()]);
    return <theme.pages.Checkout ctx={ctx} data={{requireLogin: ctx.store.requireLoginForOrderPlacement ?? true}}/>;
}
