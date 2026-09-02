import {getTranslations} from 'next-intl/server';
import type {PageProps, RegisterData} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {RegisterForm} from '../sections/RegisterForm';

export async function Register({ctx}: PageProps<RegisterData>) {
    const t = await getTranslations('PAGE.REGISTER');
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8 lg:py-10">
            <div className="flex flex-col gap-2">
                <h1 className="hair display border-b-2 pb-4 text-3xl sm:text-4xl">{t('TITLE')}</h1>
                <p className="text-sm text-muted-foreground">{t('HEADING', {store: ctx.store.name})}</p>
            </div>
            <RegisterForm storeContext={ctx.storeContext}/>
        </PageShell>
    );
}
