'use client'
import {useEffect, useState} from 'react';
import {useTranslations} from 'next-intl';
import type {Order, OrderHistoryList, StoreContext} from '@store-front/types';
import {useCustomer} from '@store-front/hooks/use-customer';
import {useUser} from '@store-front/hooks/use-user';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '@store-front/ui/table';
import {Skeleton} from '@store-front/ui/skeleton';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {StatePlate} from '../components/StatePlate';
import {Figure} from '../components/Figure';

export function OrderDetails({storeContext, orderId}: { storeContext: StoreContext; orderId: number }) {
    const t = useTranslations('PAGE.CUSTOMER');
    const {user} = useUser(storeContext);
    const {getOrderDetails, error} = useCustomer(storeContext);
    const [data, setData] = useState<{ order: Order; history: OrderHistoryList } | null>(null);

    useEffect(() => {
        if (user && orderId) getOrderDetails(orderId).then(d => d && setData(d as { order: Order; history: OrderHistoryList }));
    }, [user, orderId, getOrderDetails]);

    if (error && !data) return <ErrorBlock title={t('ORDER_NOT_FOUND')}>{error.message}</ErrorBlock>;
    if (!data) return <div className="flex flex-col gap-3" aria-busy><Skeleton className="h-8 w-1/3"/><Skeleton className="h-48 w-full"/></div>;
    const {order, history} = data;
    const status = (s: string) => t.has(s) ? t(s) : s;

    return (
        <div className="flex flex-col gap-8">
            <div className="enamel-flat flex flex-wrap items-center justify-between gap-4 p-6 lg:p-8">
                <h1 className="sign-lg text-xl lg:text-3xl">{t('ORDER_DETAILS')} <span className="figure opacity-80">#{order.id}</span></h1>
                <span className="state-plate border-transparent bg-background text-foreground">{status(order.orderStatus)}</span>
            </div>
            <div className="grid gap-8 lg:grid-cols-3">
                <div className="flex flex-col gap-8 lg:col-span-2">
                    <section className="rule-brass border">
                        <h2 className="sign rule-brass border-b px-5 py-3 text-[0.625rem] text-muted-foreground">{t('ORDER_ITEMS')}</h2>
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="sign text-start text-[0.625rem]">{t('PRODUCT')}</TableHead>
                                    <TableHead className="sign text-center text-[0.625rem]">{t('QTY')}</TableHead>
                                    <TableHead className="sign text-end text-[0.625rem]">{t('PRICE')}</TableHead>
                                    <TableHead className="sign text-end text-[0.625rem]">{t('TOTAL')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {order.products?.map(p => (
                                    <TableRow key={p.id}>
                                        <TableCell>{p.productName}</TableCell>
                                        <TableCell className="figure text-center">{p.orderedQuantity}</TableCell>
                                        <TableCell className="figure text-end">{p.price}</TableCell>
                                        <TableCell className="figure text-end">{p.subTotal}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                        <div className="ms-auto flex max-w-xs flex-col gap-2.5 p-5">
                            {order.total?.totals?.map(tot => (
                                <div key={tot.id} className="flex justify-between gap-6 text-sm">
                                    <span className="text-muted-foreground">{tot.title}</span><span className="figure">{tot.total}</span>
                                </div>
                            ))}
                            <div className="rule-brass mt-1 flex items-baseline justify-between gap-6 border-t pt-3">
                                <span className="sign text-[0.6875rem]">{t('TOTAL')}</span>
                                <Figure value={order.total?.value ?? ''} className="text-lg"/>
                            </div>
                        </div>
                    </section>
                    <section className="rule-brass border p-5">
                        <h2 className="sign rule-brass mb-4 border-b pb-2 text-[0.625rem] text-muted-foreground">{t('ORDER_HISTORY')}</h2>
                        <ol className="flex flex-col">
                            {history.map((h, i) => (
                                <li key={i} className="rule-brass flex items-baseline gap-4 border-b py-3 last:border-b-0">
                                    <span aria-hidden className="figure w-7 shrink-0 text-xs text-muted-foreground">{String(i + 1).padStart(2, '0')}</span>
                                    <div className="flex-1">
                                        <StatePlate tone="ink">{status(h.orderStatus)}</StatePlate>
                                        {h.comments && <p className="mt-2 text-sm">{h.comments}</p>}
                                    </div>
                                    <time className="figure shrink-0 text-xs text-muted-foreground">{new Date(h.date).toLocaleString()}</time>
                                </li>
                            ))}
                        </ol>
                    </section>
                </div>
                <div className="flex flex-col gap-6">
                    {[[t('SHIPPING_ADDRESS'), order.delivery], [t('BILLING_ADDRESS'), order.billing]].map(([title, a]) => {
                        const addr = a as Order['delivery'];
                        return (
                            <section key={title as string} className="rule-brass border p-5">
                                <h2 className="sign rule-brass mb-3 border-b pb-2 text-[0.625rem] text-muted-foreground">{title as string}</h2>
                                {addr ? (
                                    <address className="text-sm not-italic text-muted-foreground">
                                        <p className="text-foreground">{addr.firstName} {addr.lastName}</p>
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
