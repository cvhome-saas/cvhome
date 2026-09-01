import {getTranslations} from 'next-intl/server';
import type {OrderData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {OrderDetails} from '../client';

export async function Order({ctx, data}: PageProps<OrderData>) {
    const t = await getTranslations('PAGE.CUSTOMER');
    const tc = await getTranslations('COMMON');
    return (
        <PageShell width="content" className="flex flex-col gap-6 py-8 lg:py-10">
            <Breadcrumbs items={[
                {id: 'home', name: tc('HOME'), href: '/'},
                {id: 'account', name: t('TITLE'), href: '/customer'},
                {id: 'order', name: `${t('ORDER_ID')} #${data.orderId}`, href: `/customer/order/${data.orderId}`},
            ]}/>
            <OrderDetails storeContext={ctx.storeContext} orderId={data.orderId}/>
        </PageShell>
    );
}
