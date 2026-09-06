'use client'
import {useEffect, useState} from 'react';
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import {useDir} from '@store-front/i18n/use-dir';
import type {Customer, OrderPage, StoreContext} from '@store-front/types';
import {useCustomer} from '@store-front/hooks/use-customer';
import {useUser} from '@store-front/hooks/use-user';
import {Tabs, TabsContent, TabsList, TabsTrigger} from '@store-front/ui/tabs';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '@store-front/ui/table';
import {Button} from '@store-front/ui/button';
import {Badge} from '@store-front/ui/badge';
import {Skeleton} from '@store-front/ui/skeleton';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {EmptyState} from '../states/EmptyState';

function Address({a, title}: { a: Customer['billing'] | undefined; title: string }) {
    return (
        <section className="rounded-card border p-4">
            <h3 className="mb-2 text-sm font-semibold">{title}</h3>
            {a?.address ? (
                <address className="text-sm not-italic text-muted-foreground">
                    <p className="text-foreground">{a.firstName} {a.lastName}</p>
                    <p>{a.address}</p><p>{a.city} {a.postalCode}</p><p>{a.country}</p><p dir="ltr">{a.phone}</p>
                </address>
            ) : <p className="text-sm text-muted-foreground">—</p>}
        </section>
    );
}

export function CustomerTabs({storeContext}: { storeContext: StoreContext }) {
    const t = useTranslations('PAGE.CUSTOMER');
    const dir = useDir();
    const {user} = useUser(storeContext);
    const {getCustomerInfo, listOrders, loading, error} = useCustomer(storeContext);
    const [customer, setCustomer] = useState<Customer | null>(null);
    const [orders, setOrders] = useState<OrderPage | null>(null);

    useEffect(() => {
        if (!user) return;
        getCustomerInfo().then(c => c && setCustomer(c));
        listOrders(0, 20).then(o => o && setOrders(o));
    }, [user, getCustomerInfo, listOrders]);

    if (error && !customer) return <ErrorBlock title={t('ERROR_LOADING')}>{error.message}</ErrorBlock>;
    if (loading && !customer) return <div className="flex flex-col gap-3" aria-busy><Skeleton className="h-10 w-full"/><Skeleton className="h-40 w-full"/></div>;

    return (
        <Tabs defaultValue="profile" dir={dir}>
            <TabsList className="mb-6 grid w-full grid-cols-3">
                <TabsTrigger value="profile">{t('PROFILE')}</TabsTrigger>
                <TabsTrigger value="addresses">{t('ADDRESSES')}</TabsTrigger>
                <TabsTrigger value="orders">{t('ORDERS')}</TabsTrigger>
            </TabsList>
            <TabsContent value="profile">
                <dl className="grid gap-4 rounded-card border p-4 sm:grid-cols-2">
                    {[[t('FIRST_NAME'), customer?.firstName], [t('LAST_NAME'), customer?.lastName], [t('EMAIL'), customer?.emailAddress], [t('PHONE'), customer?.billing?.phone], [t('USERNAME'), customer?.username]].map(([k, v]) => (
                        <div key={k as string}><dt className="text-xs text-muted-foreground">{k}</dt><dd className="font-medium">{v || '—'}</dd></div>
                    ))}
                </dl>
            </TabsContent>
            <TabsContent value="addresses" className="grid gap-4 md:grid-cols-2">
                <Address a={customer?.billing} title={t('BILLING_ADDRESS')}/>
                <Address a={customer?.delivery} title={t('SHIPPING_ADDRESS')}/>
            </TabsContent>
            <TabsContent value="orders">
                {!orders?.content?.length ? <EmptyState kind="orders"/> : (
                    <div className="overflow-x-auto rounded-card border">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="text-start">{t('ORDER_ID')}</TableHead>
                                    <TableHead className="text-start">{t('DATE')}</TableHead>
                                    <TableHead className="text-start">{t('TOTAL')}</TableHead>
                                    <TableHead className="text-start">{t('STATUS')}</TableHead>
                                    <TableHead className="text-end">{t('ACTIONS')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {orders.content.map(o => (
                                    <TableRow key={o.id}>
                                        <TableCell className="font-medium">#{o.id}</TableCell>
                                        <TableCell>{new Date(o.datePurchased).toLocaleDateString()}</TableCell>
                                        <TableCell>{o.total?.value}</TableCell>
                                        <TableCell><Badge variant="outline">{t.has(o.orderStatus) ? t(o.orderStatus) : o.orderStatus}</Badge></TableCell>
                                        <TableCell className="text-end">
                                            <Button variant="ghost" size="sm" asChild><Link prefetch={false} href={`/customer/order/${o.id}`}>{t('VIEW_ORDER')}</Link></Button>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                )}
            </TabsContent>
        </Tabs>
    );
}
