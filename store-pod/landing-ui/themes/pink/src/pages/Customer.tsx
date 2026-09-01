import {getTranslations} from 'next-intl/server';
import type {CustomerData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {CustomerTabs} from '../client';

export async function Customer({ctx}: PageProps<CustomerData>) {
    const t = await getTranslations('PAGE.CUSTOMER');
    return (
        <PageShell width="content" className="flex flex-col gap-6 py-8 lg:py-10">
            <h1 className="hair display border-b-2 pb-4 text-3xl sm:text-4xl">{t('TITLE')}</h1>
            <CustomerTabs storeContext={ctx.storeContext}/>
        </PageShell>
    );
}
