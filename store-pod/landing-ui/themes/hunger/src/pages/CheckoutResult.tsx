'use client'
import {useEffect} from 'react';
import {useTranslations} from 'next-intl';
import {useSearchParams} from 'next/navigation';
import type {CheckoutResultData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {useOrderStatus} from '@store-front/hooks/use-order-status';
import {useUser} from '@store-front/hooks/use-user';
import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../components/PageShell';

/**
 * The receipt. Verifies the real order status through the API — never trusts the success/cancel URL alone
 * — and prints the outcome as a stamped docket rather than a coloured icon card.
 */
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

    const state = isLoading ? null
        : isPaid ? {title: t('RESULT_SUCCESS_TITLE'), body: t('RESULT_SUCCESS_MESSAGE'), plate: true}
            : isPending ? {title: t('RESULT_PENDING_TITLE'), body: t('RESULT_PENDING_MESSAGE'), plate: false}
                : isCancelled ? {title: t('RESULT_FAILED_TITLE'), body: t('RESULT_FAILED_MESSAGE'), plate: false}
                    : {title: t('RESULT_NOT_FOUND'), body: '', plate: false};

    return (
        <PageShell width="narrow" className="py-section">
            <div className="border-2 border-foreground" aria-live="polite">
                {isLoading ? (
                    <div className="flex flex-col gap-3 p-6">
                        <Skeleton className="h-8 w-2/3"/><Skeleton className="h-4 w-full"/><Skeleton className="h-4 w-1/2"/>
                        <p className="press mt-2 text-xs tracking-wide text-muted-foreground">{t('RESULT_LOADING')}</p>
                    </div>
                ) : (
                    <>
                        <h1 className={`press px-4 py-3 text-3xl leading-none sm:text-4xl ${state!.plate ? 'plate' : 'border-b-2 border-foreground'}`}>
                            {state!.title}
                        </h1>
                        <div className="flex flex-col gap-4 p-4">
                            {state!.body && <p className="text-sm text-muted-foreground">{state!.body}</p>}
                            {orderStatus && (
                                <p className="flex items-baseline gap-2 border-y border-border py-2">
                                    <span className="press text-xs tracking-wide text-muted-foreground">{t('ORDER_NUMBER')}</span>
                                    <span aria-hidden className="leader"/>
                                    <span className="price text-xl">#{orderStatus.orderId}</span>
                                </p>
                            )}
                            <div className="flex flex-col gap-2 sm:flex-row">
                                {isPending && orderStatus?.redirectUrl && (
                                    <a href={orderStatus.redirectUrl} className="fold plate h-12 flex-1 justify-center border-primary text-base">{t('COMPLETE_PAYMENT')}</a>
                                )}
                                <Link prefetch={false} href="/"
                                      className={`fold h-12 flex-1 justify-center text-base ${isPending && orderStatus?.redirectUrl ? '' : 'plate border-primary'}`}>
                                    {t('BACK_TO_HOME')}
                                </Link>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </PageShell>
    );
}
