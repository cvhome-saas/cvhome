'use client'
import {useTranslations} from 'next-intl';
import {ShoppingBagIcon} from 'lucide-react';
import type {Product, StoreContext} from '@store-front/types';
import {useProductPurchase, type ProductPurchase} from '@store-front/hooks/use-product-purchase';
import {Button} from '@store-front/ui/button';
import {Price} from '@store-front/ui/price';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {Badge} from '@store-front/ui/badge';
import {cn} from '@store-front/ui/lib/utils';
import {Gallery} from './Gallery';

/**
 * Title · price · option/variant selector · stock · quantity · add to cart. Owns the gallery too, because
 * the shown images follow the selected variant.
 */
/** The first axis with nothing chosen — what the "please choose" line names. */
const unpicked = (p: ProductPurchase) => p.options.find(o => p.selection[o.id] === undefined);

export function BuyBox({product, storeContext, layout = 'split'}: { product: Product; storeContext: StoreContext; layout?: 'split' | 'stacked' }) {
    const t = useTranslations('PAGE.PRODUCT');
    const p = useProductPurchase(storeContext, product);

    return (
        <div className={cn('grid gap-8', layout === 'split' && 'lg:grid-cols-2 lg:items-start')}>
            <Gallery images={p.images} alt={product.description?.name ?? ''}/>
            <div className="flex flex-col gap-6">
                <div className="flex flex-col gap-2">
                    {product.manufacturer?.description?.name && <p className="text-sm text-muted-foreground">{product.manufacturer.description.name}</p>}
                    <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">{product.description?.name}</h1>
                    <Price size="lg" finalPrice={p.price.finalPrice} originalPrice={p.price.originalPrice} discounted={p.price.discounted}/>
                    <div className="flex flex-wrap items-center gap-2 text-sm">
                        {p.isOutOfStock ? <Badge variant="secondary">{t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK')}</Badge>
                            : p.maxQty <= 5 ? <Badge variant="outline">{t('LOW_STOCK', {count: p.maxQty})}</Badge>
                                : <span className="text-success">{t('IN_STOCK')}</span>}
                        {p.sku && <span className="text-muted-foreground">{t('SKU')}: <span dir="ltr">{p.sku}</span></span>}
                    </div>
                </div>

                {p.options.map(option => (
                    <fieldset key={option.id} className="flex flex-col gap-2">
                        <legend className="text-sm font-medium">
                            {option.name || option.code}
                            {p.selection[option.id] === undefined && <span className="ms-2 font-normal text-muted-foreground">— {t('SELECT_OPTION', {option: option.name || option.code})}</span>}
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
                                            className={cn('min-w-10 rounded-control border px-3 py-1.5 text-sm transition-colors duration-(--motion-fast)',
                                                selected ? 'border-primary bg-primary text-primary-foreground' : 'hover:bg-muted',
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
                    <Button size="lg" className="flex-1" disabled={!p.canAdd} onClick={p.addToCart}>
                        <ShoppingBagIcon data-icon="inline-start"/>
                        {p.status === 'adding' ? t('ADDING') : p.isOutOfStock ? t(p.unresolved ? 'UNAVAILABLE_COMBINATION' : 'OUT_OF_STOCK') : t('ADD_TO_CART')}
                    </Button>
                </div>
                {p.inCartQuantity > 0 && <p className="text-sm text-muted-foreground">{t('IN_CART', {count: p.inCartQuantity})}</p>}
                {!p.allSelected && !p.isOutOfStock && (
                    <p className="text-sm text-muted-foreground">{t('OPTION_REQUIRED', {option: unpicked(p)?.name || unpicked(p)?.code || ''})}</p>
                )}
            </div>
        </div>
    );
}
