import {getTranslations} from 'next-intl/server';
import type {CheckoutData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {CheckoutForm, OrderSummary} from '../client';

export async function Checkout({ctx, data}: PageProps<CheckoutData>) {
    const t = await getTranslations('PAGE.CHECKOUT');
    const tc = await getTranslations('COMMON');
    return (
        <PageShell width="content" className="flex flex-col gap-6 py-6 lg:py-8">
            <Breadcrumbs items={[{id: 'home', name: tc('HOME'), href: '/'}, {id: 'checkout', name: t('TITLE'), href: '/checkout'}]}/>
            <h1 className="q font-display text-4xl font-bold uppercase leading-[0.9] tracking-tight [overflow-wrap:anywhere] sm:text-5xl">{t('TITLE')}</h1>
            <div className="grid gap-6 lg:grid-cols-3 lg:items-start lg:gap-8">
                <div className="plate p-5 lg:col-span-2"><CheckoutForm storeContext={ctx.storeContext} requireLogin={data.requireLogin}/></div>
                <OrderSummary storeContext={ctx.storeContext}/>
            </div>
        </PageShell>
    );
}
