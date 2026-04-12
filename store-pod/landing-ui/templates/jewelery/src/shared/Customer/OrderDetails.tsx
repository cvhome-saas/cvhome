'use client'

import { useTranslations } from "next-intl"
import { useCustomer } from "@store-front/hooks/use-customer"
import { useUser } from "@store-front/hooks/use-user"
import { StoreContext } from "@/types/store-context"
import { useEffect, useState } from "react"
import { Order, OrderHistoryList } from "@/types/order"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Breadcrumb } from "@/shared/Common/Breadcrumb"
import { BreadcrumbItem } from "@/types/bread-crumb"
import { isRtl } from "@/services/direction-utils"
import { cn } from "@/lib/utils"

export const OrderDetails = ({ storeContext, orderId }: { storeContext: StoreContext, orderId: number }) => {
    const t = useTranslations('PAGE.CUSTOMER')
    const { getOrderDetails, loading } = useCustomer(storeContext)
    const { user } = useUser(storeContext)
    const [orderData, setOrderData] = useState<{ order: Order|undefined, history: OrderHistoryList|undefined } | null>(null)
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

    const { order, history } = orderData

    const breadcrumbs = {
        prev: [
            { id: "1", name: t('HOME_TITLE'), href: '/' },
            { id: "2", name: t('ORDERS'), href: '/customer' },
        ],
        current: { id: "3", name: `${t('ORDER_ID')} #${order?.id}`, href: `/customer/order/${order?.id}` } as BreadcrumbItem
    }

    return (
        <div className={cn("max-w-7xl mx-auto py-10 px-4 sm:px-6 lg:px-8 bg-background", isRtlLayout ? "text-right" : "text-left")}>
            <Breadcrumb breadcrumbs={breadcrumbs} />
            
            <div className="flex flex-col md:flex-row items-center justify-between mb-12 mt-8 gap-4">
                <div>
                    <h1 className="text-3xl font-serif text-foreground tracking-wide">{t('ORDER_DETAILS')}</h1>
                    <p className="text-muted-foreground uppercase tracking-[0.2em] text-xs mt-2">{t('ORDER_ID')} #{order?.id}</p>
                </div>
                <Badge variant="outline" className="text-[10px] uppercase tracking-widest px-6 py-2 border-primary text-primary bg-primary/5 rounded-none">{t(order?.orderStatus??'UNKNOWN')}</Badge>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">
                <div className="lg:col-span-2 space-y-12">
                    <Card className="border-none shadow-none bg-secondary/5">
                        <CardHeader>
                            <CardTitle className={cn("font-serif tracking-wide", isRtlLayout ? "text-right" : "text-left")}>{t('ORDER_ITEMS')}</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <Table>
                                <TableHeader>
                                    <TableRow className="border-border">
                                        <TableHead className={cn("uppercase tracking-widest text-[10px]", isRtlLayout ? "text-right" : "text-left")}>{t('PRODUCT')}</TableHead>
                                        <TableHead className="text-center uppercase tracking-widest text-[10px]">{t('QTY')}</TableHead>
                                        <TableHead className={cn("uppercase tracking-widest text-[10px]", isRtlLayout ? "text-left" : "text-right")}>{t('PRICE')}</TableHead>
                                        <TableHead className={cn("uppercase tracking-widest text-[10px]", isRtlLayout ? "text-left" : "text-right")}>{t('TOTAL')}</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {order&&order.products && order.products.map((product) => (
                                        <TableRow key={product.id} className="border-border/50">
                                            <TableCell className={cn("font-medium", isRtlLayout ? "text-right" : "text-left")}>
                                                <p className="uppercase tracking-wider text-xs">{product.productName}</p>
                                            </TableCell>
                                            <TableCell className="text-center text-muted-foreground">{product.orderedQuantity}</TableCell>
                                            <TableCell className={cn("text-muted-foreground", isRtlLayout ? "text-left" : "text-right")}>{product.price}</TableCell>
                                            <TableCell className={cn("font-semibold", isRtlLayout ? "text-left" : "text-right")}>{product.subTotal}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                            <div className={cn("mt-12 space-y-3", isRtlLayout ? "text-left" : "text-right")}>
                                <div className={cn("flex justify-between max-w-xs", isRtlLayout ? "me-auto" : "ms-auto")}>
                                    <span className="text-muted-foreground uppercase tracking-widest text-[10px]">{t('SUB_TOTAL')}</span>
                                    <span className="text-sm font-medium">{order?.total?.value}</span>
                                </div>
                                <Separator className="bg-border/50" />
                                <div className={cn("flex justify-between max-w-xs", isRtlLayout ? "me-auto" : "ms-auto")}>
                                    <span className="font-serif tracking-wide text-lg uppercase">{t('TOTAL')}</span>
                                    <span className="font-serif text-2xl text-primary">{order?.total?.value}</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card className="border-none shadow-none bg-secondary/5">
                        <CardHeader>
                            <CardTitle className={cn("font-serif tracking-wide", isRtlLayout ? "text-right" : "text-left")}>{t('ORDER_HISTORY')}</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-8 pt-4">
                            {history&&history.map((h, i) => (
                                <div key={i} className={cn("flex gap-6 border-border pb-8 relative last:pb-0", isRtlLayout ? "border-e border-dashed pe-8" : "border-s border-dashed ps-8")}>
                                    <div className={cn("absolute top-0 size-3 border border-primary bg-background rotate-45", isRtlLayout ? "-end-[7px]" : "-start-[7px]")} />
                                    <div>
                                        <p className="font-semibold uppercase tracking-widest text-xs text-primary">{t(h.orderStatus??'UNKNOWN')}</p>
                                        <p className="text-[10px] text-muted-foreground mt-1 uppercase tracking-tighter">{new Date(h.date).toLocaleString()}</p>
                                        {h.comments && <p className="mt-3 text-sm italic font-serif text-muted-foreground">{h.comments}</p>}
                                    </div>
                                </div>
                            ))}
                        </CardContent>
                    </Card>
                </div>


                <div className="space-y-12">
                        <Card className="border-none shadow-none bg-secondary/5">
                            <CardHeader>
                                <CardTitle className={cn("font-serif tracking-wide text-sm", isRtlLayout ? "text-right" : "text-left")}>{t('SHIPPING_ADDRESS')}</CardTitle>
                            </CardHeader>
                            {order &&<CardContent className="space-y-2 text-sm">
                                <p className="font-semibold uppercase tracking-wider">{order.delivery.firstName} {order.delivery.lastName}</p>
                                <p className="text-muted-foreground">{order.delivery.address}</p>
                                <p className="text-muted-foreground">{order.delivery.city}  {order.delivery.postalCode}</p>
                                <p className="text-muted-foreground uppercase tracking-widest">{order.delivery.country}</p>
                                <p className="text-muted-foreground pt-2 italic">{order.delivery.phone}</p>
                            </CardContent>
                            }
                        </Card>

                        <Card className="border-none shadow-none bg-secondary/5">
                            <CardHeader>
                                <CardTitle className={cn("font-serif tracking-wide text-sm", isRtlLayout ? "text-right" : "text-left")}>{t('BILLING_ADDRESS')}</CardTitle>
                            </CardHeader>
                            {order &&<CardContent className="space-y-2 text-sm">
                                <p className="font-semibold uppercase tracking-wider">{order.billing.firstName} {order.billing.lastName}</p>
                                <p className="text-muted-foreground">{order.billing.address}</p>
                                <p className="text-muted-foreground">{order.billing.city}, {order.billing.postalCode}</p>
                                <p className="text-muted-foreground uppercase tracking-widest">{order.billing.country}</p>
                                <p className="text-muted-foreground pt-2 italic">{order.billing.phone}</p>
                            </CardContent>
                            }
                        </Card>
                    </div>

            </div>
        </div>
    )
}
