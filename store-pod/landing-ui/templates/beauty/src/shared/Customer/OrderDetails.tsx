'use client'

import {useTranslations} from "next-intl"
import {useCustomer} from "@store-front/hooks/use-customer"
import {useUser} from "@store-front/hooks/use-user"
import {StoreContext} from "@/types/store-context"
import {useEffect, useState} from "react"
import {Order, OrderHistoryList} from "@/types/order"
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card"
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table"
import {Badge} from "@/components/ui/badge"
import {Separator} from "@/components/ui/separator"
import {Breadcrumb} from "@/shared/Common/Breadcrumb"
import {BreadcrumbItem} from "@/types/bread-crumb"
import {isRtl} from "@/services/direction-utils"
import {cn} from "@/lib/utils"

export const OrderDetails = ({storeContext, orderId}: { storeContext: StoreContext, orderId: number }) => {
    const t = useTranslations('PAGE.CUSTOMER')
    const {getOrderDetails, loading} = useCustomer(storeContext)
    const {user} = useUser(storeContext)
    const [orderData, setOrderData] = useState<{
        order: Order | undefined,
        history: OrderHistoryList | undefined
    } | null>(null)
    const isRtlLayout = isRtl(storeContext.locale)

    useEffect(() => {
        if (user && orderId) {
            getOrderDetails(orderId).then(data => data && setOrderData(data))
        }
    }, [user, orderId, getOrderDetails])

    if (loading && !orderData) {
        return <div className="flex justify-center p-20">{t('LOADING')}</div>
    }

    if (!orderData) return null

    const {order, history} = orderData

    const breadcrumbs = {
        prev: [
            {id: "1", name: t('HOME_TITLE'), href: '/'},
            {id: "2", name: t('ORDERS'), href: '/customer'},
        ],
        current: {
            id: "3",
            name: `${t('ORDER_ID')} #${order?.id}`,
            href: `/customer/order/${order?.id}`
        } as BreadcrumbItem
    }

    return (
        <div
            className={cn("max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8 bg-background", isRtlLayout ? "text-right" : "text-left")}>
            <Breadcrumb breadcrumbs={breadcrumbs}/>

            <div className="flex flex-col sm:flex-row items-center justify-between mb-12 mt-10 gap-6">
                <div>
                    <h1 className="text-4xl font-bold text-foreground tracking-tight">{t('ORDER_DETAILS')}</h1>
                    <p className="text-primary font-bold mt-2 text-lg">#{order?.id}</p>
                </div>
                <Badge
                    className="text-xs uppercase tracking-widest px-8 py-3 bg-primary text-white border-none rounded-full shadow-lg shadow-primary/20">
                    {t(order?.orderStatus ?? 'UNKNOWN')}
                </Badge>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">
                <div className="lg:col-span-2 space-y-12">
                    <Card className="border-2 border-primary/10 shadow-none rounded-[2rem] overflow-hidden">
                        <CardHeader className="bg-primary/5">
                            <CardTitle
                                className={cn("text-xl", isRtlLayout ? "text-right" : "text-left")}>{t('ORDER_ITEMS')}</CardTitle>
                        </CardHeader>
                        <CardContent className="p-0">
                            <Table>
                                <TableHeader className="bg-muted/20">
                                    <TableRow className="border-none">
                                        <TableHead
                                            className={cn("py-6 px-8", isRtlLayout ? "text-right" : "text-left")}>{t('PRODUCT')}</TableHead>
                                        <TableHead className="text-center py-6">{t('QTY')}</TableHead>
                                        <TableHead
                                            className={cn("py-6", isRtlLayout ? "text-left" : "text-right")}>{t('PRICE')}</TableHead>
                                        <TableHead
                                            className={cn("py-6 px-8", isRtlLayout ? "text-left" : "text-right")}>{t('TOTAL')}</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {order && order.products && order.products.map((product) => (
                                        <TableRow key={product.id} className="border-primary/5">
                                            <TableCell
                                                className={cn("font-bold py-6 px-8", isRtlLayout ? "text-right" : "text-left")}>
                                                <span className="text-foreground/80">{product.productName}</span>
                                            </TableCell>
                                            <TableCell
                                                className="text-center font-medium text-muted-foreground">{product.orderedQuantity}</TableCell>
                                            <TableCell
                                                className={cn("font-medium", isRtlLayout ? "text-left" : "text-right")}>{product.price}</TableCell>
                                            <TableCell
                                                className={cn("font-bold text-primary px-8", isRtlLayout ? "text-left" : "text-right")}>{product.subTotal}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                            <div className={cn("p-8 bg-primary/5 space-y-4", isRtlLayout ? "text-left" : "text-right")}>
                                <div
                                    className={cn("flex justify-between max-w-xs", isRtlLayout ? "me-auto" : "ms-auto")}>
                                    <span className="text-muted-foreground font-semibold">{t('SUB_TOTAL')}</span>
                                    <span className="font-bold">{order?.total?.value}</span>
                                </div>
                                <Separator className="bg-primary/10"/>
                                <div
                                    className={cn("flex justify-between max-w-xs text-2xl font-black text-primary", isRtlLayout ? "me-auto" : "ms-auto")}>
                                    <span>{t('TOTAL')}</span>
                                    <span>{order?.total?.value}</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card className="border-2 border-primary/10 shadow-none rounded-[2rem] overflow-hidden">
                        <CardHeader className="bg-primary/5">
                            <CardTitle
                                className={cn("text-xl", isRtlLayout ? "text-right" : "text-left")}>{t('ORDER_HISTORY')}</CardTitle>
                        </CardHeader>
                        <CardContent className="p-8 space-y-10">
                            {history && history.map((h, i) => (
                                <div key={i}
                                     className={cn("flex gap-8 pb-10 relative last:pb-0", isRtlLayout ? "border-e-2 border-primary/10 pe-10" : "border-s-2 border-primary/10 ps-10")}>
                                    <div
                                        className={cn("absolute top-0 size-6 rounded-full bg-background border-4 border-primary shadow-sm shadow-primary/20", isRtlLayout ? "-end-[13px]" : "-start-[13px]")}/>
                                    <div className="flex-grow">
                                        <div
                                            className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
                                            <p className="font-black text-lg text-foreground uppercase tracking-tight">{t(h.orderStatus ?? 'UNKNOWN')}</p>
                                            <p className="text-xs font-bold text-primary/60 bg-primary/5 px-3 py-1 rounded-full">{new Date(h.date).toLocaleString()}</p>
                                        </div>
                                        {h.comments &&
                                            <p className="text-sm text-muted-foreground leading-relaxed bg-muted/30 p-4 rounded-2xl italic">{h.comments}</p>}
                                    </div>
                                </div>
                            ))}
                        </CardContent>
                    </Card>
                </div>


                <div className="space-y-8">
                    <Card className="border-2 border-primary/10 shadow-none rounded-[2rem]">
                        <CardHeader className="pb-4">
                            <CardTitle
                                className={cn("text-lg font-bold uppercase tracking-widest text-primary/60", isRtlLayout ? "text-right" : "text-left")}>{t('SHIPPING_ADDRESS')}</CardTitle>
                        </CardHeader>
                        {order && <CardContent className="space-y-2">
                            <p className="font-black text-lg">{order.delivery.firstName} {order.delivery.lastName}</p>
                            <p className="text-foreground/70">{order.delivery.address}</p>
                            <p className="text-foreground/70">{order.delivery.city} {order.delivery.postalCode}</p>
                            <p className="text-foreground/70 font-bold">{order.delivery.country}</p>
                            <div className="mt-4 pt-4 border-t border-primary/5">
                                <p className="text-primary font-bold">{order.delivery.phone}</p>
                            </div>
                        </CardContent>
                        }
                    </Card>

                    <Card className="border-2 border-primary/10 shadow-none rounded-[2rem]">
                        <CardHeader className="pb-4">
                            <CardTitle
                                className={cn("text-lg font-bold uppercase tracking-widest text-primary/60", isRtlLayout ? "text-right" : "text-left")}>{t('BILLING_ADDRESS')}</CardTitle>
                        </CardHeader>
                        {order && <CardContent className="space-y-2">
                            <p className="font-black text-lg">{order.billing.firstName} {order.billing.lastName}</p>
                            <p className="text-foreground/70">{order.billing.address}</p>
                            <p className="text-foreground/70">{order.billing.city}, {order.billing.postalCode}</p>
                            <p className="text-foreground/70 font-bold">{order.billing.country}</p>
                            <div className="mt-4 pt-4 border-t border-primary/5">
                                <p className="text-primary font-bold">{order.billing.phone}</p>
                            </div>
                        </CardContent>
                        }
                    </Card>
                </div>

            </div>
        </div>
    )
}
