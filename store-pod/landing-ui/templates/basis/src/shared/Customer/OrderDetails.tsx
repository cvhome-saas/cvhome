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

export const OrderDetails = ({ storeContext, orderId }: { storeContext: StoreContext, orderId: number }) => {
    const t = useTranslations('PAGE.CUSTOMER')
    const { getOrderDetails, loading } = useCustomer(storeContext)
    const { user } = useUser(storeContext)
    const [orderData, setOrderData] = useState<{ order: Order|undefined, history: OrderHistoryList|undefined } | null>(null)

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
        <div className="max-w-7xl mx-auto py-10 px-4 sm:px-6 lg:px-8 bg-background">
            <Breadcrumb breadcrumbs={breadcrumbs} />
            
            <div className="flex items-center justify-between mb-8 mt-6">
                <h1 className="text-3xl font-bold text-foreground">{t('ORDER_DETAILS')}</h1>
                <Badge variant="outline" className="text-lg px-4 py-1">{t(order?.orderStatus??'UNKNOWN')}</Badge>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 space-y-8">
                    <Card>
                        <CardHeader>
                            <CardTitle>{t('ORDER_ITEMS')}</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>{t('PRODUCT')}</TableHead>
                                        <TableHead className="text-center">{t('QTY')}</TableHead>
                                        <TableHead className="text-right">{t('PRICE')}</TableHead>
                                        <TableHead className="text-right">{t('TOTAL')}</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {order&&order.products && order.products.map((product) => (
                                        <TableRow key={product.id}>
                                            <TableCell className="font-medium">{product.productName}</TableCell>
                                            <TableCell className="text-center">{product.orderedQuantity}</TableCell>
                                            <TableCell className="text-right">{product.price}</TableCell>
                                            <TableCell className="text-right font-semibold">{product.subTotal}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                            <div className="mt-8 space-y-2 text-right">
                                <div className="flex justify-between max-w-xs ms-auto">
                                    <span className="text-muted-foreground">{t('SUB_TOTAL')}</span>
                                    <span>{order?.total?.value}</span>
                                </div>
                                <Separator />
                                <div className="flex justify-between max-w-xs ms-auto text-xl font-bold">
                                    <span>{t('TOTAL')}</span>
                                    <span>{order?.total?.value}</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle>{t('ORDER_HISTORY')}</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {history&&history.map((h, i) => (
                                <div key={i} className="flex gap-4 border-s-2 border-border ps-6 pb-6 relative last:pb-0">
                                    <div className="absolute -start-[9px] top-0 size-4 rounded-full bg-primary" />
                                    <div>
                                        <p className="font-semibold text-lg">{t(h.orderStatus??'UNKNOWN')}</p>
                                        <p className="text-sm text-muted-foreground">{new Date(h.date).toLocaleString()}</p>
                                        {h.comments && <p className="mt-2 text-sm">{h.comments}</p>}
                                    </div>
                                </div>
                            ))}
                        </CardContent>
                    </Card>
                </div>


                <div className="space-y-8">
                        <Card>
                            <CardHeader>
                                <CardTitle>{t('SHIPPING_ADDRESS')}</CardTitle>
                            </CardHeader>
                            {order &&<CardContent className="space-y-2">
                                <p className="font-medium">{order.delivery.firstName} {order.delivery.lastName}</p>
                                <p>{order.delivery.address}</p>
                                <p>{order.delivery.city}  {order.delivery.postalCode}</p>
                                <p>{order.delivery.country}</p>
                                <p>{order.delivery.phone}</p>
                            </CardContent>
                            }
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>{t('BILLING_ADDRESS')}</CardTitle>
                            </CardHeader>
                            {order &&<CardContent className="space-y-2">
                                <p className="font-medium">{order.billing.firstName} {order.billing.lastName}</p>
                                <p>{order.billing.address}</p>
                                <p>{order.billing.city}, {order.billing.postalCode}</p>
                                <p>{order.billing.country}</p>
                                <p>{order.billing.phone}</p>
                            </CardContent>
                            }
                        </Card>
                    </div>

            </div>
        </div>
    )
}
