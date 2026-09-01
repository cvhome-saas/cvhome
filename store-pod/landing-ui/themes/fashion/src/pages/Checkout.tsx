import {getTranslations} from 'next-intl/server';
import type {CheckoutData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {CheckoutForm, OrderSummary} from '../client';

/** Checkout: the form on a big sheet, the bag on a sheet beside it. */
export async function Checkout({ctx, data}: PageProps<CheckoutData>) {
    const t = await getTranslations('PAGE.CHECKOUT');
    const tc = await getTranslations('COMMON');
    return (
        <PageShell width="content" className="flex flex-col gap-6 py-6 lg:py-8">
            <Breadcrumbs items={[{id: 'home', name: tc('HOME'), href: '/'}, {id: 'checkout', name: t('TITLE'), href: '/checkout'}]}/>
            <h1 className="strip h-12 w-fit text-2xl [--tilt:-0.8deg] sm:text-3xl">{t('TITLE')}</h1>
            <div className="grid gap-6 lg:grid-cols-3 lg:items-start lg:gap-8">
                <div className="sheet p-5 [--tilt:0.25deg] sm:p-7 lg:col-span-2"><CheckoutForm storeContext={ctx.storeContext} requireLogin={data.requireLogin}/></div>
                <OrderSummary storeContext={ctx.storeContext}/>
            </div>
        </PageShell>
    );
}
