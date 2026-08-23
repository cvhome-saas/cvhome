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

/** The receipt sheet: the real order status (re-read from the API, never trusted from the URL) as a big stamp. */
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
            <div className="sheet sheen peel flex flex-col items-center gap-6 p-8 text-center [--tilt:-0.6deg] sm:p-12" aria-live="polite">
                {isLoading && <><Skeleton className="h-14 w-48 rounded-none"/><p className="text-muted-foreground">{t('RESULT_LOADING')}</p></>}
                {!isLoading && !orderStatus && <><span className="stamp stamp-lg">{t('RESULT_NOT_FOUND')}</span></>}
                {isPaid && <><span className="stamp stamp-xl stamp-glo max-w-full whitespace-normal">{t('RESULT_SUCCESS_TITLE')}</span><p className="text-muted-foreground">{t('RESULT_SUCCESS_MESSAGE')}</p></>}
                {isPending && <><span className="stamp stamp-xl max-w-full whitespace-normal">{t('RESULT_PENDING_TITLE')}</span><p className="text-muted-foreground">{t('RESULT_PENDING_MESSAGE')}</p></>}
                {isCancelled && <><span className="stamp stamp-xl stamp-sale max-w-full whitespace-normal">{t('RESULT_FAILED_TITLE')}</span><p className="text-muted-foreground">{t('RESULT_FAILED_MESSAGE')}</p></>}
                {orderStatus && <p className="text-sm uppercase tracking-wide text-muted-foreground">{t('ORDER_NUMBER')}: <span className="font-bold tabular-nums text-foreground">#{orderStatus.orderId}</span></p>}
                <div className="flex w-full flex-col gap-3 sm:flex-row sm:justify-center">
                    {isPending && orderStatus?.redirectUrl && <a href={orderStatus.redirectUrl} className="glo h-12 px-6 text-base">{t('COMPLETE_PAYMENT')}</a>}
                    <Link prefetch={false} href="/" className={isPending && orderStatus?.redirectUrl ? 'strip strip-hover h-12 justify-center px-6 text-base [--tilt:0deg]' : 'glo h-12 px-6 text-base'}>
                        {t('BACK_TO_HOME')}
                    </Link>
                </div>
            </div>
        </PageShell>
    );
}
