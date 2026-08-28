import {getTranslations} from 'next-intl/server';
import type {CheckoutData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {PageHead} from '../components/PageHead';
import {CheckoutForm} from '../sections/CheckoutForm';
import {OrderSummary} from '../sections/OrderSummary';

/** The delivery desk: where the goods go, how they are paid for, and the docket alongside. */
export async function Checkout({ctx, data}: PageProps<CheckoutData>) {
    const t = await getTranslations('PAGE.CHECKOUT');
    const tc = await getTranslations('COMMON');
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
            <Breadcrumbs items={[{id: 'home', name: tc('HOME'), href: '/'}, {id: 'checkout', name: t('TITLE'), href: '/checkout'}]}/>
            <PageHead title={t('TITLE')}/>
            <div className="grid gap-10 lg:grid-cols-3 lg:items-start lg:gap-14">
                <div className="lg:col-span-2"><CheckoutForm storeContext={ctx.storeContext} requireLogin={data.requireLogin}/></div>
                <OrderSummary storeContext={ctx.storeContext}/>
            </div>
        </PageShell>
    );
}
