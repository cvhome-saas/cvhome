import {getTranslations} from 'next-intl/server';
import type {CustomerData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {CustomerTabs} from '../client';

export async function Customer({ctx}: PageProps<CustomerData>) {
    const t = await getTranslations('PAGE.CUSTOMER');
    return (
        <PageShell width="content" className="flex flex-col gap-6 py-8">
            <h1 className="q font-display text-4xl font-bold uppercase leading-[0.9] tracking-tight [overflow-wrap:anywhere] sm:text-5xl">{t('TITLE')}</h1>
            <CustomerTabs storeContext={ctx.storeContext}/>
        </PageShell>
    );
}
