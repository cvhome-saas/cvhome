'use client'
import {useEffect} from 'react';
import {useTranslations} from 'next-intl';
import {useSearchParams} from 'next/navigation';
import {CheckCircle2Icon, ClockIcon, XCircleIcon} from 'lucide-react';
import type {CheckoutResultData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {useOrderStatus} from '@store-front/hooks/use-order-status';
import {useUser} from '@store-front/hooks/use-user';
import {TagButton} from '../components/TagButton';
import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../components/PageShell';

/** Verifies the real order status through the API — never trusts the success/cancel URL alone. */
export function CheckoutResult({ctx, data}: PageProps<CheckoutResultData>) {
    const t = useTranslations('PAGE.CHECKOUT');
    const orderIdParam = useSearchParams().get('orderId');
    const orderId = orderIdParam ? Number(orderIdParam) : undefined;
    const {user, loading: userLoading, login} = useUser(ctx.storeContext);
    const awaitingLogin = data.requireLogin && (userLoading || !user);
    useEffect(() => { if (data.requireLogin && !userLoading && !user) login(); }, [data.requireLogin, userLoading, user, login]);
    const {orderStatus, loading} = useOrderStatus(ctx.storeContext, awaitingLogin ? undefined : orderId);

    const isLoading = awaitingLogin || (loading && !orderStatus);
    const isPaid = orderStatus?.orderStatus === 'CONFIRMED' || orderStatus?.paymentStatus === 'PAID';
    const isCancelled = orderStatus?.orderStatus === 'CANCELLED' || orderStatus?.paymentStatus === 'FAILED';
    const isPending = !!orderStatus && !isPaid && !isCancelled;

    return (
        <PageShell width="narrow" className="py-section">
            <div className="plate relative flex flex-col items-center gap-6 p-8 text-center" aria-live="polite">
                <div className="hazard absolute inset-x-0 top-0 h-2" aria-hidden/>
                {isLoading && <><Skeleton className="size-14 rounded-none"/><p className="font-mono text-sm uppercase tracking-wide text-muted-foreground">{t('RESULT_LOADING')}</p></>}
                {!isLoading && !orderStatus && <><XCircleIcon className="size-14 text-muted-foreground"/><p className="q font-display text-xl font-semibold uppercase">{t('RESULT_NOT_FOUND')}</p></>}
                {isPaid && <><CheckCircle2Icon className="size-14 text-success"/><div><h1 className="q font-display text-3xl font-bold uppercase tracking-tight">{t('RESULT_SUCCESS_TITLE')}</h1><p className="mt-2 font-mono text-sm text-muted-foreground">{t('RESULT_SUCCESS_MESSAGE')}</p></div></>}
                {isPending && <><ClockIcon className="size-14 text-warning"/><div><h1 className="q font-display text-3xl font-bold uppercase tracking-tight">{t('RESULT_PENDING_TITLE')}</h1><p className="mt-2 font-mono text-sm text-muted-foreground">{t('RESULT_PENDING_MESSAGE')}</p></div></>}
                {isCancelled && <><XCircleIcon className="size-14 text-destructive"/><div><h1 className="q font-display text-3xl font-bold uppercase tracking-tight">{t('RESULT_FAILED_TITLE')}</h1><p className="mt-2 font-mono text-sm text-muted-foreground">{t('RESULT_FAILED_MESSAGE')}</p></div></>}
                {orderStatus && <p className="plate px-3 py-1 font-mono text-xs uppercase tracking-wide">{t('ORDER_NUMBER')}: <span className="font-bold">#{orderStatus.orderId}</span></p>}
                <div className="flex w-full flex-col gap-3 sm:flex-row">
                    {isPending && orderStatus?.redirectUrl && <TagButton asChild size="lg" className="flex-1"><a href={orderStatus.redirectUrl}>{t('COMPLETE_PAYMENT')}</a></TagButton>}
                    {isPending && orderStatus?.redirectUrl
                        ? <Link prefetch={false} href="/" className="plate inline-flex h-12 flex-1 items-center justify-center font-display text-base font-semibold uppercase tracking-wide hover:bg-foreground hover:text-background"><span className="q">{t('BACK_TO_HOME')}</span></Link>
                        : <TagButton asChild size="lg" className="flex-1"><Link prefetch={false} href="/">{t('BACK_TO_HOME')}</Link></TagButton>}
                </div>
            </div>
        </PageShell>
    );
}
