import {getTranslations} from 'next-intl/server';
import type {LoginData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {LoginForm} from '../sections/LoginForm';

export async function Login({ctx, data}: PageProps<LoginData>) {
    const t = await getTranslations('PAGE.LOGIN');
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-12">
            <h1 className="text-3xl font-semibold tracking-tight">{t('HEADING', {store: ctx.store.name})}</h1>
            <LoginForm data={data}/>
        </PageShell>
    );
}
