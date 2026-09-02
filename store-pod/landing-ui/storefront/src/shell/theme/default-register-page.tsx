'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {PageProps, RegisterData} from '@store-front/theme';
import {useRegisterForm, type RegistrationField} from '@store-front/hooks/use-register-form';
import {Button} from '@store-front/ui/button';
import {Input} from '@store-front/ui/input';
import {Label} from '@store-front/ui/label';

/**
 * The registration form for a theme that has not designed its own. Token-only, like the login fallback; the
 * behaviour is entirely `useRegisterForm`. A theme replaces it by exporting `pages.Register`.
 */
export function DefaultRegisterPage({ctx}: PageProps<RegisterData>) {
    const t = useTranslations('PAGE.REGISTER');
    const form = useRegisterForm(ctx.storeContext);

    const field = (name: RegistrationField, label: string, props: React.ComponentProps<'input'> = {}) => {
        const error = form.fieldErrors[name];
        return (
            <div className="flex flex-col gap-1.5">
                <Label htmlFor={name}>{label}</Label>
                <Input id={name} name={name} value={form.values[name] ?? ''} onChange={e => form.set(name, e.target.value)}
                       aria-invalid={error ? true : undefined} aria-describedby={error ? `${name}-error` : undefined} {...props}/>
                {error && <p id={`${name}-error`} role="alert" className="text-sm text-destructive">{error}</p>}
            </div>
        );
    };

    return (
        <div className="mx-auto w-full max-w-md px-4 py-12">
            <h1 className="mb-6 text-2xl font-semibold">{t('HEADING', {store: ctx.store.name})}</h1>
            {form.error && (
                <p role="alert" className="mb-4 rounded-card border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">
                    {form.error}
                </p>
            )}
            <form onSubmit={e => { e.preventDefault(); form.submit(); }} noValidate className="flex flex-col gap-4">
                {field('username', t('USERNAME'), {autoComplete: 'username', required: true, minLength: 3, maxLength: 50})}
                {field('email', t('EMAIL'), {type: 'email', autoComplete: 'email', required: true})}
                {field('password', t('PASSWORD'), {type: 'password', autoComplete: 'new-password', required: true, minLength: 6})}
                <div className="grid gap-4 sm:grid-cols-2">
                    {field('firstName', t('FIRST_NAME'), {autoComplete: 'given-name'})}
                    {field('lastName', t('LAST_NAME'), {autoComplete: 'family-name'})}
                </div>
                <Button type="submit" className="mt-2 w-full" disabled={form.submitting}>{t('SUBMIT')}</Button>
            </form>
            <p className="mt-6 text-center text-sm text-muted-foreground">
                {t('ALREADY')}{' '}
                <Link prefetch={false} href="/login" className="text-foreground underline">{t('SIGN_IN')}</Link>
            </p>
        </div>
    );
}
