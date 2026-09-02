import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import type {LoginData, LoginError} from '@store-front/theme';
import {AuthService} from '@store-front/services/auth-service';
import {orUndefined} from '@store-front/services/http-utils';
import {getStoreContext} from '@/shell/request/store-context';
import {getTheme} from '@/shell/theme/get-theme';
import {loadPageContext} from '@/shell/loaders/page-context';
import {DefaultLoginPage} from '@/shell/theme/default-login-page';
import {LoginRedirect} from '@/shell/auth/login-redirect';

type Props = { searchParams: Promise<Record<string, string | string[] | undefined>> };

const ERRORS: readonly LoginError[] = ['invalid', 'social'];

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.LOGIN');
    return {title: t('TITLE'), robots: {index: false}};
}

/**
 * Two pages under one route.
 *
 * Without `?auth=1` this is the entry into sign-in: it starts the authorization-code flow against cua, which
 * is what deep links and the `/customer/**` gate rely on. cua then sends the browser back here *with* the
 * marker, meaning "I am holding your authorize request; show me credentials" — and that is when the themed
 * form renders. The form posts straight to cua and the flow resumes to `/callback`. The marker page never
 * redirects on its own, so the two halves cannot loop.
 */
export default async function LoginPage({searchParams}: Props) {
    const sp = await searchParams;
    const storeContext = await getStoreContext();
    if (sp.auth !== '1') {
        return <LoginRedirect storeContext={storeContext}/>;
    }
    const [theme, ctx, socialLogins] = await Promise.all([
        getTheme(), loadPageContext(), orUndefined(AuthService.socialLogins(storeContext)),
    ]);
    const error = typeof sp.error === 'string' && (ERRORS as readonly string[]).includes(sp.error)
        ? sp.error as LoginError : undefined;
    const data: LoginData = {
        action: AuthService.loginAction(storeContext),
        clientId: storeContext.store,
        lang: storeContext.locale,
        error,
        socialLogins: (socialLogins ?? []).map(login => ({
            providerId: login.providerId,
            name: login.name,
            href: AuthService.socialLoginHref(storeContext, login),
        })),
    };
    const Page = theme.pages.Login ?? DefaultLoginPage;
    return <Page ctx={ctx} data={data}/>;
}
