import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {DefaultRegisterPage} from '@/shell/theme/default-register-page';
import {themed, type ThemeSource} from './theme-source';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.REGISTER');
    return {title: t('TITLE'), robots: {index: false}};
}

/** Self-registration. Needs no auth flow behind it — the store comes from the request, and cua is called as JSON. */
export function registerPage(theme: ThemeSource) {
    return async function RegisterPage() {
        const {theme: resolved, ctx} = await themed(theme);
        const Page = resolved.pages.Register ?? DefaultRegisterPage;
        return <Page ctx={ctx} data={{}}/>;
    };
}
