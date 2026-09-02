import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {LoginData, PageProps} from '@store-front/theme';
import {Button} from '@store-front/ui/button';
import {Input} from '@store-front/ui/input';
import {Label} from '@store-front/ui/label';

/**
 * The sign-in form for a theme that has not designed its own.
 *
 * Built only from design tokens, so it takes on the theme's type, colour, spacing and radius. It is a plain
 * HTML form: the credentials go to cua as a full-page POST, and cua sends the browser on to the OAuth2 callback
 * — no JavaScript is involved in the hand-off, which is what keeps it working when a theme's client bundle
 * has not loaded yet. A theme replaces it by exporting `pages.Login`.
 */
export function DefaultLoginPage({ctx, data}: PageProps<LoginData>) {
    const t = useTranslations('PAGE.LOGIN');
    return (
        <div className="mx-auto w-full max-w-md px-4 py-12">
            <h1 className="mb-6 text-2xl font-semibold">{t('HEADING', {store: ctx.store.name})}</h1>
            {data.error && (
                <p role="alert" className="mb-4 rounded-card border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">
                    {t(data.error === 'social' ? 'ERROR_SOCIAL' : data.error === 'expired' ? 'ERROR_EXPIRED' : 'ERROR_INVALID')}
                </p>
            )}
            <form method="post" action={data.action} className="flex flex-col gap-4">
                <input type="hidden" name="client_id" value={data.clientId}/>
                <input type="hidden" name="lang" value={data.lang}/>
                {data.csrfToken && <input type="hidden" name="_csrf" value={data.csrfToken}/>}
                <div className="flex flex-col gap-1.5">
                    <Label htmlFor="username">{t('USERNAME')}</Label>
                    <Input id="username" name="username" autoComplete="username" required autoFocus/>
                </div>
                <div className="flex flex-col gap-1.5">
                    <Label htmlFor="password">{t('PASSWORD')}</Label>
                    <Input id="password" name="password" type="password" autoComplete="current-password" required/>
                </div>
                <Button type="submit" className="mt-2 w-full">{t('SUBMIT')}</Button>
            </form>
            {data.socialLogins.length > 0 && (
                <div className="mt-6 flex flex-col gap-2">
                    <p className="text-center text-xs uppercase tracking-wide text-muted-foreground">{t('OR')}</p>
                    {data.socialLogins.map(login => (
                        <Button key={login.providerId} asChild variant="outline" className="w-full">
                            <a href={login.href}>{t('SIGN_IN_WITH', {provider: login.name})}</a>
                        </Button>
                    ))}
                </div>
            )}
            <p className="mt-6 text-center text-sm text-muted-foreground">
                {t('NO_ACCOUNT')}{' '}
                <Link prefetch={false} href="/register" className="text-foreground underline">{t('SIGN_UP')}</Link>
            </p>
        </div>
    );
}
