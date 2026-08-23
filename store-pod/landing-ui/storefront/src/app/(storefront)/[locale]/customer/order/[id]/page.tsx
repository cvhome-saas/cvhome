import type {Metadata} from 'next';
import {notFound} from 'next/navigation';
import {getTranslations} from 'next-intl/server';
import {getTheme} from '@/shell/theme/get-theme';
import {loadPageContext} from '@/shell/loaders/page-context';
import {Secured} from '@/shell/auth/secured';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.CUSTOMER');
    return {title: t('ORDER_DETAILS'), robots: {index: false}};
}

export default async function OrderPage({params}: { params: Promise<{ id: string }> }) {
    const {id} = await params;
    const orderId = Number(id);
    if (!Number.isInteger(orderId) || orderId <= 0) notFound();
    const [theme, ctx] = await Promise.all([getTheme(), loadPageContext()]);
    return (
        <Secured storeContext={ctx.storeContext}>
            <theme.pages.Order ctx={ctx} data={{orderId}}/>
        </Secured>
    );
}
