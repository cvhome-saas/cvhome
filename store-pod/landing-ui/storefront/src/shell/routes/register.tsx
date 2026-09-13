import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import type {ThemeDefinition} from '@store-front/theme';
import {DefaultRegisterPage} from '@/shell/theme/default-register-page';
import {loadPageContext} from '@/shell/loaders/page-context';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.REGISTER');
    return {title: t('TITLE'), robots: {index: false}};
}

/** Self-registration. Needs no auth flow behind it — the store comes from the request, and cua is called as JSON. */
export function registerPage(theme: ThemeDefinition) {
    return async function RegisterPage() {
        const ctx = await loadPageContext(theme);
        const Page = theme.pages.Register ?? DefaultRegisterPage;
        return <Page ctx={ctx} data={{}}/>;
    };
}
