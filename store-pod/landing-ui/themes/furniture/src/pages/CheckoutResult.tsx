'use client'
import {useEffect} from 'react';
import {useTranslations} from 'next-intl';
import {useSearchParams} from 'next/navigation';
import type {CheckoutResultData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {useOrderStatus} from '@store-front/hooks/use-order-status';
import {useUser} from '@store-front/hooks/use-user';
import {Button} from '@store-front/ui/button';
import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../components/PageShell';
import {StatePlate} from '../components/StatePlate';

/**
 * The receipt at the desk. Verifies the real order status through the API — never trusts the
 * success/cancel URL alone — and prints the outcome as a word plus the order's own figure.
 */
export function CheckoutResult({ctx, data}: PageProps<CheckoutResultData>) {
    const t = useTranslations('PAGE.CHECKOUT');
    const ts = useTranslations('PAGE.CUSTOMER');
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
            <div className="rule-brass flex flex-col gap-6 border p-8 lg:p-12" aria-live="polite">
                {isLoading && <><Skeleton className="h-8 w-40 rounded-none"/><Skeleton className="h-5 w-64 rounded-none"/></>}

                {!isLoading && !orderStatus && <h1 className="sign-lg text-xl lg:text-2xl">{t('RESULT_NOT_FOUND')}</h1>}
                {isPaid && (
                    <>
                        <h1 className="sign-lg text-2xl lg:text-4xl">{t('RESULT_SUCCESS_TITLE')}</h1>
                        <p className="text-muted-foreground">{t('RESULT_SUCCESS_MESSAGE')}</p>
                    </>
                )}
                {isPending && (
                    <>
                        <h1 className="sign-lg text-2xl lg:text-4xl">{t('RESULT_PENDING_TITLE')}</h1>
                        <p className="text-muted-foreground">{t('RESULT_PENDING_MESSAGE')}</p>
                    </>
                )}
                {isCancelled && (
                    <>
                        <h1 className="sign-lg text-2xl lg:text-4xl">{t('RESULT_FAILED_TITLE')}</h1>
                        <p className="text-muted-foreground">{t('RESULT_FAILED_MESSAGE')}</p>
                    </>
                )}

                {orderStatus && (
                    <div className="rule-brass flex flex-wrap items-center justify-between gap-4 border-t pt-4">
                        <p className="flex items-baseline gap-3">
                            <span className="sign text-[0.625rem] text-muted-foreground">{t('ORDER_NUMBER')}</span>
                            <span className="figure text-lg">#{orderStatus.orderId}</span>
                        </p>
                        <StatePlate tone={isPaid ? 'enamel' : isCancelled ? 'sale' : 'ink'}>
                            {ts.has(orderStatus.orderStatus) ? ts(orderStatus.orderStatus) : orderStatus.orderStatus}
                        </StatePlate>
                    </div>
                )}

                <div className="mt-2 flex w-full flex-col gap-3 sm:flex-row">
                    {isPending && orderStatus?.redirectUrl && (
                        <Button asChild size="lg" className="sign flex-1 text-[0.6875rem]"><a href={orderStatus.redirectUrl}>{t('COMPLETE_PAYMENT')}</a></Button>
                    )}
                    <Button asChild size="lg" variant={isPending && orderStatus?.redirectUrl ? 'outline' : 'default'}
                            className="sign rule-brass flex-1 text-[0.6875rem]">
                        <Link prefetch={false} href="/">{t('BACK_TO_HOME')}</Link>
                    </Button>
                </div>
            </div>
        </PageShell>
    );
}
