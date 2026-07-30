'use client'

import {useEffect} from "react"
import {useTranslations} from "next-intl"
import {useSearchParams} from "next/navigation"
import {useOrderStatus} from "@store-front/hooks/use-order-status"
import {useUser} from "@store-front/hooks/use-user"
import {StoreContext} from "@/types/store-context"
import {Card, CardContent} from "@/components/ui/card"
import {Button} from "@/components/ui/button"
import {Link} from "@/i18n/navigation"
import {CheckCircle2, Clock, XCircle} from "lucide-react"
import {isRtl} from "@/services/direction-utils"
import {cn} from "@/lib/utils"

export const CheckoutResult = ({storeContext, requireLoginForOrderPlacement}: {
    storeContext: StoreContext,
    requireLoginForOrderPlacement: boolean
}) => {
    const t = useTranslations('PAGE.CHECKOUT')
    const searchParams = useSearchParams()
    const orderId = searchParams.get('orderId') ? Number(searchParams.get('orderId')) : undefined
    const {user, loading: userLoading, login} = useUser(storeContext)
    const isRtlLayout = isRtl(storeContext.locale)

    const awaitingLogin = requireLoginForOrderPlacement && (userLoading || !user)

    useEffect(() => {
        if (requireLoginForOrderPlacement && !userLoading && !user) {
            login()
        }
    }, [requireLoginForOrderPlacement, userLoading, user, login])

    const {orderStatus, loading} = useOrderStatus(storeContext, awaitingLogin ? undefined : orderId)
    const isLoading = awaitingLogin || (loading && !orderStatus)

    const isPaid = orderStatus?.orderStatus === 'CONFIRMED' || orderStatus?.paymentStatus === 'PAID'
    const isCancelled = orderStatus?.orderStatus === 'CANCELLED' || orderStatus?.paymentStatus === 'FAILED'
    const isPending = !!orderStatus && !isPaid && !isCancelled

    return (
        <div className={cn("flex-grow bg-background flex items-center justify-center px-4 py-24", isRtlLayout ? "text-right" : "text-left")}>
            <Card className="w-full max-w-lg rounded-none border-primary/20 shadow-none bg-secondary/5">
                <CardContent className="flex flex-col items-center text-center gap-8 p-12">
                    {isLoading && (
                        <>
                            <div className="animate-spin rounded-full h-12 w-12 border-b border-primary"/>
                            <p className="text-muted-foreground uppercase tracking-widest text-xs">{t('RESULT_LOADING')}</p>
                        </>
                    )}

                    {!isLoading && !orderStatus && (
                        <>
                            <XCircle className="size-14 text-muted-foreground" strokeWidth={1}/>
                            <p className="text-muted-foreground uppercase tracking-widest text-xs">{t('RESULT_NOT_FOUND')}</p>
                        </>
                    )}

                    {isPaid && (
                        <>
                            <CheckCircle2 className="size-14 text-primary" strokeWidth={1}/>
                            <div>
                                <h1 className="text-2xl font-serif tracking-wide text-foreground">{t('RESULT_SUCCESS_TITLE')}</h1>
                                <p className="text-muted-foreground mt-3 text-sm">{t('RESULT_SUCCESS_MESSAGE')}</p>
                            </div>
                        </>
                    )}

                    {isPending && (
                        <>
                            <Clock className="size-14 text-amber-600" strokeWidth={1}/>
                            <div>
                                <h1 className="text-2xl font-serif tracking-wide text-foreground">{t('RESULT_PENDING_TITLE')}</h1>
                                <p className="text-muted-foreground mt-3 text-sm">{t('RESULT_PENDING_MESSAGE')}</p>
                            </div>
                        </>
                    )}

                    {isCancelled && (
                        <>
                            <XCircle className="size-14 text-destructive" strokeWidth={1}/>
                            <div>
                                <h1 className="text-2xl font-serif tracking-wide text-foreground">{t('RESULT_FAILED_TITLE')}</h1>
                                <p className="text-muted-foreground mt-3 text-sm">{t('RESULT_FAILED_MESSAGE')}</p>
                            </div>
                        </>
                    )}

                    {orderStatus && (
                        <p className="text-[11px] uppercase tracking-[0.2em] text-muted-foreground">
                            {t('ORDER_NUMBER')} #{orderStatus.orderId}
                        </p>
                    )}

                    <div className="flex flex-col sm:flex-row gap-3 w-full mt-2">
                        {isPending && orderStatus?.redirectUrl && (
                            <Button asChild size="lg" className="w-full sm:flex-1 rounded-none uppercase tracking-widest text-xs">
                                <a href={orderStatus.redirectUrl}>{t('COMPLETE_PAYMENT')}</a>
                            </Button>
                        )}
                        <Button asChild variant={isPending && orderStatus?.redirectUrl ? "outline" : "default"} size="lg"
                                className="w-full sm:flex-1 rounded-none uppercase tracking-widest text-xs">
                            <Link prefetch={false} href="/">{t('BACK_TO_HOME')}</Link>
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    )
}
