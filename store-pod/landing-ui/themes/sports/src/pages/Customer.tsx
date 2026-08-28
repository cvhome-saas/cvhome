import {getTranslations} from 'next-intl/server';
import type {CustomerData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {CustomerTabs} from '../sections/CustomerTabs';

export async function Customer({ctx}: PageProps<CustomerData>) {
    const t = await getTranslations('PAGE.CUSTOMER');
    return (
        <PageShell width="content" className="flex flex-col gap-6 py-8">
            <h1 className="text-3xl font-semibold tracking-tight">{t('TITLE')}</h1>
            <CustomerTabs storeContext={ctx.storeContext}/>
        </PageShell>
    );
}
