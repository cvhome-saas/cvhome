'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {StoreContext} from '@store-front/types';
import {useRegisterForm, type RegistrationField} from '@store-front/hooks/use-register-form';
import {Button} from '@store-front/ui/button';
import {Input} from '@store-front/ui/input';
import {Label} from '@store-front/ui/label';

type FieldProps = { id: RegistrationField; label: string; error?: string; children: React.ReactNode };

function Field({id, label, error, children}: FieldProps) {
    return (
        <div className="flex flex-col gap-2" data-invalid={!!error || undefined}>
            <Label htmlFor={id} className="sign text-[0.625rem] text-muted-foreground">{label}</Label>
            {children}
            {error && <p id={`${id}-error`} role="alert" className="text-sm text-destructive">{error}</p>}
        </div>
    );
}

/** Behaviour is entirely `useRegisterForm`; on success it hands the new shopper into the login flow. */
export function RegisterForm({storeContext}: { storeContext: StoreContext }) {
    const t = useTranslations('PAGE.REGISTER');
    const f = useRegisterForm(storeContext);
    const control = (name: RegistrationField, props: React.ComponentProps<'input'> = {}) => (
        <Input id={name} name={name} value={f.values[name] ?? ''} onChange={e => f.set(name, e.target.value)}
               aria-invalid={f.fieldErrors[name] ? true : undefined}
               aria-describedby={f.fieldErrors[name] ? `${name}-error` : undefined} {...props}/>
    );

    return (
        <div className="flex flex-col gap-6">
            {f.error && (
                <p role="alert" className="rounded-card border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">{f.error}</p>
            )}
            <form onSubmit={e => { e.preventDefault(); f.submit(); }} noValidate className="flex flex-col gap-4">
                <Field id="username" label={t('USERNAME')} error={f.fieldErrors.username}>
                    {control('username', {autoComplete: 'username', required: true, minLength: 3, maxLength: 50})}
                </Field>
                <Field id="email" label={t('EMAIL')} error={f.fieldErrors.email}>
                    {control('email', {type: 'email', autoComplete: 'email', required: true})}
                </Field>
                <Field id="password" label={t('PASSWORD')} error={f.fieldErrors.password}>
                    {control('password', {type: 'password', autoComplete: 'new-password', required: true, minLength: 6})}
                </Field>
                <div className="grid gap-4 sm:grid-cols-2">
                    <Field id="firstName" label={t('FIRST_NAME')} error={f.fieldErrors.firstName}>
                        {control('firstName', {autoComplete: 'given-name'})}
                    </Field>
                    <Field id="lastName" label={t('LAST_NAME')} error={f.fieldErrors.lastName}>
                        {control('lastName', {autoComplete: 'family-name'})}
                    </Field>
                </div>
                <Button type="submit" size="lg" className="sign px-8 text-[0.6875rem]" disabled={f.submitting}>{t('SUBMIT')}</Button>
            </form>
            <p className="text-center text-sm text-muted-foreground">
                {t('ALREADY')}{' '}
                <Link prefetch={false} href="/login" className="text-foreground underline">{t('SIGN_IN')}</Link>
            </p>
        </div>
    );
}
