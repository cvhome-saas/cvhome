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
        <div className={cn("max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8", isRtlLayout ? "text-right" : "text-left")}>
            <h1 className="text-4xl font-bold mb-10 text-foreground tracking-tight">{t('TITLE')}</h1>

            <Tabs defaultValue="profile" className="w-full" dir={isRtlLayout ? "rtl" : "ltr"}>
                <TabsList className="grid w-full grid-cols-3 mb-12 bg-primary/5 p-1 rounded-2xl">
                    <TabsTrigger value="profile"
                                 className="rounded-xl py-3 data-[state=active]:bg-background data-[state=active]:shadow-sm transition-all">{t('PROFILE')}</TabsTrigger>
                    <TabsTrigger value="addresses"
                                 className="rounded-xl py-3 data-[state=active]:bg-background data-[state=active]:shadow-sm transition-all">{t('ADDRESSES')}</TabsTrigger>
                    <TabsTrigger value="orders"
                                 className="rounded-xl py-3 data-[state=active]:bg-background data-[state=active]:shadow-sm transition-all">{t('ORDERS')}</TabsTrigger>
                </TabsList>

                <TabsContent value="profile" className="space-y-6">
                    <Card className="border-2 border-primary/10 shadow-none rounded-[2rem] overflow-hidden">
                        <CardHeader className="bg-primary/5 pb-8">
                            <CardTitle
                                className={cn("text-2xl", isRtlLayout ? "text-right" : "text-left")}>{t('PERSONAL_INFO')}</CardTitle>
                        </CardHeader>
                        <CardContent className="pt-8 space-y-8">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-8">
                                <div className="space-y-2">
                                    <Label
                                        className="text-xs font-bold uppercase tracking-widest text-primary/60">{t('FIRST_NAME')}</Label>
                                    <p className="text-lg font-medium text-foreground/80">{customer?.firstNames}</p>
                                </div>
                                <div className="space-y-2">
                                    <Label
                                        className="text-xs font-bold uppercase tracking-widest text-primary/60">{t('LAST_NAME')}</Label>
                                    <p className="text-lg font-medium text-foreground/80">{customer?.lastName}</p>
                                </div>
                                <div className="space-y-2">
                                    <Label
                                        className="text-xs font-bold uppercase tracking-widest text-primary/60">{t('EMAIL')}</Label>
                                    <p className="text-lg font-medium text-foreground/80">{customer?.emailAddress}</p>
                                </div>
                                <div className="space-y-2">
                                    <Label
                                        className="text-xs font-bold uppercase tracking-widest text-primary/60">{t('PHONE')}</Label>
                                    <p className="text-lg font-medium text-foreground/80">{customer?.billing?.phone}</p>
                                </div>
                                <div className="space-y-2 md:col-span-2">
                                    <Label
                                        className="text-xs font-bold uppercase tracking-widest text-primary/60">{t('USERNAME')}</Label>
                                    <p className="text-lg font-medium text-foreground/80">{customer?.username}</p>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </TabsContent>

                <TabsContent value="addresses" className="space-y-8">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        <Card className="border-2 border-primary/10 shadow-none rounded-[2rem]">
                            <CardHeader>
                                <CardTitle
                                    className={cn("text-xl", isRtlLayout ? "text-right" : "text-left")}>{t('BILLING_ADDRESS')}</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-3">
                                <p className="font-bold text-primary">{customer?.billing?.firstName} {customer?.billing?.lastName}</p>
                                <p className="text-foreground/70">{customer?.billing?.address}</p>
                                <p className="text-foreground/70">{customer?.billing?.city}, {customer?.billing?.postalCode}</p>
                                <p className="text-foreground/70 font-semibold">{customer?.billing?.country}</p>
                                <div className="pt-4 border-t border-primary/5 mt-4">
                                    <p className="text-sm text-muted-foreground">{customer?.billing?.phone}</p>
                                </div>
                            </CardContent>
                        </Card>
                        <Card className="border-2 border-primary/10 shadow-none rounded-[2rem]">
                            <CardHeader>
                                <CardTitle
                                    className={cn("text-xl", isRtlLayout ? "text-right" : "text-left")}>{t('SHIPPING_ADDRESS')}</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-3">
                                <p className="font-bold text-primary">{customer?.delivery?.firstName} {customer?.delivery?.lastName}</p>
                                <p className="text-foreground/70">{customer?.delivery?.address}</p>
                                <p className="text-foreground/70">{customer?.delivery?.city}, {customer?.delivery?.postalCode}</p>
                                <p className="text-foreground/70 font-semibold">{customer?.delivery?.country}</p>
                                <div className="pt-4 border-t border-primary/5 mt-4">
                                    <p className="text-sm text-muted-foreground">{customer?.delivery?.phone}</p>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </TabsContent>

                <TabsContent value="orders">
                    <Card className="border-2 border-primary/10 shadow-none rounded-[2rem] overflow-hidden">
                        <CardHeader className="bg-primary/5">
                            <CardTitle
                                className={cn(isRtlLayout ? "text-right" : "text-left")}>{t('ORDERS')}</CardTitle>
                        </CardHeader>
                        <CardContent className="p-0">
                            {!orders || !orders.content || orders.content.length === 0 ? (
                                <p className="text-center py-20 text-muted-foreground italic">{t('NO_ORDERS')}</p>
                            ) : (
                                <div className="overflow-x-auto">
                                    <Table>
                                        <TableHeader className="bg-muted/30">
                                            <TableRow className="border-none">
                                                <TableHead
                                                    className={cn("py-6 px-8", isRtlLayout ? "text-right" : "text-left")}>{t('ORDER_ID')}</TableHead>
                                                <TableHead
                                                    className={cn("py-6", isRtlLayout ? "text-right" : "text-left")}>{t('DATE')}</TableHead>
                                                <TableHead
                                                    className={cn("py-6", isRtlLayout ? "text-right" : "text-left")}>{t('TOTAL')}</TableHead>
                                                <TableHead
                                                    className={cn("py-6", isRtlLayout ? "text-right" : "text-left")}>{t('STATUS')}</TableHead>
                                                <TableHead
                                                    className={cn("py-6 px-8", isRtlLayout ? "text-left" : "text-right")}>{t('ACTIONS')}</TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {orders.content.map((order) => (
                                                <TableRow key={order.id}
                                                          className="border-primary/5 hover:bg-primary/5 transition-colors">
                                                    <TableCell className="font-bold py-6 px-8">#{order.id}</TableCell>
                                                    <TableCell
                                                        className="text-foreground/70">{new Date(order.datePurchased).toLocaleDateString()}</TableCell>
                                                    <TableCell
                                                        className="font-bold text-primary">{order?.total?.value}</TableCell>
                                                    <TableCell>
                                                        <span
                                                            className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-primary/10 text-primary uppercase tracking-wider">
                                                            {t(order?.orderStatus ?? 'UNKNOWN')}
                                                        </span>
                                                    </TableCell>
                                                    <TableCell
                                                        className={cn("py-6 px-8", isRtlLayout ? "text-left" : "text-right")}>
                                                        <Button variant="ghost" asChild size="sm"
                                                                className="rounded-full hover:bg-primary hover:text-white transition-all px-6">
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
