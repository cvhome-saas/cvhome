import {getTranslations} from 'next-intl/server';
import type {LoginData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {PageHead} from '../components/PageHead';
import {LoginForm} from '../sections/LoginForm';

export async function Login({ctx, data}: PageProps<LoginData>) {
    const t = await getTranslations('PAGE.LOGIN');
    return (
        <PageShell width="narrow" className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
            <PageHead title={t('TITLE')} meta={t('HEADING', {store: ctx.store.name})}/>
            <LoginForm data={data}/>
        </PageShell>
    );
}
