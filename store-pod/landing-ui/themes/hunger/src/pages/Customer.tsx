import {getTranslations} from 'next-intl/server';
import type {CustomerData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {CustomerTabs} from '../client';

export async function Customer({ctx}: PageProps<CustomerData>) {
    const t = await getTranslations('PAGE.CUSTOMER');
    return (
        <PageShell width="content" className="flex flex-col gap-6 py-6">
            <h1 className="press plate px-3 py-2 text-3xl leading-none sm:text-4xl">{t('TITLE')}</h1>
            <CustomerTabs storeContext={ctx.storeContext}/>
        </PageShell>
    );
}
