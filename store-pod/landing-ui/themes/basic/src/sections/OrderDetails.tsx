'use client'
import {useEffect, useState} from 'react';
import {useTranslations} from 'next-intl';
import type {Order, OrderHistoryList, StoreContext} from '@store-front/types';
import {useCustomer} from '@store-front/hooks/use-customer';
import {useUser} from '@store-front/hooks/use-user';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '@store-front/ui/table';
import {Skeleton} from '@store-front/ui/skeleton';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';

/** One order as a printed invoice: the ledger of lines, totals in the price voice, the history as a ruled timeline, addresses in cells. */
export function OrderDetails({storeContext, orderId}: { storeContext: StoreContext; orderId: number }) {
    const t = useTranslations('PAGE.CUSTOMER');
    const {user} = useUser(storeContext);
    const {getOrderDetails, error} = useCustomer(storeContext);
    const [data, setData] = useState<{ order: Order; history: OrderHistoryList } | null>(null);

    useEffect(() => {
        if (user && orderId) getOrderDetails(orderId).then(d => d && setData(d as { order: Order; history: OrderHistoryList }));
    }, [user, orderId, getOrderDetails]);

    if (error && !data) return <ErrorBlock title={t('ORDER_NOT_FOUND')} className="border">{error.message}</ErrorBlock>;
    if (!data) return <div className="flex flex-col gap-3" aria-busy><Skeleton className="h-10 w-1/3"/><Skeleton className="h-48 w-full"/></div>;
    const {order, history} = data;
    const status = (s: string) => t.has(s) ? t(s) : s;
    const cellHead = 'display border-b px-4 py-3 text-lg';

    return (
        <div className="flex flex-col gap-6">
            <div className="running-head">
                <h1 className="running-head-title">{t('ORDER_DETAILS')} <span className="text-muted-foreground tabular-nums">#{order.id}</span></h1>
                <span className="stamp stamp-outline mb-0.5">{status(order.orderStatus)}</span>
            </div>
            <div className="grid gap-6 lg:grid-cols-3">
                <div className="flex flex-col gap-6 lg:col-span-2">
                    <section className="border">
                        <h2 className={cellHead}>{t('ORDER_ITEMS')}</h2>
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="text-start">{t('PRODUCT')}</TableHead>
                                    <TableHead className="text-center">{t('QTY')}</TableHead>
                                    <TableHead className="text-end">{t('PRICE')}</TableHead>
                                    <TableHead className="text-end">{t('TOTAL')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {order.products?.map(p => (
                                    <TableRow key={p.id}>
                                        <TableCell className="whitespace-normal font-medium"><bdi dir="auto">{p.productName}</bdi>{!!p.attributes?.length && <span className="block text-xs font-normal text-muted-foreground" dir="auto">{p.attributes.map(a => `${a.attributeName}: ${a.attributeValue}`).join(' / ')}</span>}</TableCell>
                                        <TableCell className="text-center tabular-nums">{p.orderedQuantity}</TableCell>
                                        <TableCell className="text-end tabular-nums">{p.price}</TableCell>
                                        <TableCell className="price text-end text-base">{p.subTotal}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                        <div className="ms-auto flex max-w-xs flex-col gap-2 border-t p-4">
                            {order.total?.totals?.map(tot => (
                                <div key={tot.id} className="flex justify-between gap-4 text-sm tabular-nums"><span className="text-muted-foreground">{tot.title}</span><span>{tot.total}</span></div>
                            ))}
                            <div className="mt-1 flex items-baseline justify-between gap-4 border-t pt-2">
                                <span className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">{t('TOTAL')}</span>
                                <span className="price text-2xl">{order.total?.value}</span>
                            </div>
                        </div>
                    </section>
                    <section className="border">
                        <h2 className={cellHead}>{t('ORDER_HISTORY')}</h2>
                        <ol className="m-4 flex flex-col gap-4 border-s ps-5">
                            {history.map((h, i) => (
                                <li key={i} className="relative">
                                    <span className="absolute -start-[calc(1.25rem+3.5px)] top-1.5 size-1.5 bg-primary" aria-hidden/>
                                    <p className="font-medium">{status(h.orderStatus)}</p>
                                    <p className="text-xs tabular-nums text-muted-foreground">{new Date(h.date).toLocaleString()}</p>
                                    {h.comments && <p className="mt-1 text-sm">{h.comments}</p>}
                                </li>
                            ))}
                        </ol>
                    </section>
                </div>
                <div className="ruled lg:grid-cols-1">
                    {[[t('SHIPPING_ADDRESS'), order.delivery], [t('BILLING_ADDRESS'), order.billing]].map(([title, a]) => {
                        const addr = a as Order['delivery'];
                        return (
                            <section key={title as string} className="p-4 lg:p-5">
                                <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">{title as string}</h2>
                                {addr ? (
                                    <address className="text-sm not-italic text-muted-foreground">
                                        <p className="font-medium text-foreground">{addr.firstName} {addr.lastName}</p>
                                        <p>{addr.address}</p><p>{addr.city} {addr.postalCode}</p><p>{addr.country}</p><p dir="ltr">{addr.phone}</p>
                                    </address>
                                ) : <p className="text-sm text-muted-foreground">—</p>}
                            </section>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}
