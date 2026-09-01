'use client'
import {useTranslations} from 'next-intl';
import {ShoppingCartIcon} from 'lucide-react';
import type {Product, StoreContext} from '@store-front/types';
import {useProductPurchase, type ProductPurchase} from '@store-front/hooks/use-product-purchase';
import {Button} from '@store-front/ui/button';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';
import {Gallery} from './Gallery';

/**
 * The rail tag at product scale: name in the signage voice, the price big on its tag, stock printed as a
 * sticker, options as tiles, stepper + add. Owns the gallery too, because the shown images follow the
 * selected variant. On phones the price and the add action ride a sticky counter bar at the bottom.
 */
/** The first axis with nothing chosen — what the "please choose" line names. */
const unpicked = (p: ProductPurchase) => p.options.find(o => p.selection[o.id] === undefined);

export function BuyBox({product, storeContext, details}: { product: Product; storeContext: StoreContext; details?: ReactNode }) {
    const t = useTranslations('PAGE.PRODUCT');
    const p = useProductPurchase(storeContext, product);
    const addLabel = p.status === 'adding' ? t('ADDING') : p.isOutOfStock ? t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK') : t('ADD_TO_CART');

    return (
        <div className="grid gap-6 lg:grid-cols-2 lg:items-start lg:gap-10">
            <Gallery images={p.images} alt={product.description?.name ?? ''}/>
            <div className="flex flex-col gap-6">
                <div className="flex flex-col gap-3">
                    {product.manufacturer?.description?.name && (
                        <p className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">{product.manufacturer.description.name}</p>
                    )}
                    <h1 className="signage text-3xl sm:text-4xl" dir="auto"><bdi>{product.description?.name}</bdi></h1>
                    <div className="flex flex-wrap items-end gap-x-4 gap-y-1">
                        {p.price.discounted && p.price.originalPrice && <del className="text-base tabular-nums text-muted-foreground">{p.price.originalPrice}</del>}
                        <span className="price text-5xl">{p.price.finalPrice}</span>
                    </div>
                    <div className="flex flex-wrap items-center gap-2 text-sm">
                        {p.isOutOfStock ? <span className="sticker">{t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK')}</span>
                            : p.maxQty <= 5 ? <span className="sticker sticker-outline">{t('LOW_STOCK', {count: p.maxQty})}</span>
                                : <span className="sticker sticker-success">{t('IN_STOCK')}</span>}
                        {p.sku && <span className="font-mono text-xs text-muted-foreground">{t('SKU')}: <span dir="ltr">{p.sku}</span></span>}
                    </div>
                </div>

                {p.options.map(option => (
                    <fieldset key={option.id} className="flex flex-col gap-2">
                        <legend className="signage mb-1 text-lg">
                            {option.name || option.code}
                            {p.selection[option.id] === undefined && <span className="ms-2 font-sans text-sm font-normal normal-case tracking-normal text-muted-foreground">— {t('SELECT_OPTION', {option: option.name || option.code})}</span>}
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
                                            className={cn('min-w-11 rounded-control border-2 px-3.5 py-2 text-sm font-semibold transition-colors duration-(--motion-fast)',
                                                selected ? 'border-primary bg-primary text-primary-foreground' : 'bg-card hover:bg-muted',
                                                !available && 'border-dashed text-muted-foreground line-through')}>
                                        {label}
                                    </button>
                                );
                            })}
                        </div>
                    </fieldset>
                ))}

                <div className="flex flex-wrap items-center gap-3">
                    <QuantityStepper value={p.quantity} onDecrement={p.decrementQuantity} onIncrement={p.incrementQuantity}
                                     canDecrement={p.canDecrease} canIncrement={p.canIncrease}
                                     decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}/>
                    <Button size="lg" className="signage h-12 flex-1 text-lg" disabled={!p.canAdd} onClick={p.addToCart}>
                        <ShoppingCartIcon data-icon="inline-start"/>
                        {addLabel}
                    </Button>
                </div>
                {p.inCartQuantity > 0 && <p className="text-sm font-semibold text-muted-foreground">{t('IN_CART', {count: p.inCartQuantity})}</p>}
                {!p.allSelected && !p.isOutOfStock && (
                    <p className="text-sm text-muted-foreground">{t('OPTION_REQUIRED', {option: unpicked(p)?.name || unpicked(p)?.code || ''})}</p>
                )}
                {details}
            </div>

            {/* the counter bar: price + add, always in thumb reach on phones */}
            <div className="fixed inset-x-0 bottom-0 z-30 border-t-2 bg-background p-3 pb-[max(0.75rem,env(safe-area-inset-bottom))] shadow-overlay lg:hidden">
                <div className="mx-auto flex max-w-content items-center gap-3">
                    <span className="price text-2xl">{p.price.finalPrice}</span>
                    <Button size="lg" className="signage h-12 flex-1 text-base" disabled={!p.canAdd} onClick={p.addToCart}>
                        <ShoppingCartIcon data-icon="inline-start"/>
                        {addLabel}
                    </Button>
                </div>
            </div>
        </div>
    );
}
