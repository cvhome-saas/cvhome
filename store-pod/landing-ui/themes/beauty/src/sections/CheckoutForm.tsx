'use client'
import {useTranslations} from 'next-intl';
import {CheckCircle2Icon, HelpCircleIcon} from 'lucide-react';
import type {Box, Order, StoreContext} from '@store-front/types';
import {defaultCheckoutValue} from '@store-front/types';
import {useCheckoutForm} from '@store-front/hooks/use-checkout-form';
import {useRouter} from '@store-front/i18n/navigation';
import {parseDescription} from '@store-front/services/description-view-util';
import {TagButton} from '../components/TagButton';
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

function Field({id, label, error, children, className}: FieldProps) {
    return (
        <div className={cn('flex flex-col gap-1.5', className)} data-invalid={!!error || undefined}>
            <Label htmlFor={id} className="font-mono text-[0.7rem] uppercase tracking-wide"><span className="q">{label}</span></Label>
            {children}
            {error && <p id={`${id}-error`} role="alert" className="font-mono text-xs text-destructive">{error}</p>}
        </div>
    );
}

/** Shipping/billing + payment type + agreement. Behaviour is entirely `useCheckoutForm`. */
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
                <h2 className="q font-display text-2xl font-semibold uppercase tracking-tight">{t('FORM_TITLE')}</h2>
                <div className="grid gap-4 sm:grid-cols-2">
                    <Field id="firstName" label={t('FIRST_NAME')} error={e.customer?.billing?.firstName?.message}>
                        <Input className="rounded-none border-foreground font-mono text-sm" id="firstName" autoComplete="given-name" {...f.register('customer.billing.firstName')} {...inv(e.customer?.billing?.firstName?.message)}/>
                    </Field>
                    <Field id="lastName" label={t('LAST_NAME')} error={e.customer?.billing?.lastName?.message}>
                        <Input className="rounded-none border-foreground font-mono text-sm" id="lastName" autoComplete="family-name" {...f.register('customer.billing.lastName')} {...inv(e.customer?.billing?.lastName?.message)}/>
                    </Field>
                    <Field id="emailAddress" label={t('EMAIL')} error={e.customer?.emailAddress?.message}>
                        <Input className="rounded-none border-foreground font-mono text-sm" id="emailAddress" type="email" autoComplete="email" {...f.register('customer.emailAddress')} {...inv(e.customer?.emailAddress?.message)}/>
                    </Field>
                    <Field id="phone" label={t('PHONE')} error={e.customer?.billing?.phone?.message}>
                        <Input className="rounded-none border-foreground font-mono text-sm" id="phone" type="tel" autoComplete="tel" dir="ltr" {...f.register('customer.billing.phone')} {...inv(e.customer?.billing?.phone?.message)}/>
                    </Field>
                </div>
                <div className="grid gap-4 sm:grid-cols-3">
                    <Field id="country" label={t('COUNTRY')} error={e.customer?.billing?.country?.message}>
                        <Select onValueChange={v => f.setValue('customer.billing.country', v, {shouldValidate: true})} defaultValue={defaultCheckoutValue.customer.billing.country || undefined}>
                            <SelectTrigger id="country" className="w-full rounded-none border-foreground font-mono text-sm" {...inv(e.customer?.billing?.country?.message)}><SelectValue placeholder={t('SELECT_COUNTRY')}/></SelectTrigger>
                            <SelectContent>
                                <SelectGroup>{f.readableCountryList?.map(c => <SelectItem key={c.code} value={c.code}>{c.name}</SelectItem>)}</SelectGroup>
                            </SelectContent>
                        </Select>
                    </Field>
                    <Field id="city" label={t('CITY')} error={e.customer?.billing?.city?.message}>
                        <Input className="rounded-none border-foreground font-mono text-sm" id="city" autoComplete="address-level2" {...f.register('customer.billing.city')} {...inv(e.customer?.billing?.city?.message)}/>
                    </Field>
                    <Field id="postalCode" label={t('POSTAL_CODE')} error={e.customer?.billing?.postalCode?.message}>
                        <Input className="rounded-none border-foreground font-mono text-sm" id="postalCode" autoComplete="postal-code" {...f.register('customer.billing.postalCode')} {...inv(e.customer?.billing?.postalCode?.message)}/>
                    </Field>
                </div>
                <Field id="address" label={t('ADDRESS')} error={e.customer?.billing?.address?.message}>
                    <Textarea className="rounded-none border-foreground font-mono text-sm" id="address" autoComplete="street-address" {...f.register('customer.billing.address')} {...inv(e.customer?.billing?.address?.message)}/>
                </Field>
                <Field id="paymentType" label={t('PAYMENT_TYPE')} error={e.paymentType?.message}>
                    <Select onValueChange={v => f.setValue('paymentType', v as never, {shouldValidate: true})} defaultValue={f.supportedPaymentTypes?.[0]}>
                        <SelectTrigger id="paymentType" className="w-full rounded-none border-foreground font-mono text-sm" {...inv(e.paymentType?.message)}><SelectValue placeholder={t('SELECT_PAYMENT_TYPE')}/></SelectTrigger>
                        <SelectContent>
                            <SelectGroup>{f.supportedPaymentTypes?.map(p => <SelectItem key={p} value={p}>{t(`PAYMENT_TYPES.${p}`)}</SelectItem>)}</SelectGroup>
                        </SelectContent>
                    </Select>
                </Field>
                {f.agreement && (
                    <div className="flex items-start gap-2">
                        <Checkbox id="isAgree" checked={f.isAgree} onCheckedChange={c => f.setIsAgree(!!c)} aria-invalid={!!e.customer?.billing?.isAgree}/>
                        <div className="flex flex-col gap-1">
                            <Label htmlFor="isAgree" className="font-normal">
                                {t('AGREEMENT')}{' '}
                                <button type="button" onClick={f.handleClickOnAgreement} className="underline">{t('TERMS_AND_CONDITIONS')}</button>
                            </Label>
                            {e.customer?.billing?.isAgree && !f.isAgree && <p role="alert" className="text-sm text-destructive">{e.customer.billing.isAgree.message}</p>}
                        </div>
                    </div>
                )}
                <TagButton type="submit" size="lg" className="self-start">{t('PLACE_ORDER')}</TagButton>
            </form>
        </>
    );
}

const dialogContent = 'rounded-none border border-foreground bg-background p-0 shadow-overlay gap-0 overflow-hidden';
const dialogTitle = 'font-display text-2xl font-semibold uppercase tracking-tight';
const dialogBody = 'font-mono text-sm text-muted-foreground';
const plateButton = 'plate inline-flex h-10 items-center justify-center rounded-none px-4 font-display text-sm font-semibold uppercase tracking-wide hover:bg-foreground hover:text-background';

/** Dialogs in the world: a hazard band across the top, quoted display title, mono body, tag for the primary action. */
function SuccessDialog({order, open, onOpenChange}: { order: Order | undefined; open: boolean; onOpenChange: (o: boolean) => void }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const router = useRouter();
    return (
        <AlertDialog open={open} onOpenChange={onOpenChange}>
            <AlertDialogContent className={dialogContent}>
                <div className="hazard h-3 border-b border-foreground" aria-hidden/>
                <AlertDialogHeader className="items-center gap-3 p-6 text-center">
                    <span className="tag flex size-12 items-center justify-center rounded-none !pe-0"><CheckCircle2Icon className="size-6"/></span>
                    <AlertDialogTitle className={dialogTitle}><span className="q">{t('ORDER_PLACED_SUCCESSFULLY')}</span></AlertDialogTitle>
                    <AlertDialogDescription className={dialogBody}>
                        {order ? <span className="plate inline-flex px-2 py-1 text-foreground">{t('ORDER_ID')}: <b className="ms-1">#{order.id}</b></span> : t('NO_ORDER_DETAILED')}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter className="border-t border-foreground p-4">
                    <AlertDialogAction asChild>
                        <TagButton size="lg" className="w-full" onClick={() => { onOpenChange(false); router.push('/'); }}><span className="q">{t('CONTINUE_SHOPPING')}</span></TagButton>
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
}

function AgreementDialog({box, open, onOpenChange, setIsAgree}: { box: Box | undefined; open: boolean; onOpenChange: (o: boolean) => void; setIsAgree: (a: boolean) => void }) {
    const t = useTranslations('PAGE.CHECKOUT');
    return (
        <AlertDialog open={open} onOpenChange={onOpenChange}>
            <AlertDialogContent className={dialogContent}>
                <div className="hazard h-3 border-b border-foreground" aria-hidden/>
                <AlertDialogHeader className="gap-3 p-6 text-start">
                    <AlertDialogTitle className={dialogTitle}><span className="q">{t('TERMS_AND_CONDITIONS')}</span></AlertDialogTitle>
                    {box && <AlertDialogDescription asChild><div className="plate max-h-64 overflow-y-auto p-3 font-mono text-sm leading-relaxed text-foreground [&_a]:underline [&_p]:mb-2" dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}/></AlertDialogDescription>}
                </AlertDialogHeader>
                <AlertDialogFooter className="gap-2 border-t border-foreground p-4">
                    <AlertDialogCancel asChild><button type="button" className={plateButton} onClick={() => setIsAgree(false)}><span className="q">{t('REJECT')}</span></button></AlertDialogCancel>
                    <AlertDialogAction asChild><TagButton onClick={() => setIsAgree(true)}><span className="q">{t('AGREE')}</span></TagButton></AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
}

function LoginRequiredDialog({open, onOpenChange, login}: { open: boolean; onOpenChange: (o: boolean) => void; login: () => void }) {
    const t = useTranslations('PAGE.CHECKOUT');
    return (
        <AlertDialog open={open} onOpenChange={onOpenChange}>
            <AlertDialogContent className={dialogContent}>
                <div className="hazard h-3 border-b border-foreground" aria-hidden/>
                <AlertDialogHeader className="items-center gap-3 p-6 text-center">
                    <span className="plate flex size-12 items-center justify-center"><HelpCircleIcon className="size-6"/></span>
                    <AlertDialogTitle className={dialogTitle}><span className="q">{t('LOGIN_REQUIRED_TITLE')}</span></AlertDialogTitle>
                    <AlertDialogDescription className={dialogBody}>{t('LOGIN_REQUIRED_DESCRIPTION')}</AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter className="gap-2 border-t border-foreground p-4 sm:justify-center">
                    <AlertDialogCancel asChild><button type="button" className={plateButton}><span className="q">{t('CANCEL')}</span></button></AlertDialogCancel>
                    <AlertDialogAction asChild><TagButton onClick={login}><span className="q">{t('LOGIN')}</span></TagButton></AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
}
