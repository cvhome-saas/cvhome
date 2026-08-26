'use client'
import {useEffect, useState} from 'react';
import {useTranslations} from 'next-intl';
import type {Order, OrderHistoryList, StoreContext} from '@store-front/types';
import {useCustomer} from '@store-front/hooks/use-customer';
import {useUser} from '@store-front/hooks/use-user';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '@store-front/ui/table';
import {Separator} from '@store-front/ui/separator';
import {Skeleton} from '@store-front/ui/skeleton';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';

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
            <div className="flex flex-wrap items-center justify-between gap-3">
                <h1 className="press flex items-center gap-3 text-3xl leading-none">{t('ORDER_DETAILS')} <span className="dish-no" dir="ltr">{order.id}</span></h1>
                <span className="mark">{status(order.orderStatus)}</span>
            </div>
            <div className="grid gap-8 lg:grid-cols-3">
                <div className="flex flex-col gap-8 lg:col-span-2">
                    <section className="border border-foreground">
                        <h2 className="press plate px-3 py-1.5 text-lg leading-none">{t('ORDER_ITEMS')}</h2>
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="press text-start text-xs tracking-wide">{t('PRODUCT')}</TableHead>
                                    <TableHead className="press text-center text-xs tracking-wide">{t('QTY')}</TableHead>
                                    <TableHead className="press text-end text-xs tracking-wide">{t('PRICE')}</TableHead>
                                    <TableHead className="press text-end text-xs tracking-wide">{t('TOTAL')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {order.products?.map(p => (
                                    <TableRow key={p.id}>
                                        <TableCell className="font-medium">{p.productName}</TableCell>
                                        <TableCell className="text-center">{p.orderedQuantity}</TableCell>
                                        <TableCell className="price text-end">{p.price}</TableCell>
                                        <TableCell className="price text-end">{p.subTotal}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                        <div className="ms-auto flex max-w-xs flex-col gap-2 p-4">
                            {order.total?.totals?.map(tot => (
                                <div key={tot.id} className="flex justify-between text-sm"><span className="text-muted-foreground">{tot.title}</span><span>{tot.total}</span></div>
                            ))}
                            <Separator/>
                            <div className="flex items-baseline justify-between"><span className="press text-base">{t('TOTAL')}</span><span className="price text-2xl">{order.total?.value}</span></div>
                        </div>
                    </section>
                    <section className="border border-foreground p-4">
                        <h2 className="press mb-4 border-b border-foreground pb-1 text-base tracking-wide">{t('ORDER_HISTORY')}</h2>
                        <ol className="flex flex-col gap-4 border-s-2 ps-5">
                            {history.map((h, i) => (
                                <li key={i} className="relative">
                                    <span className="absolute -start-[1.6rem] top-1.5 size-2.5 bg-primary" aria-hidden/>
                                    <p className="press text-sm tracking-wide">{status(h.orderStatus)}</p>
                                    <p className="text-xs text-muted-foreground">{new Date(h.date).toLocaleString()}</p>
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
                            <section key={title as string} className="rounded-card border p-4">
                                <h2 className="press mb-2 border-b border-border pb-1 text-sm tracking-wide">{title as string}</h2>
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
