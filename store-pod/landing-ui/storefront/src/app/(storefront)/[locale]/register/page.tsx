import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {getTheme} from '@/shell/theme/get-theme';
import {loadPageContext} from '@/shell/loaders/page-context';
import {DefaultRegisterPage} from '@/shell/theme/default-register-page';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.REGISTER');
    return {title: t('TITLE'), robots: {index: false}};
}

/** Self-registration. Needs no auth flow behind it — the store comes from the request, and cua is called as JSON. */
export default async function RegisterPage() {
    const [theme, ctx] = await Promise.all([getTheme(), loadPageContext()]);
    const Page = theme.pages.Register ?? DefaultRegisterPage;
    return <Page ctx={ctx} data={{}}/>;
}
