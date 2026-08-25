'use client'
import {useEffect} from 'react';
import {useTranslations} from 'next-intl';
import {useSearchParams} from 'next/navigation';
import {CheckCircle2Icon, ClockIcon, XCircleIcon} from 'lucide-react';
import type {CheckoutResultData, PageProps} from '@store-front/theme';
import {Link} from '@store-front/i18n/navigation';
import {useOrderStatus} from '@store-front/hooks/use-order-status';
import {useUser} from '@store-front/hooks/use-user';
import {Button} from '@store-front/ui/button';
import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../components/PageShell';

/** Verifies the real order status through the API — never trusts the success/cancel URL alone. Rendered as a printed receipt slip. */
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
    const title = 'display text-3xl sm:text-4xl';

    return (
        <PageShell width="narrow" className="py-section">
            <div className="flex flex-col items-center gap-6 border p-8 text-center sm:p-10" aria-live="polite">
                {isLoading && <><Skeleton className="size-14 rounded-full"/><p className="text-muted-foreground">{t('RESULT_LOADING')}</p></>}
                {!isLoading && !orderStatus && <><XCircleIcon className="size-14 text-muted-foreground"/><p className={title}>{t('RESULT_NOT_FOUND')}</p></>}
                {isPaid && <><CheckCircle2Icon className="size-14 text-success"/><div><h1 className={title}>{t('RESULT_SUCCESS_TITLE')}</h1><p className="mt-2 text-muted-foreground">{t('RESULT_SUCCESS_MESSAGE')}</p></div></>}
                {isPending && <><ClockIcon className="size-14 text-warning"/><div><h1 className={title}>{t('RESULT_PENDING_TITLE')}</h1><p className="mt-2 text-muted-foreground">{t('RESULT_PENDING_MESSAGE')}</p></div></>}
                {isCancelled && <><XCircleIcon className="size-14 text-destructive"/><div><h1 className={title}>{t('RESULT_FAILED_TITLE')}</h1><p className="mt-2 text-muted-foreground">{t('RESULT_FAILED_MESSAGE')}</p></div></>}
                {orderStatus && (
                    <p className="flex items-baseline gap-2 border-t pt-4 text-sm text-muted-foreground">
                        {t('ORDER_NUMBER')} <span className="price text-xl text-foreground">#{orderStatus.orderId}</span>
                    </p>
                )}
                <div className="flex w-full flex-col gap-3 sm:flex-row">
                    {isPending && orderStatus?.redirectUrl && <Button asChild size="lg" className="h-12 flex-1 text-base font-semibold"><a href={orderStatus.redirectUrl}>{t('COMPLETE_PAYMENT')}</a></Button>}
                    <Button asChild size="lg" variant={isPending && orderStatus?.redirectUrl ? 'outline' : 'default'} className="h-12 flex-1 text-base font-semibold">
                        <Link prefetch={false} href="/">{t('BACK_TO_HOME')}</Link>
                    </Button>
                </div>
            </div>
        </PageShell>
    );
}
