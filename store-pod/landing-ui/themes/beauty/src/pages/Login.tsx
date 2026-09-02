import {getTranslations} from 'next-intl/server';
import type {LoginData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {LoginForm} from '../sections/LoginForm';

export async function Login({ctx, data}: PageProps<LoginData>) {
    const t = await getTranslations('PAGE.LOGIN');
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8">
            <div className="flex flex-col gap-2">
                <h1 className="q font-display text-4xl font-bold uppercase leading-[0.9] tracking-tight [overflow-wrap:anywhere] sm:text-5xl">{t('TITLE')}</h1>
                <p className="text-sm text-muted-foreground">{t('HEADING', {store: ctx.store.name})}</p>
            </div>
            <div className="plate p-5 sm:p-6">
                <LoginForm data={data}/>
            </div>
        </PageShell>
    );
}
