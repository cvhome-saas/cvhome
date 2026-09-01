import {getTranslations} from 'next-intl/server';
import type {CustomerData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {PageHead} from '../components/PageHead';
import {CustomerTabs} from '../client';

export async function Customer({ctx}: PageProps<CustomerData>) {
    const t = await getTranslations('PAGE.CUSTOMER');
    return (
        <PageShell width="content" className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
            <PageHead title={t('TITLE')}/>
            <CustomerTabs storeContext={ctx.storeContext}/>
        </PageShell>
    );
}
