'use client'
import {useEffect, useState} from 'react';
import {useTranslations} from 'next-intl';
import type {Order, OrderHistoryList, StoreContext} from '@store-front/types';
import {useCustomer} from '@store-front/hooks/use-customer';
import {useUser} from '@store-front/hooks/use-user';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '@store-front/ui/table';
import {Skeleton} from '@store-front/ui/skeleton';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';

const HEAD = 'font-display text-xs uppercase tracking-wide text-muted-foreground';

/** One order: its number sheet with a status stamp, the items sheet with totals, the history sheet, the address sheets. */
export function OrderDetails({storeContext, orderId}: { storeContext: StoreContext; orderId: number }) {
    const t = useTranslations('PAGE.CUSTOMER');
    const {user} = useUser(storeContext);
    const {getOrderDetails, error} = useCustomer(storeContext);
    const [data, setData] = useState<{ order: Order; history: OrderHistoryList } | null>(null);

    useEffect(() => {
        if (user && orderId) getOrderDetails(orderId).then(d => d && setData(d as { order: Order; history: OrderHistoryList }));
    }, [user, orderId, getOrderDetails]);

    if (error && !data) return <div className="sheet p-6"><ErrorBlock title={t('ORDER_NOT_FOUND')}>{error.message}</ErrorBlock></div>;
    if (!data) return <div className="flex flex-col gap-3" aria-busy><Skeleton className="h-8 w-1/3 rounded-none"/><Skeleton className="h-48 w-full rounded-none"/></div>;
    const {order, history} = data;
    const status = (s: string) => t.has(s) ? t(s) : s;

    return (
        <div className="flex flex-col gap-6">
            <div className="flex flex-wrap items-center gap-4">
                <h1 className="strip h-11 text-xl [--tilt:-0.7deg]">{t('ORDER_DETAILS')} <span className="tabular-nums text-muted-foreground">#{order.id}</span></h1>
                <span className="stamp [rotate:-4deg]">{status(order.orderStatus)}</span>
            </div>
            <div className="grid gap-6 lg:grid-cols-3">
                <div className="flex flex-col gap-6 lg:col-span-2">
                    <section className="sheet [--tilt:0.3deg]">
                        <h2 className="px-4 pt-4 font-display text-lg uppercase tracking-wide">{t('ORDER_ITEMS')}</h2>
                        <Table>
                            <TableHeader>
                                <TableRow className="rule">
                                    <TableHead className={`${HEAD} text-start`}>{t('PRODUCT')}</TableHead>
                                    <TableHead className={`${HEAD} text-center`}>{t('QTY')}</TableHead>
                                    <TableHead className={`${HEAD} text-end`}>{t('PRICE')}</TableHead>
                                    <TableHead className={`${HEAD} text-end`}>{t('TOTAL')}</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {order.products?.map(p => (
                                    <TableRow key={p.id} className="rule">
                                        <TableCell className="font-medium">{p.productName}{!!p.attributes?.length && <span className="block text-xs font-normal text-muted-foreground" dir="auto">{p.attributes.map(a => `${a.attributeName}: ${a.attributeValue}`).join(' / ')}</span>}</TableCell>
                                        <TableCell className="text-center tabular-nums">{p.orderedQuantity}</TableCell>
                                        <TableCell className="text-end tabular-nums">{p.price}</TableCell>
                                        <TableCell className="text-end font-semibold tabular-nums">{p.subTotal}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                        <div className="ms-auto flex max-w-xs flex-col gap-2 p-4">
                            {order.total?.totals?.map(tot => (
                                <div key={tot.id} className="flex justify-between text-sm"><span className="text-muted-foreground">{tot.title}</span><span className="tabular-nums">{tot.total}</span></div>
                            ))}
                            <div className="rule flex justify-between border-t pt-2 font-display text-lg uppercase"><span>{t('TOTAL')}</span><span className="tabular-nums">{order.total?.value}</span></div>
                        </div>
                    </section>
                    <section className="sheet p-4 [--tilt:-0.3deg]">
                        <h2 className="mb-4 font-display text-lg uppercase tracking-wide">{t('ORDER_HISTORY')}</h2>
                        <ol className="rule flex flex-col gap-4 border-s-2 ps-5">
                            {history.map((h, i) => (
                                <li key={i} className="relative">
                                    <span className="absolute -start-[1.45rem] top-1.5 size-3 bg-primary" aria-hidden/>
                                    <p className="font-display uppercase tracking-wide">{status(h.orderStatus)}</p>
                                    <p className="text-xs tabular-nums text-muted-foreground">{new Date(h.date).toLocaleString()}</p>
                                    {h.comments && <p className="mt-1 text-sm">{h.comments}</p>}
                                </li>
                            ))}
                        </ol>
                    </section>
                </div>
                <div className="wall wall-calm flex flex-col gap-6">
                    {[[t('SHIPPING_ADDRESS'), order.delivery], [t('BILLING_ADDRESS'), order.billing]].map(([title, a]) => {
                        const addr = a as Order['delivery'];
                        return (
                            <section key={title as string} className="sheet p-4">
                                <h2 className="mb-2 font-display text-base uppercase tracking-wide">{title as string}</h2>
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
