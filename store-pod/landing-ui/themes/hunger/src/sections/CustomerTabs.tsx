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
import {Skeleton} from '@store-front/ui/skeleton';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {EmptyState} from '../states/EmptyState';

function Address({a, title}: { a: Customer['billing'] | undefined; title: string }) {
    return (
        <section className="border border-foreground p-4">
            <h3 className="press mb-2 border-b border-border pb-1 text-sm tracking-wide">{title}</h3>
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
            <TabsList className="mb-6 grid w-full grid-cols-3 rounded-none border border-foreground bg-transparent p-0">
                <TabsTrigger value="profile" className="press rounded-none data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">{t('PROFILE')}</TabsTrigger>
                <TabsTrigger value="addresses" className="press rounded-none data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">{t('ADDRESSES')}</TabsTrigger>
                <TabsTrigger value="orders" className="press rounded-none data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">{t('ORDERS')}</TabsTrigger>
            </TabsList>
            <TabsContent value="profile">
                <dl className="grid gap-4 border border-foreground p-4 sm:grid-cols-2">
                    {[[t('FIRST_NAME'), customer?.firstName], [t('LAST_NAME'), customer?.lastName], [t('EMAIL'), customer?.emailAddress], [t('PHONE'), customer?.billing?.phone], [t('USERNAME'), customer?.username]].map(([k, v]) => (
                        <div key={k as string}><dt className="press text-xs tracking-wide text-muted-foreground">{k}</dt><dd className="font-medium">{v || '—'}</dd></div>
                    ))}
                </dl>
            </TabsContent>
            <TabsContent value="addresses" className="grid gap-4 md:grid-cols-2">
                <Address a={customer?.billing} title={t('BILLING_ADDRESS')}/>
                <Address a={customer?.delivery} title={t('SHIPPING_ADDRESS')}/>
            </TabsContent>
            <TabsContent value="orders">
                {!orders?.content?.length ? <EmptyState kind="orders"/> : (
                    <div className="overflow-x-auto border border-foreground">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="press text-start text-xs tracking-wide">{t('ORDER_ID')}</TableHead>
                                    <TableHead className="press text-start text-xs tracking-wide">{t('DATE')}</TableHead>
                                    <TableHead className="press text-start text-xs tracking-wide">{t('TOTAL')}</TableHead>
                                    <TableHead className="press text-start text-xs tracking-wide">{t('STATUS')}</TableHead>
                                    <TableHead className="press text-end text-xs tracking-wide">{t('ACTIONS')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {orders.content.map(o => (
                                    <TableRow key={o.id}>
                                        <TableCell><span className="dish-no" dir="ltr">{o.id}</span></TableCell>
                                        <TableCell>{new Date(o.datePurchased).toLocaleDateString()}</TableCell>
                                        <TableCell className="price">{o.total?.value}</TableCell>
                                        <TableCell><span className="mark">{t.has(o.orderStatus) ? t(o.orderStatus) : o.orderStatus}</span></TableCell>
                                        <TableCell className="text-end">
                                            <Link prefetch={false} href={`/customer/order/${o.id}`} className="press text-xs tracking-wide underline decoration-primary underline-offset-4">{t('VIEW_ORDER')}</Link>
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
