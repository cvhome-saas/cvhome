'use client'
import {useTranslations} from 'next-intl';
import type {AgreementText, Order, StoreContext} from '@store-front/types';
import {defaultCheckoutValue} from '@store-front/types';
import {useCheckoutForm} from '@store-front/hooks/use-checkout-form';
import {useRouter} from '@store-front/i18n/navigation';
import {Input} from '@store-front/ui/input';
import {Label} from '@store-front/ui/label';
import {Textarea} from '@store-front/ui/textarea';
import {Checkbox} from '@store-front/ui/checkbox';
import {Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue} from '@store-front/ui/select';
import {
    AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle,
} from '@store-front/ui/alert-dialog';
import {cn} from '@store-front/ui/lib/utils';

type FieldProps = { id: string; label: string; error?: string; children: React.ReactNode; className?: string };

const SHEET_DIALOG = 'sheet rounded-none border-0 [--tilt:0deg]';
const DIALOG_TITLE = 'font-display text-2xl uppercase leading-none tracking-wide';
const SELECT_CONTENT = 'sheet rounded-none border-0 [--tilt:0deg]';
const SELECT_ITEM = 'rounded-none';

function Field({id, label, error, children, className}: FieldProps) {
    return (
        <div className={cn('flex flex-col gap-1.5', className)} data-invalid={!!error || undefined}>
            <Label htmlFor={id}>{label}</Label>
            {children}
            {error && <p id={`${id}-error`} role="alert" className="text-sm text-destructive">{error}</p>}
        </div>
    );
}

/** Shipping/billing + payment type + agreement on the checkout sheet. Behaviour is entirely `useCheckoutForm`. */
export function CheckoutForm({storeContext, requireLogin}: { storeContext: StoreContext; requireLogin: boolean }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const f = useCheckoutForm(storeContext, requireLogin);
    const e = f.errors;
    const inv = (msg?: string) => ({'aria-invalid': !!msg || undefined});

    return (
        <>
            <SuccessDialog order={f.order} open={f.successDialogOpen} onOpenChange={f.setSuccessDialogOpen}/>
            <AgreementDialog box={f.agreement} open={f.agreeDialogOpen} onOpenChange={f.setAgreeDialogOpen} setIsAgree={f.setIsAgree}/>
            <LoginRequiredDialog open={f.loginRequiredDialogOpen} onOpenChange={f.setLoginRequiredDialogOpen} login={f.login}/>

            <form onSubmit={f.handleSubmit(f.onSubmit)} noValidate className="flex flex-col gap-6">
                <h2 className="font-display text-2xl uppercase leading-none tracking-wide">{t('FORM_TITLE')}</h2>
                <div className="grid gap-4 sm:grid-cols-2">
                    <Field id="firstName" label={t('FIRST_NAME')} error={e.customer?.billing?.firstName?.message}>
                        <Input id="firstName" autoComplete="given-name" {...f.register('customer.billing.firstName')} {...inv(e.customer?.billing?.firstName?.message)}/>
                    </Field>
                    <Field id="lastName" label={t('LAST_NAME')} error={e.customer?.billing?.lastName?.message}>
                        <Input id="lastName" autoComplete="family-name" {...f.register('customer.billing.lastName')} {...inv(e.customer?.billing?.lastName?.message)}/>
                    </Field>
                    <Field id="emailAddress" label={t('EMAIL')} error={e.customer?.emailAddress?.message}>
                        <Input id="emailAddress" type="email" autoComplete="email" {...f.register('customer.emailAddress')} {...inv(e.customer?.emailAddress?.message)}/>
                    </Field>
                    <Field id="phone" label={t('PHONE')} error={e.customer?.billing?.phone?.message}>
                        <Input id="phone" type="tel" autoComplete="tel" dir="ltr" {...f.register('customer.billing.phone')} {...inv(e.customer?.billing?.phone?.message)}/>
                    </Field>
                </div>
                <div className="grid gap-4 sm:grid-cols-3">
                    <Field id="country" label={t('COUNTRY')} error={e.customer?.billing?.country?.message}>
                        <Select onValueChange={v => f.setValue('customer.billing.country', v, {shouldValidate: true})} defaultValue={defaultCheckoutValue.customer.billing.country || undefined}>
                            <SelectTrigger id="country" className="w-full" {...inv(e.customer?.billing?.country?.message)}><SelectValue placeholder={t('SELECT_COUNTRY')}/></SelectTrigger>
                            <SelectContent className={SELECT_CONTENT}>
                                <SelectGroup>{f.readableCountryList?.map(c => <SelectItem key={c.code} value={c.code} className={SELECT_ITEM}>{c.name}</SelectItem>)}</SelectGroup>
                            </SelectContent>
                        </Select>
                    </Field>
                    <Field id="city" label={t('CITY')} error={e.customer?.billing?.city?.message}>
                        <Input id="city" autoComplete="address-level2" {...f.register('customer.billing.city')} {...inv(e.customer?.billing?.city?.message)}/>
                    </Field>
                    <Field id="postalCode" label={t('POSTAL_CODE')} error={e.customer?.billing?.postalCode?.message}>
                        <Input id="postalCode" autoComplete="postal-code" {...f.register('customer.billing.postalCode')} {...inv(e.customer?.billing?.postalCode?.message)}/>
                    </Field>
                </div>
                <Field id="address" label={t('ADDRESS')} error={e.customer?.billing?.address?.message}>
                    <Textarea id="address" autoComplete="street-address" {...f.register('customer.billing.address')} {...inv(e.customer?.billing?.address?.message)}/>
                </Field>
                <Field id="paymentType" label={t('PAYMENT_TYPE')} error={e.paymentType?.message}>
                    <Select onValueChange={v => f.setValue('paymentType', v as never, {shouldValidate: true})} defaultValue={f.supportedPaymentTypes?.[0]}>
                        <SelectTrigger id="paymentType" className="w-full" {...inv(e.paymentType?.message)}><SelectValue placeholder={t('SELECT_PAYMENT_TYPE')}/></SelectTrigger>
                        <SelectContent className={SELECT_CONTENT}>
                            <SelectGroup>{f.supportedPaymentTypes?.map(p => <SelectItem key={p} value={p} className={SELECT_ITEM}>{t(`PAYMENT_TYPES.${p}`)}</SelectItem>)}</SelectGroup>
                        </SelectContent>
                    </Select>
                </Field>
                {f.agreement && (
                    <div className="flex items-start gap-2">
                        <Checkbox id="isAgree" className="mt-0.5 rounded-none" checked={f.isAgree} onCheckedChange={c => f.setIsAgree(!!c)} aria-invalid={!!e.customer?.billing?.isAgree}/>
                        <div className="flex flex-col gap-1">
                            <Label htmlFor="isAgree" className="font-sans text-sm normal-case tracking-normal">
                                {t('AGREEMENT')}{' '}
                                <button type="button" onClick={f.handleClickOnAgreement} className="underline decoration-primary decoration-2 underline-offset-4">{t('TERMS_AND_CONDITIONS')}</button>
                            </Label>
                            {e.customer?.billing?.isAgree && !f.isAgree && <p role="alert" className="text-sm text-destructive">{e.customer.billing.isAgree.message}</p>}
                        </div>
                    </div>
                )}
                <button type="submit" className="glo h-12 self-start px-8 text-base">{t('PLACE_ORDER')}</button>
            </form>
        </>
    );
}

function SuccessDialog({order, open, onOpenChange}: { order: Order | undefined; open: boolean; onOpenChange: (o: boolean) => void }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const router = useRouter();
    return (
        <AlertDialog open={open} onOpenChange={onOpenChange}>
            <AlertDialogContent className={SHEET_DIALOG}>
                <AlertDialogHeader className="items-center gap-4 text-center">
                    <AlertDialogTitle asChild><h2 className="stamp stamp-lg stamp-glo max-w-full whitespace-normal">{t('ORDER_PLACED_SUCCESSFULLY')}</h2></AlertDialogTitle>
                    <AlertDialogDescription className="tabular-nums">{order ? `${t('ORDER_ID')}: ${order.id}` : t('NO_ORDER_DETAILED')}</AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogAction className="glo h-11 w-full rounded-none text-sm hover:bg-primary-hover" onClick={() => { onOpenChange(false); router.push('/'); }}>{t('CONTINUE_SHOPPING')}</AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
}

function AgreementDialog({box, open, onOpenChange, setIsAgree}: { box: AgreementText | undefined; open: boolean; onOpenChange: (o: boolean) => void; setIsAgree: (a: boolean) => void }) {
    const t = useTranslations('PAGE.CHECKOUT');
    return (
        <AlertDialog open={open} onOpenChange={onOpenChange}>
            <AlertDialogContent className={SHEET_DIALOG}>
                <AlertDialogHeader>
                    <AlertDialogTitle className={DIALOG_TITLE}>{t('TERMS_AND_CONDITIONS')}</AlertDialogTitle>
                    {box && <AlertDialogDescription asChild><div className="max-h-64 overflow-y-auto text-start text-sm [&_a]:underline" dangerouslySetInnerHTML={{__html: box.html}}/></AlertDialogDescription>}
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel className="strip strip-hover h-11 rounded-none border-0 text-sm [--tilt:0deg]" onClick={() => setIsAgree(false)}>{t('REJECT')}</AlertDialogCancel>
                    <AlertDialogAction className="glo h-11 rounded-none text-sm hover:bg-primary-hover" onClick={() => setIsAgree(true)}>{t('AGREE')}</AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
}

function LoginRequiredDialog({open, onOpenChange, login}: { open: boolean; onOpenChange: (o: boolean) => void; login: () => void }) {
    const t = useTranslations('PAGE.CHECKOUT');
    return (
        <AlertDialog open={open} onOpenChange={onOpenChange}>
            <AlertDialogContent className={SHEET_DIALOG}>
                <AlertDialogHeader className="items-center text-center">
                    <AlertDialogTitle className={DIALOG_TITLE}>{t('LOGIN_REQUIRED_TITLE')}</AlertDialogTitle>
                    <AlertDialogDescription>{t('LOGIN_REQUIRED_DESCRIPTION')}</AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter className="sm:justify-center">
                    <AlertDialogCancel className="strip strip-hover h-11 rounded-none border-0 text-sm [--tilt:0deg]">{t('CANCEL')}</AlertDialogCancel>
                    <AlertDialogAction className="glo h-11 rounded-none text-sm hover:bg-primary-hover" onClick={login}>{t('LOGIN')}</AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
}
