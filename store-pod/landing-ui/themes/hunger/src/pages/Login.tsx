import {getTranslations} from 'next-intl/server';
import type {LoginData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {LoginForm} from '../sections/LoginForm';

export async function Login({ctx, data}: PageProps<LoginData>) {
    const t = await getTranslations('PAGE.LOGIN');
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-6">
            <div className="flex flex-col gap-2">
                <h1 className="press plate px-3 py-2 text-3xl leading-none sm:text-4xl">{t('TITLE')}</h1>
                <p className="text-sm text-muted-foreground">{t('HEADING', {store: ctx.store.name})}</p>
            </div>
            <LoginForm data={data}/>
        </PageShell>
    );
}
