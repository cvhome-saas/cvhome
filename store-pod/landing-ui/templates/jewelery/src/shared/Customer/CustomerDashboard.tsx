'use client'

import {useTranslations} from "next-intl"
import {useCustomer} from "@store-front/hooks/use-customer"
import {useUser} from "@store-front/hooks/use-user"
import {StoreContext} from "@/types/store-context"
import {useEffect, useState} from "react"
import {Customer} from "@/types/customer"
import {OrderPage} from "@/types/order"
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs"
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card"
import {Label} from "@/components/ui/label"
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table"
import {Button} from "@/components/ui/button"
import {Link} from "@/i18n/navigation"
import {isRtl} from "@/services/direction-utils"
import {cn} from "@/lib/utils"

export const CustomerDashboard = ({storeContext}: { storeContext: StoreContext }) => {
    const t = useTranslations('PAGE.CUSTOMER')
    const {getCustomerInfo, listOrders, loading} = useCustomer(storeContext)
    const {user} = useUser(storeContext)
    const [customer, setCustomer] = useState<Customer | null>(null)
    const [orders, setOrders] = useState<OrderPage | null>(null)
    const isRtlLayout = isRtl(storeContext.locale)

    useEffect(() => {
        if (user) {
            getCustomerInfo().then(data => data && setCustomer(data))
            listOrders(0, 10).then(data => data && setOrders(data))
        }
    }, [user, getCustomerInfo, listOrders])

    if (loading && !customer) {
        return <div className="flex justify-center p-20">{t('LOADING')}</div>
    }

    return (
        <div className={cn("max-w-7xl mx-auto py-10 px-4 sm:px-6 lg:px-8", isRtlLayout ? "text-right" : "text-left")}>
            <h1 className="text-3xl font-serif mb-8 text-foreground tracking-wide">{t('TITLE')}</h1>

            <Tabs defaultValue="profile" className="w-full" dir={isRtlLayout ? "rtl" : "ltr"}>
                <TabsList className="grid w-full grid-cols-3 mb-8 bg-secondary/20">
                    <TabsTrigger value="profile"
                                 className="uppercase tracking-widest text-xs">{t('PROFILE')}</TabsTrigger>
                    <TabsTrigger value="addresses"
                                 className="uppercase tracking-widest text-xs">{t('ADDRESSES')}</TabsTrigger>
                    <TabsTrigger value="orders"
                                 className="uppercase tracking-widest text-xs">{t('ORDERS')}</TabsTrigger>
                </TabsList>

                <TabsContent value="profile">
                    <Card className="border-none shadow-none bg-secondary/5">
                        <CardHeader>
                            <CardTitle
                                className={cn("font-serif tracking-wide", isRtlLayout ? "text-right" : "text-left")}>{t('PERSONAL_INFO')}</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-6">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                <div className="space-y-1">
                                    <Label
                                        className="text-[10px] uppercase tracking-widest text-muted-foreground">{t('FIRST_NAME')}</Label>
                                    <p className="font-medium border-b border-border pb-2">{customer?.firstNames}</p>
                                </div>
                                <div className="space-y-1">
                                    <Label
                                        className="text-[10px] uppercase tracking-widest text-muted-foreground">{t('LAST_NAME')}</Label>
                                    <p className="font-medium border-b border-border pb-2">{customer?.lastName}</p>
                                </div>
                                <div className="space-y-1">
                                    <Label
                                        className="text-[10px] uppercase tracking-widest text-muted-foreground">{t('EMAIL')}</Label>
                                    <p className="font-medium border-b border-border pb-2">{customer?.emailAddress}</p>
                                </div>
                                <div className="space-y-1">
                                    <Label
                                        className="text-[10px] uppercase tracking-widest text-muted-foreground">{t('PHONE')}</Label>
                                    <p className="font-medium border-b border-border pb-2">{customer?.billing?.phone}</p>
                                </div>
                                <div className="space-y-1">
                                    <Label
                                        className="text-[10px] uppercase tracking-widest text-muted-foreground">{t('USERNAME')}</Label>
                                    <p className="font-medium border-b border-border pb-2">{customer?.username}</p>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </TabsContent>

                <TabsContent value="addresses" className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        <Card className="border-none shadow-none bg-secondary/5">
                            <CardHeader>
                                <CardTitle
                                    className={cn("font-serif tracking-wide", isRtlLayout ? "text-right" : "text-left")}>{t('BILLING_ADDRESS')}</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-2 text-sm">
                                <p className="font-semibold uppercase tracking-wider">{customer?.billing?.firstName} {customer?.billing?.lastName}</p>
                                <p className="text-muted-foreground">{customer?.billing?.address}</p>
                                <p className="text-muted-foreground">{customer?.billing?.city} {customer?.billing?.postalCode}</p>
                                <p className="text-muted-foreground uppercase tracking-widest">{customer?.billing?.country}</p>
                                <p className="text-muted-foreground pt-2 italic">{customer?.billing?.phone}</p>
                            </CardContent>
                        </Card>
                        <Card className="border-none shadow-none bg-secondary/5">
                            <CardHeader>
                                <CardTitle
                                    className={cn("font-serif tracking-wide", isRtlLayout ? "text-right" : "text-left")}>{t('SHIPPING_ADDRESS')}</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-2 text-sm">
                                <p className="font-semibold uppercase tracking-wider">{customer?.delivery?.firstName} {customer?.delivery?.lastName}</p>
                                <p className="text-muted-foreground">{customer?.delivery?.address}</p>
                                <p className="text-muted-foreground">{customer?.delivery?.city} {customer?.delivery?.postalCode}</p>
                                <p className="text-muted-foreground uppercase tracking-widest">{customer?.delivery?.country}</p>
                                <p className="text-muted-foreground pt-2 italic">{customer?.delivery?.phone}</p>
                            </CardContent>
                        </Card>
                    </div>
                </TabsContent>

                <TabsContent value="orders">
                    <Card className="border-none shadow-none bg-secondary/5">
                        <CardHeader>
                            <CardTitle
                                className={cn("font-serif tracking-wide", isRtlLayout ? "text-right" : "text-left")}>{t('ORDERS')}</CardTitle>
                        </CardHeader>
                        <CardContent>
                            {!orders || !orders.content || orders.content.length === 0 ? (
                                <p className="text-center py-12 text-muted-foreground font-serif italic">{t('NO_ORDERS')}</p>
                            ) : (
                                <div className="overflow-x-auto">
                                    <Table>
                                        <TableHeader>
                                            <TableRow className="border-border">
                                                <TableHead
                                                    className={cn("uppercase tracking-widest text-[10px]", isRtlLayout ? "text-right" : "text-left")}>{t('ORDER_ID')}</TableHead>
                                                <TableHead
                                                    className={cn("uppercase tracking-widest text-[10px]", isRtlLayout ? "text-right" : "text-left")}>{t('DATE')}</TableHead>
                                                <TableHead
                                                    className={cn("uppercase tracking-widest text-[10px]", isRtlLayout ? "text-right" : "text-left")}>{t('TOTAL')}</TableHead>
                                                <TableHead
                                                    className={cn("uppercase tracking-widest text-[10px]", isRtlLayout ? "text-right" : "text-left")}>{t('STATUS')}</TableHead>
                                                <TableHead
                                                    className={cn("uppercase tracking-widest text-[10px]", isRtlLayout ? "text-left" : "text-right")}>{t('ACTIONS')}</TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {orders.content.map((order) => (
                                                <TableRow key={order.id} className="border-border/50">
                                                    <TableCell
                                                        className="font-medium text-primary">#{order.id}</TableCell>
                                                    <TableCell
                                                        className="text-muted-foreground">{new Date(order.datePurchased).toLocaleDateString()}</TableCell>
                                                    <TableCell
                                                        className="font-semibold">{order?.total?.value}</TableCell>
                                                    <TableCell>
                                                        <span
                                                            className="inline-block px-2 py-1 bg-primary/10 text-primary text-[10px] uppercase tracking-widest rounded-full font-bold">
                                                            {t(order?.orderStatus ?? 'UNKNOWN')}
                                                        </span>
                                                    </TableCell>
                                                    <TableCell className={cn(isRtlLayout ? "text-left" : "text-right")}>
                                                        <Button variant="outline" asChild size="sm"
                                                                className="rounded-none border-primary/20 hover:border-primary hover:bg-primary/5 uppercase tracking-widest text-[10px]">
                                                            <Link href={`/customer/order/${order.id}`}>
                                                                {t('VIEW_ORDER')}
                                                            </Link>
                                                        </Button>
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </TabsContent>
            </Tabs>
        </div>
    )
}
