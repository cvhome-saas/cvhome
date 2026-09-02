import {getTranslations} from 'next-intl/server';
import type {PageProps, RegisterData} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {PageHead} from '../components/PageHead';
import {RegisterForm} from '../sections/RegisterForm';

export async function Register({ctx}: PageProps<RegisterData>) {
    const t = await getTranslations('PAGE.REGISTER');
    return (
        <PageShell width="narrow" className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
            <PageHead title={t('TITLE')} meta={t('HEADING', {store: ctx.store.name})}/>
            <RegisterForm storeContext={ctx.storeContext}/>
        </PageShell>
    );
}
