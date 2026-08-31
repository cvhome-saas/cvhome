'use client'
import {useTranslations} from 'next-intl';
import {ShoppingBagIcon} from 'lucide-react';
import type {Product, StoreContext} from '@store-front/types';
import {useProductPurchase} from '@store-front/hooks/use-product-purchase';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {cn} from '@store-front/ui/lib/utils';
import {Gallery} from './Gallery';
import {ProductBadges} from '../components/ProductBadges';
import {useMounted} from '../components/use-mounted';

/**
 * The product's poster (gallery) beside its spec sheet: brand line, name in poster caps, the price printed
 * large, stock and SKU facts, option strips (the chosen one on day-glo), quantity and the day-glo ADD strip.
 * Owns the gallery because the shown images follow the selected variant.
 */
export function BuyBox({product, storeContext}: { product: Product; storeContext: StoreContext }) {
    const t = useTranslations('PAGE.PRODUCT');
    const p = useProductPurchase(storeContext, product);
    const mounted = useMounted();
    const name = product.description?.name ?? '';
    const brand = product.manufacturer?.description?.name;

    return (
        <div className="grid gap-6 lg:grid-cols-[minmax(0,7fr)_minmax(0,5fr)] lg:items-start lg:gap-10">
            <Gallery images={p.images} alt={name} brand={brand}/>
            <div className="sheet sheen flex flex-col gap-6 p-5 sm:p-7 lg:sticky lg:top-[calc(var(--header-h-lg)+1.5rem)] [--tilt:0.5deg]">
                <div className="flex flex-col gap-3">
                    {brand && <p className="font-display text-xs uppercase tracking-[0.2em] text-muted-foreground">{brand}</p>}
                    <h1 className="font-display text-3xl uppercase leading-[0.9] [overflow-wrap:anywhere] sm:text-4xl" dir="auto"><bdi>{name}</bdi></h1>
                    <p className="flex flex-wrap items-baseline gap-x-3 leading-none tabular-nums">
                        <span className="text-3xl font-bold">{p.price.finalPrice}</span>
                        {p.price.discounted && p.price.originalPrice && <del className="text-base text-muted-foreground">{p.price.originalPrice}</del>}
                    </p>
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm">
                        {p.isOutOfStock ? <ProductBadges product={product} className="flex gap-2"/>
                            : p.maxQty <= 5 ? <span className="stamp [rotate:-3deg]">{t('LOW_STOCK', {count: p.maxQty})}</span>
                                : <span className="font-display uppercase tracking-wide text-muted-foreground">{t('IN_STOCK')}</span>}
                        {p.sku && <span className="text-xs uppercase tracking-wide text-muted-foreground">{t('SKU')} <span dir="ltr" className="tabular-nums">{p.sku}</span></span>}
                    </div>
                </div>

                {p.options.map(option => (
                    <fieldset key={option.id} className="flex flex-col gap-2">
                        <legend className="mb-2 font-display text-sm uppercase tracking-wide">
                            {option.name}
                            {p.selection[option.id] === undefined && <span className="ms-2 font-sans text-xs normal-case tracking-normal text-muted-foreground">— {t('SELECT_OPTION', {option: option.name})}</span>}
                        </legend>
                        <div className="flex flex-wrap gap-2" role="radiogroup" aria-label={option.name}>
                            {option.values.map((value, i) => {
                                const selected = p.selection[option.id] === value.id;
                                const available = p.isValueAvailable(option, value);
                                const label = value.name || value.code;
                                return (
                                    <button key={value.id} type="button" role="radio" aria-checked={selected} onClick={() => p.select(option.id, value.id)}
                                            aria-label={available ? label : `${label} — ${t('UNAVAILABLE_COMBINATION')}`}
                                            className={cn('strip min-w-10 justify-center text-sm', i % 2 ? '[--tilt:0.6deg]' : '[--tilt:-0.5deg]',
                                                selected ? 'strip-on' : 'strip-hover',
                                                !available && 'text-muted-foreground line-through opacity-70')}>
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
                                     decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}
                                     className="[&_button]:rounded-none [&_button]:border-foreground/40"/>
                    <button type="button" className="glo h-12 flex-1 px-6 text-base" disabled={!p.canAdd} onClick={p.addToCart}>
                        <ShoppingBagIcon className="size-5"/>
                        {p.status === 'adding' ? t('ADDING') : p.isOutOfStock ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                    </button>
                </div>
                {mounted && p.inCartQuantity > 0 && <p className="text-sm text-muted-foreground" aria-live="polite">{t('IN_CART', {count: p.inCartQuantity})}</p>}
                {!p.allSelected && !p.isOutOfStock && (
                    <p className="text-sm text-muted-foreground">{t('OPTION_REQUIRED', {option: p.options.find(o => p.selection[o.id] === undefined)?.name ?? ''})}</p>
                )}
            </div>
        </div>
    );
}
