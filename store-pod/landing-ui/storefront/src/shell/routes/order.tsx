import type {Metadata} from 'next';
import {notFound} from 'next/navigation';
import {getTranslations} from 'next-intl/server';
import {Secured} from '@/shell/auth/secured';
import {themed, type ThemeSource} from './theme-source';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.CUSTOMER');
    return {title: t('ORDER_DETAILS'), robots: {index: false}};
}

export function orderPage(theme: ThemeSource) {
    return async function OrderPage({params}: { params: Promise<{ id: string }> }) {
        const {id} = await params;
        const orderId = Number(id);
        if (!Number.isInteger(orderId) || orderId <= 0) notFound();
        const {theme: resolved, ctx} = await themed(theme);
        return (
            <Secured storeContext={ctx.storeContext}>
                <resolved.pages.Order ctx={ctx} data={{orderId}}/>
            </Secured>
        );
    };
}
