'use client'
import {useTranslations} from 'next-intl';
import {ShoppingBagIcon} from 'lucide-react';
import type {Product, StoreContext} from '@store-front/types';
import {useProductPurchase} from '@store-front/hooks/use-product-purchase';
import {Button} from '@store-front/ui/button';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {cn} from '@store-front/ui/lib/utils';
import {Gallery} from './Gallery';
import {useMounted} from '../components/use-mounted';

/**
 * The entry at full size: gallery · maker · name · the price in the condensed voice with its stock slot and
 * catalogue number · option rows as chips · quantity · the one primary action. Owns the gallery because the
 * shown images follow the selected variant.
 */
export function BuyBox({product, storeContext, layout = 'split'}: { product: Product; storeContext: StoreContext; layout?: 'split' | 'stacked' }) {
    const t = useTranslations('PAGE.PRODUCT');
    const tp = useTranslations('COMPONENTS.PRODUCT');
    const p = useProductPurchase(storeContext, product);
    const mounted = useMounted();  // the cart lives in the browser: its facts render after hydration

    return (
        <div className={cn('grid gap-6 lg:gap-12', layout === 'split' && 'lg:grid-cols-2 lg:items-start')}>
            <Gallery images={p.images} alt={product.description?.name ?? ''}/>
            <div className="flex flex-col gap-6">
                <div className="flex flex-col gap-3 border-b pb-5">
                    <h1 className="font-sans text-2xl font-semibold leading-snug tracking-tight sm:text-3xl" dir="auto"><bdi>{product.description?.name}</bdi></h1>
                    <div className="flex flex-wrap items-end gap-x-4 gap-y-2">
                        <p className="flex flex-col">
                            {p.price.discounted && p.price.originalPrice && <del className="text-sm tabular-nums text-muted-foreground">{p.price.originalPrice}</del>}
                            <span className="price text-4xl sm:text-5xl">{p.price.finalPrice}</span>
                        </p>
                        <p className="mb-1">
                            {p.isOutOfStock ? <span className="stamp">{t('OUT_OF_STOCK')}</span>
                                : p.maxQty <= 5 ? <span className="stamp stamp-outline">{t('LOW_STOCK', {count: p.maxQty})}</span>
                                    : <span className="text-sm font-semibold text-success">{t('IN_STOCK')}</span>}
                        </p>
                    </div>
                    {(p.sku || product.manufacturer?.description?.name) && (
                        <p className="cat-no flex flex-wrap gap-x-2">
                            {product.manufacturer?.description?.name && <bdi dir="auto">{product.manufacturer.description.name}</bdi>}
                            {p.sku && product.manufacturer?.description?.name && <span aria-hidden>·</span>}
                            {p.sku && <span>{tp('CATALOGUE_NO')} <span dir="ltr">{p.sku}</span></span>}
                        </p>
                    )}
                </div>

                {p.options.map(option => (
                    <fieldset key={option.id} className="flex flex-col gap-2">
                        <legend className="mb-2 text-sm font-semibold">
                            {option.name}
                            {p.selection[option.id] === undefined && <span className="ms-2 font-normal text-muted-foreground">— {t('SELECT_OPTION', {option: option.name})}</span>}
                        </legend>
                        <div className="flex flex-wrap gap-2" role="radiogroup" aria-label={option.name}>
                            {option.optionValues.map(value => {
                                const selected = p.selection[option.id] === value.id;
                                const available = p.isValueAvailable(option, value);
                                const label = value.name || value.code;
                                return (
                                    <button key={value.id} type="button" role="radio" aria-checked={selected} onClick={() => p.select(option.id, value.id)}
                                            aria-label={available ? label : `${label} — ${t('UNAVAILABLE_COMBINATION')}`}
                                            className={cn('chip min-w-10 justify-center', !available && 'chip-off')}>
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
                    <Button size="lg" className="h-12 flex-1 text-base font-semibold" disabled={!p.canAdd} onClick={p.addToCart}>
                        <ShoppingBagIcon data-icon="inline-start"/>
                        {p.status === 'adding' ? t('ADDING') : p.isOutOfStock ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                    </Button>
                </div>
                {mounted && p.inCartQuantity > 0 && <p className="text-sm tabular-nums text-muted-foreground" aria-live="polite">{t('IN_CART', {count: p.inCartQuantity})}</p>}
                {!p.allSelected && !p.isOutOfStock && (
                    <p className="text-sm text-muted-foreground">{t('OPTION_REQUIRED', {option: p.options.find(o => p.selection[o.id] === undefined)?.name ?? ''})}</p>
                )}
            </div>
        </div>
    );
}
