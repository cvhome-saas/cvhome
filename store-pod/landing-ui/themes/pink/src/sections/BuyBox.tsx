'use client'
import type {ReactNode} from 'react';
import {useTranslations} from 'next-intl';
import type {Product, StoreContext} from '@store-front/types';
import {useProductPurchase, type ProductPurchase} from '@store-front/hooks/use-product-purchase';
import {Button} from '@store-front/ui/button';
import {Price} from '@store-front/ui/price';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {cn} from '@store-front/ui/lib/utils';
import {ArrowMark, RingMark} from '../components/Marks';
import {Gallery} from './Gallery';

/**
 * The feature page: picture well beside the printed facts. Name at cover scale, the price on the biggest
 * flag in the issue, options as ruled chips, and the pen calling out whatever is nearly gone. Owns the
 * gallery too, because the shown images follow the selected variant. `details` rides under the action so
 * the buying column reads as one printed panel instead of trailing off into white.
 */
/** The first axis with nothing chosen — what the "please choose" line names. */
const unpicked = (p: ProductPurchase) => p.options.find(o => p.selection[o.id] === undefined);

export function BuyBox({product, storeContext, layout = 'split', details}: {
    product: Product; storeContext: StoreContext; layout?: 'split' | 'stacked'; details?: ReactNode;
}) {
    const t = useTranslations('PAGE.PRODUCT');
    const p = useProductPurchase(storeContext, product);

    return (
        <div className={cn('grid gap-8 lg:gap-12', layout === 'split' && 'lg:grid-cols-2 lg:items-start')}>
            <Gallery images={p.images} alt={product.description?.name ?? ''}/>
            <div className="flex flex-col gap-6">
                <div className="flex flex-col gap-4">
                    {product.manufacturer?.description?.name && (
                        <p className="cover-line text-muted-foreground">{product.manufacturer.description.name}</p>
                    )}
                    <h1 className="display text-3xl sm:text-4xl">{product.description?.name}</h1>
                    <div className="relative w-fit">
                        <Price className="price-flag price-flag-lg" finalPrice={p.price.finalPrice} originalPrice={p.price.originalPrice} discounted={p.price.discounted}/>
                        {p.price.discounted && (
                            <span aria-hidden className="pointer-events-none absolute -inset-x-4 -inset-y-2.5 text-sale">
                                <RingMark className="size-full"/>
                            </span>
                        )}
                    </div>
                    <div className="flex flex-wrap items-center gap-3">
                        {p.isOutOfStock ? <span className="flag flag-ink">{t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK')}</span>
                            : p.maxQty <= 5 ? (
                                <span className="marker">
                                    <ArrowMark className="h-4 w-7 shrink-0 rtl:-scale-x-100"/>{t('LOW_STOCK', {count: p.maxQty})}
                                </span>
                            ) : <span className="cover-line text-success">{t('IN_STOCK')}</span>}
                        {p.sku && (
                            <span className="pagemark-open pagemark">
                                {/* merchants whose SKU already begins with "SKU" would otherwise print "SKU SKU-…" */}
                                {!/^sku/i.test(p.sku) && `${t('SKU')} `}<span dir="ltr">{p.sku}</span>
                            </span>
                        )}
                    </div>
                </div>

                {p.options.map(option => (
                    <fieldset key={option.id} className="flex flex-col gap-2.5">
                        <legend className="cover-line mb-1.5">
                            {option.name || option.code}
                            {p.selection[option.id] === undefined && <span className="ms-2 text-muted-foreground">— {t('SELECT_OPTION', {option: option.name || option.code})}</span>}
                        </legend>
                        <div className="flex flex-wrap gap-2" role="radiogroup" aria-label={option.name || option.code}>
                            {option.values.map(value => {
                                const selected = p.selection[option.id] === value.id;
                                const available = p.isValueAvailable(option, value);
                                const label = value.name || value.code;
                                return (
                                    <button key={value.id} type="button" role="radio" aria-checked={selected} onClick={() => p.select(option.id, value.id)}
                                            aria-label={available ? label : `${label} — ${t('UNAVAILABLE_COMBINATION')}`}
                                            aria-disabled={!available}
                                            className={cn('hair min-w-11 border px-3.5 py-2 text-sm font-bold transition-colors duration-(--motion-fast)',
                                                selected ? 'flood border-primary' : 'hover:bg-secondary',
                                                !available && 'border-dashed text-muted-foreground line-through')}>
                                        {label}
                                    </button>
                                );
                            })}
                        </div>
                    </fieldset>
                ))}

                <div className="hair flex flex-wrap items-center gap-3 border-t-2 pt-5">
                    <QuantityStepper value={p.quantity} onDecrement={p.decrementQuantity} onIncrement={p.incrementQuantity}
                                     canDecrement={p.canDecrease} canIncrement={p.canIncrease}
                                     decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}/>
                    <Button size="lg" className="flex-1" disabled={!p.canAdd} onClick={p.addToCart}>
                        {p.status === 'adding' ? t('ADDING') : p.isOutOfStock ? t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK') : t('ADD_TO_CART')}
                    </Button>
                </div>
                {p.inCartQuantity > 0 && <p className="figure text-sm text-muted-foreground">{t('IN_CART', {count: p.inCartQuantity})}</p>}
                {!p.allSelected && !p.isOutOfStock && (
                    <p className="text-sm text-muted-foreground">{t('OPTION_REQUIRED', {option: unpicked(p)?.name || unpicked(p)?.code || ''})}</p>
                )}
                {details}
            </div>
        </div>
    );
}
