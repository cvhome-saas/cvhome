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
import {Skeleton} from '@store-front/ui/skeleton';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {StatePlate} from '../components/StatePlate';
import {EmptyState} from '../states/EmptyState';

function Address({a, title}: { a: Customer['billing'] | undefined; title: string }) {
    return (
        <section className="rule-brass border p-5">
            <h3 className="sign rule-brass mb-3 border-b pb-2 text-[0.625rem] text-muted-foreground">{title}</h3>
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
            <TabsList className="rule-brass mb-8 grid w-full grid-cols-3 rounded-control border bg-transparent p-0">
                {(['profile', 'addresses', 'orders'] as const).map(v => (
                    <TabsTrigger key={v} value={v}
                                 className="sign rounded-none py-2.5 text-[0.625rem] data-[state=active]:bg-primary data-[state=active]:text-primary-foreground data-[state=active]:shadow-none">
                        {t(v.toUpperCase())}
                    </TabsTrigger>
                ))}
            </TabsList>
            <TabsContent value="profile">
                <dl className="rule-brass grid gap-5 border p-5 sm:grid-cols-2">
                    {[[t('FIRST_NAME'), customer?.firstNames], [t('LAST_NAME'), customer?.lastName], [t('EMAIL'), customer?.emailAddress], [t('PHONE'), customer?.billing?.phone], [t('USERNAME'), customer?.username]].map(([k, v]) => (
                        <div key={k as string}><dt className="sign text-[0.625rem] text-muted-foreground">{k}</dt><dd className="mt-1">{v || '—'}</dd></div>
                    ))}
                </dl>
            </TabsContent>
            <TabsContent value="addresses" className="grid gap-4 md:grid-cols-2">
                <Address a={customer?.billing} title={t('BILLING_ADDRESS')}/>
                <Address a={customer?.delivery} title={t('SHIPPING_ADDRESS')}/>
            </TabsContent>
            <TabsContent value="orders">
                {!orders?.content?.length ? <EmptyState kind="orders"/> : (
                    <div className="rule-brass overflow-x-auto border">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="sign text-start text-[0.625rem]">{t('ORDER_ID')}</TableHead>
                                    <TableHead className="sign text-start text-[0.625rem]">{t('DATE')}</TableHead>
                                    <TableHead className="sign text-start text-[0.625rem]">{t('TOTAL')}</TableHead>
                                    <TableHead className="sign text-start text-[0.625rem]">{t('STATUS')}</TableHead>
                                    <TableHead className="sign text-end text-[0.625rem]">{t('ACTIONS')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {orders.content.map(o => (
                                    <TableRow key={o.id}>
                                        <TableCell className="figure">#{o.id}</TableCell>
                                        <TableCell className="figure">{new Date(o.datePurchased).toLocaleDateString()}</TableCell>
                                        <TableCell className="figure">{o.total?.value}</TableCell>
                                        <TableCell><StatePlate tone="ink">{t.has(o.orderStatus) ? t(o.orderStatus) : o.orderStatus}</StatePlate></TableCell>
                                        <TableCell className="text-end">
                                            <Button variant="ghost" size="sm" asChild className="sign text-[0.625rem]"><Link prefetch={false} href={`/customer/order/${o.id}`}>{t('VIEW_ORDER')}</Link></Button>
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
