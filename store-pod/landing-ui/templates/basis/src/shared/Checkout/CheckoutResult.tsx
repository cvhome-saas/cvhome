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
        <div className={cn("flex-grow bg-background flex items-center justify-center px-4 py-20", isRtlLayout ? "text-right" : "text-left")}>
            <Card className="w-full max-w-lg">
                <CardContent className="flex flex-col items-center text-center gap-6 p-10">
                    {isLoading && (
                        <>
                            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"/>
                            <p className="text-muted-foreground">{t('RESULT_LOADING')}</p>
                        </>
                    )}

                    {!isLoading && !orderStatus && (
                        <>
                            <XCircle className="size-16 text-muted-foreground"/>
                            <p className="text-lg font-medium text-foreground">{t('RESULT_NOT_FOUND')}</p>
                        </>
                    )}

                    {isPaid && (
                        <>
                            <CheckCircle2 className="size-16 text-primary"/>
                            <div>
                                <h1 className="text-2xl font-bold text-foreground">{t('RESULT_SUCCESS_TITLE')}</h1>
                                <p className="text-muted-foreground mt-2">{t('RESULT_SUCCESS_MESSAGE')}</p>
                            </div>
                        </>
                    )}

                    {isPending && (
                        <>
                            <Clock className="size-16 text-amber-500"/>
                            <div>
                                <h1 className="text-2xl font-bold text-foreground">{t('RESULT_PENDING_TITLE')}</h1>
                                <p className="text-muted-foreground mt-2">{t('RESULT_PENDING_MESSAGE')}</p>
                            </div>
                        </>
                    )}

                    {isCancelled && (
                        <>
                            <XCircle className="size-16 text-destructive"/>
                            <div>
                                <h1 className="text-2xl font-bold text-foreground">{t('RESULT_FAILED_TITLE')}</h1>
                                <p className="text-muted-foreground mt-2">{t('RESULT_FAILED_MESSAGE')}</p>
                            </div>
                        </>
                    )}

                    {orderStatus && (
                        <p className="text-sm text-muted-foreground">
                            {t('ORDER_NUMBER')}: <span className="font-semibold text-foreground">#{orderStatus.orderId}</span>
                        </p>
                    )}

                    <div className="flex flex-col sm:flex-row gap-3 w-full mt-2">
                        {isPending && orderStatus?.redirectUrl && (
                            <Button asChild size="lg" className="w-full sm:flex-1">
                                <a href={orderStatus.redirectUrl}>{t('COMPLETE_PAYMENT')}</a>
                            </Button>
                        )}
                        <Button asChild variant={isPending && orderStatus?.redirectUrl ? "outline" : "default"} size="lg" className="w-full sm:flex-1">
                            <Link prefetch={false} href="/">{t('BACK_TO_HOME')}</Link>
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    )
}
