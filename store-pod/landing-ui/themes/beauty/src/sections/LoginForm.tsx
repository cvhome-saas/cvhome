import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {LoginData} from '@store-front/theme';
import {Button} from '@store-front/ui/button';
import {Input} from '@store-front/ui/input';
import {Label} from '@store-front/ui/label';
import {TagButton} from '../components/TagButton';

/**
 * A plain HTML form: the credentials post to cua full-page (`data.action`) with the store, locale and CSRF token
 * as hidden inputs, and cua sends the browser on to the OAuth2 callback. No client JavaScript is in the hand-off.
 */
export function LoginForm({data}: { data: LoginData }) {
    const t = useTranslations('PAGE.LOGIN');
    return (
        <div className="flex flex-col gap-6">
            {data.error && (
                <p role="alert" className="rounded-card border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">
                    {t(data.error === 'social' ? 'ERROR_SOCIAL' : data.error === 'expired' ? 'ERROR_EXPIRED' : 'ERROR_INVALID')}
                </p>
            )}
            <form method="post" action={data.action} className="flex flex-col gap-4">
                <input type="hidden" name="client_id" value={data.clientId}/>
                <input type="hidden" name="lang" value={data.lang}/>
                {data.csrfToken && <input type="hidden" name="_csrf" value={data.csrfToken}/>}
                <div className="flex flex-col gap-1.5">
                    <Label htmlFor="username" className="font-mono text-[0.7rem] uppercase tracking-wide"><span className="q">{t('USERNAME')}</span></Label>
                    <Input id="username" name="username" autoComplete="username" required autoFocus/>
                </div>
                <div className="flex flex-col gap-1.5">
                    <Label htmlFor="password" className="font-mono text-[0.7rem] uppercase tracking-wide"><span className="q">{t('PASSWORD')}</span></Label>
                    <Input id="password" name="password" type="password" autoComplete="current-password" required/>
                </div>
                <TagButton type="submit" size="lg">{t('SUBMIT')}</TagButton>
            </form>
            {data.socialLogins.length > 0 && (
                <div className="flex flex-col gap-2">
                    <p className="text-center text-xs uppercase tracking-wide text-muted-foreground">{t('OR')}</p>
                    {data.socialLogins.map(login => (
                        <Button key={login.providerId} asChild variant="outline">
                            <a href={login.href}>{t('SIGN_IN_WITH', {provider: login.name})}</a>
                        </Button>
                    ))}
                </div>
            )}
            <p className="text-center text-sm text-muted-foreground">
                {t('NO_ACCOUNT')}{' '}
                <Link prefetch={false} href="/register" className="text-foreground underline">{t('SIGN_UP')}</Link>
            </p>
        </div>
    );
}
