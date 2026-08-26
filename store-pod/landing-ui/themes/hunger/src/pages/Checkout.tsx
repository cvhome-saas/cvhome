import {getTranslations} from 'next-intl/server';
import type {CheckoutData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {CheckoutForm} from '../sections/CheckoutForm';
import {OrderSummary} from '../sections/OrderSummary';

/** The counter: where the order gets written down and paid for. */
export async function Checkout({ctx, data}: PageProps<CheckoutData>) {
    const t = await getTranslations('PAGE.CHECKOUT');
    const tc = await getTranslations('COMMON');
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-6 py-6">
            <Breadcrumbs items={[{id: 'home', name: tc('HOME'), href: '/'}, {id: 'checkout', name: t('TITLE'), href: '/checkout'}]}/>
            <h1 className="press plate px-3 py-2 text-3xl leading-none sm:text-4xl">{t('TITLE')}</h1>
            <div className="grid gap-8 lg:grid-cols-3 lg:items-start lg:gap-12">
                <div className="lg:col-span-2"><CheckoutForm storeContext={ctx.storeContext} requireLogin={data.requireLogin}/></div>
                <OrderSummary storeContext={ctx.storeContext}/>
            </div>
        </PageShell>
    );
}
