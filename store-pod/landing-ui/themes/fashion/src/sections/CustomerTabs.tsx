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

const TAB = 'strip strip-hover h-10 flex-1 justify-center rounded-none border-0 text-sm shadow-sm data-[state=active]:bg-primary data-[state=active]:text-primary-foreground data-[state=active]:shadow-sm';
const HEAD = 'font-display text-xs uppercase tracking-wide text-muted-foreground';

function Address({a, title}: { a: Customer['billing'] | undefined; title: string }) {
    return (
        <section className="sheet p-4">
            <h3 className="mb-2 font-display text-base uppercase tracking-wide">{title}</h3>
            {a?.address ? (
                <address className="text-sm not-italic text-muted-foreground">
                    <p className="text-foreground">{a.firstName} {a.lastName}</p>
                    <p>{a.address}</p><p>{a.city} {a.postalCode}</p><p>{a.country}</p><p dir="ltr">{a.phone}</p>
                </address>
            ) : <p className="text-sm text-muted-foreground">—</p>}
        </section>
    );
}

/** Account as three strips (profile · addresses · orders) over sheets. */
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

    if (error && !customer) return <div className="sheet p-6"><ErrorBlock title={t('ERROR_LOADING')}>{error.message}</ErrorBlock></div>;
    if (loading && !customer) return <div className="flex flex-col gap-3" aria-busy><Skeleton className="h-10 w-full rounded-none"/><Skeleton className="h-40 w-full rounded-none"/></div>;

    return (
        <Tabs defaultValue="profile" dir={dir}>
            <TabsList className="wall-calm wall mb-6 flex h-auto w-full gap-2 bg-transparent p-0">
                <TabsTrigger value="profile" className={TAB}>{t('PROFILE')}</TabsTrigger>
                <TabsTrigger value="addresses" className={TAB}>{t('ADDRESSES')}</TabsTrigger>
                <TabsTrigger value="orders" className={TAB}>{t('ORDERS')}</TabsTrigger>
            </TabsList>
            <TabsContent value="profile">
                <dl className="sheet grid gap-4 p-5 [--tilt:0.3deg] sm:grid-cols-2">
                    {[[t('FIRST_NAME'), customer?.firstName], [t('LAST_NAME'), customer?.lastName], [t('EMAIL'), customer?.emailAddress], [t('PHONE'), customer?.billing?.phone], [t('USERNAME'), customer?.username]].map(([k, v]) => (
                        <div key={k as string}><dt className={HEAD}>{k}</dt><dd className="font-medium">{v || '—'}</dd></div>
                    ))}
                </dl>
            </TabsContent>
            <TabsContent value="addresses" className="wall wall-calm grid gap-4 md:grid-cols-2">
                <Address a={customer?.billing} title={t('BILLING_ADDRESS')}/>
                <Address a={customer?.delivery} title={t('SHIPPING_ADDRESS')}/>
            </TabsContent>
            <TabsContent value="orders">
                {!orders?.content?.length ? <EmptyState kind="orders"/> : (
                    <div className="sheet overflow-x-auto [--tilt:0.3deg]">
                        <Table>
                            <TableHeader>
                                <TableRow className="rule">
                                    <TableHead className={`${HEAD} text-start`}>{t('ORDER_ID')}</TableHead>
                                    <TableHead className={`${HEAD} text-start`}>{t('DATE')}</TableHead>
                                    <TableHead className={`${HEAD} text-start`}>{t('TOTAL')}</TableHead>
                                    <TableHead className={`${HEAD} text-start`}>{t('STATUS')}</TableHead>
                                    <TableHead className={`${HEAD} text-end`}>{t('ACTIONS')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {orders.content.map(o => (
                                    <TableRow key={o.id} className="rule">
                                        <TableCell className="font-medium tabular-nums">#{o.id}</TableCell>
                                        <TableCell className="tabular-nums">{new Date(o.datePurchased).toLocaleDateString()}</TableCell>
                                        <TableCell className="tabular-nums">{o.total?.value}</TableCell>
                                        <TableCell><span className="stamp [rotate:-2deg] text-xs">{t.has(o.orderStatus) ? t(o.orderStatus) : o.orderStatus}</span></TableCell>
                                        <TableCell className="text-end">
                                            <Link prefetch={false} href={`/customer/order/${o.id}`} className="strip strip-hover h-8 text-xs [--tilt:0deg]">{t('VIEW_ORDER')}</Link>
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
