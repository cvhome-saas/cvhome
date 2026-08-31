'use client'
import {useEffect, useState} from 'react';
import {useTranslations} from 'next-intl';
import type {Order, OrderHistoryList, StoreContext} from '@store-front/types';
import {useCustomer} from '@store-front/hooks/use-customer';
import {useUser} from '@store-front/hooks/use-user';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '@store-front/ui/table';
import {Skeleton} from '@store-front/ui/skeleton';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';

/** One order, printed as a record: the items ruled, the totals stacked, the history down an ink rule. */
export function OrderDetails({storeContext, orderId}: { storeContext: StoreContext; orderId: number }) {
    const t = useTranslations('PAGE.CUSTOMER');
    const {user} = useUser(storeContext);
    const {getOrderDetails, error} = useCustomer(storeContext);
    const [data, setData] = useState<{ order: Order; history: OrderHistoryList } | null>(null);

    useEffect(() => {
        if (user && orderId) getOrderDetails(orderId).then(d => d && setData(d as { order: Order; history: OrderHistoryList }));
    }, [user, orderId, getOrderDetails]);

    if (error && !data) return <ErrorBlock title={t('ORDER_NOT_FOUND')}>{error.message}</ErrorBlock>;
    if (!data) return <div className="flex flex-col gap-3" aria-busy><Skeleton className="h-8 w-1/3 rounded-none"/><Skeleton className="h-48 w-full rounded-none"/></div>;
    const {order, history} = data;
    const status = (s: string) => t.has(s) ? t(s) : s;

    return (
        <div className="flex flex-col gap-8">
            <div className="hair flex flex-wrap items-center justify-between gap-3 border-b-2 pb-4">
                <h1 className="display text-3xl">{t('ORDER_DETAILS')} <span className="figure text-muted-foreground">#{order.id}</span></h1>
                <span className="pagemark">{status(order.orderStatus)}</span>
            </div>
            <div className="grid gap-8 lg:grid-cols-3">
                <div className="flex flex-col gap-8 lg:col-span-2">
                    <section className="hair border">
                        <h2 className="hair cover-line border-b px-4 py-2.5">{t('ORDER_ITEMS')}</h2>
                        <Table>
                            <TableHeader>
                                <TableRow className="wash">
                                    <TableHead className="cover-line text-start">{t('PRODUCT')}</TableHead>
                                    <TableHead className="cover-line text-center">{t('QTY')}</TableHead>
                                    <TableHead className="cover-line text-end">{t('PRICE')}</TableHead>
                                    <TableHead className="cover-line text-end">{t('TOTAL')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {order.products?.map(p => (
                                    <TableRow key={p.id}>
                                        <TableCell className="font-bold">{p.productName}{!!p.attributes?.length && <span className="block text-xs font-normal text-muted-foreground" dir="auto">{p.attributes.map(a => `${a.attributeName}: ${a.attributeValue}`).join(' / ')}</span>}</TableCell>
                                        <TableCell className="figure text-center">{p.orderedQuantity}</TableCell>
                                        <TableCell className="figure text-end">{p.price}</TableCell>
                                        <TableCell className="figure text-end">{p.subTotal}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                        <div className="hair ms-auto flex max-w-xs flex-col gap-2 border-t p-4">
                            {order.total?.totals?.map(tot => (
                                <div key={tot.id} className="flex justify-between gap-4 text-sm">
                                    <span className="text-muted-foreground">{tot.title}</span><span className="figure">{tot.total}</span>
                                </div>
                            ))}
                            <div className="hair mt-1 flex items-center justify-between gap-4 border-t pt-3">
                                <span className="cover-line">{t('TOTAL')}</span><span className="flag flag-lg">{order.total?.value}</span>
                            </div>
                        </div>
                    </section>
                    <section className="hair border p-4">
                        <h2 className="cover-line mb-5">{t('ORDER_HISTORY')}</h2>
                        <ol className="hair flex flex-col gap-5 border-s-2 ps-5">
                            {history.map((h, i) => (
                                <li key={i} className="relative">
                                    <span className="absolute -start-[1.7rem] top-1 size-3 bg-primary" aria-hidden/>
                                    <p className="cover-line">{status(h.orderStatus)}</p>
                                    <p className="figure mt-1 text-xs text-muted-foreground">{new Date(h.date).toLocaleString()}</p>
                                    {h.comments && <p className="mt-1 text-sm">{h.comments}</p>}
                                </li>
                            ))}
                        </ol>
                    </section>
                </div>
                <div className="flex flex-col gap-6">
                    {[[t('SHIPPING_ADDRESS'), order.delivery], [t('BILLING_ADDRESS'), order.billing]].map(([title, a]) => {
                        const addr = a as Order['delivery'];
                        return (
                            <section key={title as string} className="hair border">
                                <h2 className="hair cover-line border-b px-4 py-2.5">{title as string}</h2>
                                <div className="p-4">
                                    {addr ? (
                                        <address className="text-sm not-italic text-muted-foreground">
                                            <p className="font-bold text-foreground">{addr.firstName} {addr.lastName}</p>
                                            <p>{addr.address}</p><p>{addr.city} {addr.postalCode}</p><p>{addr.country}</p><p className="figure" dir="ltr">{addr.phone}</p>
                                        </address>
                                    ) : <p className="text-sm text-muted-foreground">—</p>}
                                </div>
                            </section>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}
