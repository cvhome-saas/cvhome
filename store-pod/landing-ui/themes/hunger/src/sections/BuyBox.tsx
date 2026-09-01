'use client'
import {useTranslations} from 'next-intl';
import type {Product, StoreContext} from '@store-front/types';
import {useProductPurchase, type ProductPurchase} from '@store-front/hooks/use-product-purchase';
import {Price} from '@store-front/ui/price';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {cn} from '@store-front/ui/lib/utils';
import {Gallery} from './Gallery';

/**
 * The dish, written out in full: order number, name at printing scale, the price in the same column the
 * menu uses, then every choice as a full-width printed row — the selected one lights across its whole
 * width. Owns the gallery too, because the shown pictures follow the selected variant.
 */
/** The first axis with nothing chosen — what the "please choose" line names. */
const unpicked = (p: ProductPurchase) => p.options.find(o => p.selection[o.id] === undefined);

export function BuyBox({product, storeContext, layout = 'split'}: { product: Product; storeContext: StoreContext; layout?: 'split' | 'stacked' }) {
    const t = useTranslations('PAGE.PRODUCT');
    const p = useProductPurchase(storeContext, product);

    return (
        <div className={cn('grid gap-8', layout === 'split' && 'lg:grid-cols-2 lg:items-start lg:gap-12')}>
            <Gallery images={p.images} alt={product.description?.name ?? ''}/>
            <div className="flex flex-col gap-6">
                <div className="flex flex-col gap-3">
                    <div className="flex flex-wrap items-center gap-2">
                        {p.sku && <span className="dish-no" dir="ltr">{p.sku}</span>}
                        {product.manufacturer?.description?.name && (
                            <span className="press text-xs tracking-wide text-muted-foreground">{product.manufacturer.description.name}</span>
                        )}
                    </div>
                    <h1 className="press text-4xl leading-none sm:text-5xl">{product.description?.name}</h1>
                    <div className="flex flex-wrap items-baseline justify-between gap-x-6 gap-y-2 border-y-2 border-foreground py-2.5">
                        <Price className="price text-3xl" size="lg" finalPrice={p.price.finalPrice} originalPrice={p.price.originalPrice} discounted={p.price.discounted}/>
                        {p.isOutOfStock ? <span className="mark mark-out">{t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK')}</span>
                            : p.maxQty <= 5 ? <span className="mark">{t('LOW_STOCK', {count: p.maxQty})}</span>
                                : <span className="mark mark-offer">{t('IN_STOCK')}</span>}
                    </div>
                </div>

                {p.options.map(option => (
                    <fieldset key={option.id} className="flex flex-col gap-2">
                        <legend className="press w-full border-b border-foreground pb-1 text-sm tracking-wide">
                            {option.name || option.code}
                            {p.selection[option.id] === undefined && (
                                <span className="ms-2 font-sans text-xs font-normal normal-case tracking-normal text-muted-foreground">
                                    {t('SELECT_OPTION', {option: option.name || option.code})}
                                </span>
                            )}
                        </legend>
                        <div className="flex flex-col" role="radiogroup" aria-label={option.name || option.code}>
                            {option.values.map(value => {
                                const selected = p.selection[option.id] === value.id;
                                const available = p.isValueAvailable(option, value);
                                const label = value.name || value.code;
                                return (
                                    <button key={value.id} type="button" role="radio" aria-checked={selected} onClick={() => p.select(option.id, value.id)}
                                            aria-label={available ? label : `${label} — ${t('UNAVAILABLE_COMBINATION')}`}
                                            aria-disabled={!available}
                                            className={cn('press -mt-px flex items-center justify-between gap-3 border border-foreground px-3 py-2 text-start text-sm transition-colors duration-(--motion-fast)',
                                                selected ? 'plate border-primary' : 'hover:bg-[var(--wash)]',
                                                !available && 'border-dashed text-muted-foreground line-through')}>
                                        <span>{label}</span>
                                        
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
                    <button type="button" disabled={!p.canAdd} onClick={p.addToCart}
                            className="fold plate h-12 flex-1 justify-center border-primary text-base disabled:opacity-50">
                        {p.status === 'adding' ? t('ADDING') : p.isOutOfStock ? t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK') : t('ADD_TO_CART')}
                    </button>
                </div>
                {p.inCartQuantity > 0 && <p className="press text-xs tracking-wide text-primary">{t('IN_CART', {count: p.inCartQuantity})}</p>}
                {!p.allSelected && !p.isOutOfStock && (
                    <p className="text-sm text-muted-foreground">{t('OPTION_REQUIRED', {option: unpicked(p)?.name || unpicked(p)?.code || ''})}</p>
                )}
            </div>
        </div>
    );
}
