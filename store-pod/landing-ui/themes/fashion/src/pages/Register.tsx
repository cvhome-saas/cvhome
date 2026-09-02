import {getTranslations} from 'next-intl/server';
import type {PageProps, RegisterData} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {RegisterForm} from '../sections/RegisterForm';

export async function Register({ctx}: PageProps<RegisterData>) {
    const t = await getTranslations('PAGE.REGISTER');
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-6 lg:py-8">
            <div className="flex flex-col gap-2">
                <h1 className="strip h-12 w-fit text-2xl [--tilt:-0.8deg] sm:text-3xl">{t('TITLE')}</h1>
                <p className="text-sm text-muted-foreground">{t('HEADING', {store: ctx.store.name})}</p>
            </div>
            <div className="sheet p-5 [--tilt:0.25deg] sm:p-7">
                <RegisterForm storeContext={ctx.storeContext}/>
            </div>
        </PageShell>
    );
}
