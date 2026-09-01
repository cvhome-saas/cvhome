import {getTranslations} from 'next-intl/server';
import type {CustomerData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {CustomerTabs} from '../client';

export async function Customer({ctx}: PageProps<CustomerData>) {
    const t = await getTranslations('PAGE.CUSTOMER');
    return (
        <PageShell width="content" className="flex flex-col gap-6 py-6 lg:py-8">
            <h1 className="strip h-12 w-fit text-2xl [--tilt:-0.8deg] sm:text-3xl">{t('TITLE')}</h1>
            <CustomerTabs storeContext={ctx.storeContext}/>
        </PageShell>
    );
}
