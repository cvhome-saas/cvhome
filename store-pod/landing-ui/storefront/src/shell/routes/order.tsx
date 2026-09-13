import type {Metadata} from 'next';
import {notFound} from 'next/navigation';
import {getTranslations} from 'next-intl/server';
import type {ThemeDefinition} from '@store-front/theme';
import {Secured} from '@/shell/auth/secured';
import {loadPageContext} from '@/shell/loaders/page-context';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.CUSTOMER');
    return {title: t('ORDER_DETAILS'), robots: {index: false}};
}

export function orderPage(theme: ThemeDefinition) {
    return async function OrderPage({params}: { params: Promise<{ id: string }> }) {
        const {id} = await params;
        const orderId = Number(id);
        if (!Number.isInteger(orderId) || orderId <= 0) notFound();
        const ctx = await loadPageContext(theme);
        return (
            <Secured storeContext={ctx.storeContext}>
                <theme.pages.Order ctx={ctx} data={{orderId}}/>
            </Secured>
        );
    };
}
