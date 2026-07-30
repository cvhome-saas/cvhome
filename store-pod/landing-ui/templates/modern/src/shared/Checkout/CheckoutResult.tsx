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
            <Card className="w-full max-w-lg rounded-2xl border-2">
                <CardContent className="flex flex-col items-center text-center gap-6 p-10">
                    {isLoading && (
                        <>
                            <div className="animate-spin rounded-full h-12 w-12 border-b-4 border-primary"/>
                            <p className="text-muted-foreground font-medium tracking-[0.08em] uppercase text-sm">{t('RESULT_LOADING')}</p>
                        </>
                    )}

                    {!isLoading && !orderStatus && (
                        <>
                            <XCircle className="size-16 text-muted-foreground"/>
                            <p className="text-lg font-semibold tracking-tight text-foreground">{t('RESULT_NOT_FOUND')}</p>
                        </>
                    )}

                    {isPaid && (
                        <>
                            <div className="grid place-items-center size-16 rounded-full bg-primary text-primary-foreground">
                                <CheckCircle2 className="size-9"/>
                            </div>
                            <div>
                                <h1 className="text-3xl font-bold tracking-tight text-foreground">{t('RESULT_SUCCESS_TITLE')}</h1>
                                <p className="text-muted-foreground mt-2">{t('RESULT_SUCCESS_MESSAGE')}</p>
                            </div>
                        </>
                    )}

                    {isPending && (
                        <>
                            <div className="grid place-items-center size-16 rounded-full bg-amber-500 text-white">
                                <Clock className="size-9"/>
                            </div>
                            <div>
                                <h1 className="text-3xl font-bold tracking-tight text-foreground">{t('RESULT_PENDING_TITLE')}</h1>
                                <p className="text-muted-foreground mt-2">{t('RESULT_PENDING_MESSAGE')}</p>
                            </div>
                        </>
                    )}

                    {isCancelled && (
                        <>
                            <div className="grid place-items-center size-16 rounded-full bg-destructive text-white">
                                <XCircle className="size-9"/>
                            </div>
                            <div>
                                <h1 className="text-3xl font-bold tracking-tight text-foreground">{t('RESULT_FAILED_TITLE')}</h1>
                                <p className="text-muted-foreground mt-2">{t('RESULT_FAILED_MESSAGE')}</p>
                            </div>
                        </>
                    )}

                    {orderStatus && (
                        <span className="text-xs font-semibold tracking-[0.12em] uppercase px-4 py-2 rounded-full bg-accent text-accent-foreground">
                            {t('ORDER_NUMBER')} #{orderStatus.orderId}
                        </span>
                    )}

                    <div className="flex flex-col sm:flex-row gap-3 w-full mt-2">
                        {isPending && orderStatus?.redirectUrl && (
                            <Button asChild size="lg" className="w-full sm:flex-1 rounded-full font-semibold">
                                <a href={orderStatus.redirectUrl}>{t('COMPLETE_PAYMENT')}</a>
                            </Button>
                        )}
                        <Button asChild variant={isPending && orderStatus?.redirectUrl ? "outline" : "default"} size="lg"
                                className="w-full sm:flex-1 rounded-full font-semibold">
                            <Link prefetch={false} href="/">{t('BACK_TO_HOME')}</Link>
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    )
}
