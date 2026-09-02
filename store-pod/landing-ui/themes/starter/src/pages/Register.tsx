import {getTranslations} from 'next-intl/server';
import type {PageProps, RegisterData} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {RegisterForm} from '../sections/RegisterForm';

export async function Register({ctx}: PageProps<RegisterData>) {
    const t = await getTranslations('PAGE.REGISTER');
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-12">
            <h1 className="text-3xl font-semibold tracking-tight">{t('HEADING', {store: ctx.store.name})}</h1>
            <RegisterForm storeContext={ctx.storeContext}/>
        </PageShell>
    );
}
