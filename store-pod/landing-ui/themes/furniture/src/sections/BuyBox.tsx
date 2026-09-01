'use client'
import type {ReactNode} from 'react';
import {useTranslations} from 'next-intl';
import type {Product, StoreContext} from '@store-front/types';
import {useProductPurchase, type ProductPurchase} from '@store-front/hooks/use-product-purchase';
import {Button} from '@store-front/ui/button';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {cn} from '@store-front/ui/lib/utils';
import {Figure} from '../components/Figure';
import {StatePlate} from '../components/StatePlate';
import {Gallery} from './Gallery';

/**
 * The buying column: the name, the price on the largest enamel plate on the page, then one record rule
 * carrying availability, maker and catalogue number, the options as small signs, and the single enamel
 * action. The maker sits in that rule rather than as a label above the name — a line over a heading is a
 * kicker, and the heading carries its own weight. Owns the gallery too, because the shown views follow
 * the selected variant.
 */
/** The first axis with nothing chosen — what the "please choose" line names. */
const unpicked = (p: ProductPurchase) => p.options.find(o => p.selection[o.id] === undefined);

export function BuyBox({product, storeContext, layout = 'split', floorTag}: {
    product: Product; storeContext: StoreContext; layout?: 'split' | 'stacked';
    /** Which floor of the building this piece stands on — the same number the directory board gave it. */
    floorTag?: ReactNode;
}) {
    const t = useTranslations('PAGE.PRODUCT');
    const tc = useTranslations('COMMON');
    const p = useProductPurchase(storeContext, product);

    return (
        <div className={cn('grid gap-8 lg:gap-14', layout === 'split' && 'lg:grid-cols-[1.05fr_1fr] lg:items-start')}>
            <Gallery images={p.images} alt={product.description?.name ?? ''}/>

            <div className="flex flex-col gap-7">
                <div className="flex flex-col gap-4">
                    <h1 className="sign-lg text-2xl lg:text-4xl">{product.description?.name}</h1>

                    <div className="flex flex-wrap items-center gap-3">
                        <span className="enamel-flat figure px-3.5 py-2 text-xl lg:text-2xl">
                            <Figure value={p.price.finalPrice ?? ''}/>
                        </span>
                        {p.price.discounted && p.price.originalPrice && (
                            <del className="figure text-base text-muted-foreground">{p.price.originalPrice}</del>
                        )}
                    </div>

                    <div className="rule-brass flex flex-wrap items-center gap-3 border-y py-3 text-sm">
                        {p.isOutOfStock
                            ? <StatePlate tone="quiet">{t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK')}</StatePlate>
                            : p.maxQty <= 5
                                ? <StatePlate tone="ink">{t('LOW_STOCK', {count: p.maxQty})}</StatePlate>
                                : <StatePlate tone="ink">{t('IN_STOCK')}</StatePlate>}
                        {product.manufacturer?.description?.name && (
                            <span className="text-muted-foreground">
                                <span className="sign text-[0.625rem]">{tc('MANUFACTURER')}</span>{' '}
                                <span className="text-sm text-foreground">{product.manufacturer.description.name}</span>
                            </span>
                        )}
                        {p.sku && (
                            <span className="text-muted-foreground">
                                <span className="sign text-[0.625rem]">{t('SKU')}</span>{' '}
                                <span className="figure text-sm text-foreground" dir="ltr">{p.sku}</span>
                            </span>
                        )}
                    </div>
                </div>

                {p.options.map(option => (
                    <fieldset key={option.id} className="flex flex-col gap-3">
                        <legend className="sign mb-1 text-[0.625rem] text-muted-foreground">
                            {option.name || option.code}
                            {p.selection[option.id] === undefined && <span className="ms-2 normal-case tracking-normal">— {t('SELECT_OPTION', {option: option.name || option.code})}</span>}
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
                                            className={cn('sign rule-brass min-w-11 rounded-control border px-3.5 py-2 text-[0.625rem] transition-colors duration-(--motion-fast)',
                                                selected ? 'border-transparent bg-primary text-primary-foreground' : 'hover:bg-secondary',
                                                !available && 'border-dashed text-muted-foreground line-through')}>
                                        {label}
                                    </button>
                                );
                            })}
                        </div>
                    </fieldset>
                ))}

                <div className="flex flex-wrap items-center gap-4">
                    <QuantityStepper value={p.quantity} onDecrement={p.decrementQuantity} onIncrement={p.incrementQuantity}
                                     canDecrement={p.canDecrease} canIncrement={p.canIncrease}
                                     decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}/>
                    <Button size="lg" className="sign h-12 flex-1 text-[0.6875rem]" disabled={!p.canAdd} onClick={p.addToCart}>
                        {p.status === 'adding' ? t('ADDING') : p.isOutOfStock ? t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK') : t('ADD_TO_CART')}
                    </Button>
                </div>
                {p.inCartQuantity > 0 && (
                    <p className="text-sm text-muted-foreground" aria-live="polite">{t('IN_CART', {count: p.inCartQuantity})}</p>
                )}
                {!p.allSelected && !p.isOutOfStock && (
                    <p className="text-sm text-muted-foreground">{t('OPTION_REQUIRED', {option: unpicked(p)?.name || unpicked(p)?.code || ''})}</p>
                )}
                {floorTag}
            </div>
        </div>
    );
}
