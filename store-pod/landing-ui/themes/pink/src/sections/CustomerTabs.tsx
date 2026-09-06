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
import {EmptyState} from '../states/EmptyState';

const TAB = 'cover-line h-10 rounded-none border-0 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground data-[state=active]:shadow-none';

function Address({a, title}: { a: Customer['billing'] | undefined; title: string }) {
    return (
        <section className="hair border">
            <h3 className="hair cover-line border-b px-4 py-2.5">{title}</h3>
            <div className="p-4">
                {a?.address ? (
                    <address className="text-sm not-italic text-muted-foreground">
                        <p className="font-bold text-foreground">{a.firstName} {a.lastName}</p>
                        <p>{a.address}</p><p>{a.city} {a.postalCode}</p><p>{a.country}</p><p className="figure" dir="ltr">{a.phone}</p>
                    </address>
                ) : <p className="text-sm text-muted-foreground">—</p>}
            </div>
        </section>
    );
}

/** The reader's own page in the issue: profile, addresses and the record of what she has ordered. */
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
    if (loading && !customer) return <div className="flex flex-col gap-3" aria-busy><Skeleton className="h-10 w-full rounded-none"/><Skeleton className="h-40 w-full rounded-none"/></div>;

    return (
        <Tabs defaultValue="profile" dir={dir}>
            <TabsList className="hair mb-6 grid w-full grid-cols-3 gap-0 rounded-none border-b-2 bg-transparent p-0">
                <TabsTrigger value="profile" className={TAB}>{t('PROFILE')}</TabsTrigger>
                <TabsTrigger value="addresses" className={TAB}>{t('ADDRESSES')}</TabsTrigger>
                <TabsTrigger value="orders" className={TAB}>{t('ORDERS')}</TabsTrigger>
            </TabsList>
            <TabsContent value="profile">
                <dl className="hair grid border sm:grid-cols-2">
                    {[[t('FIRST_NAME'), customer?.firstName], [t('LAST_NAME'), customer?.lastName], [t('EMAIL'), customer?.emailAddress], [t('PHONE'), customer?.billing?.phone], [t('USERNAME'), customer?.username]].map(([k, v]) => (
                        <div key={k as string} className="hair border-b border-e p-4 last:border-b-0">
                            <dt className="cover-line text-muted-foreground">{k}</dt>
                            <dd className="mt-1 font-bold">{v || '—'}</dd>
                        </div>
                    ))}
                </dl>
            </TabsContent>
            <TabsContent value="addresses" className="grid gap-4 md:grid-cols-2">
                <Address a={customer?.billing} title={t('BILLING_ADDRESS')}/>
                <Address a={customer?.delivery} title={t('SHIPPING_ADDRESS')}/>
            </TabsContent>
            <TabsContent value="orders">
                {!orders?.content?.length ? <EmptyState kind="orders"/> : (
                    <div className="hair overflow-x-auto border">
                        <Table>
                            <TableHeader>
                                <TableRow className="wash">
                                    <TableHead className="cover-line text-start">{t('ORDER_ID')}</TableHead>
                                    <TableHead className="cover-line text-start">{t('DATE')}</TableHead>
                                    <TableHead className="cover-line text-start">{t('TOTAL')}</TableHead>
                                    <TableHead className="cover-line text-start">{t('STATUS')}</TableHead>
                                    <TableHead className="cover-line text-end">{t('ACTIONS')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {orders.content.map(o => (
                                    <TableRow key={o.id}>
                                        <TableCell className="figure">#{o.id}</TableCell>
                                        <TableCell className="figure">{new Date(o.datePurchased).toLocaleDateString()}</TableCell>
                                        <TableCell><span className="flag">{o.total?.value}</span></TableCell>
                                        <TableCell><span className="pagemark-open pagemark">{t.has(o.orderStatus) ? t(o.orderStatus) : o.orderStatus}</span></TableCell>
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
