'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {PlusIcon} from 'lucide-react';
import type {Product, StoreContext} from '@store-front/types';
import {useProductPurchase} from '@store-front/hooks/use-product-purchase';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {cn} from '@store-front/ui/lib/utils';
import {Gallery} from './Gallery';
import {TagButton} from '../components/TagButton';

/**
 * The item card, printed: quoted name in display caps, a facts plate (brand · SKU · stock), the price plate,
 * option values as plates where the chosen one wears the tag and unavailable ones are struck, then the
 * quantity plates and the add tag. The gallery window sits on the start side.
 */
export function BuyBox({product, storeContext}: { product: Product; storeContext: StoreContext }) {
    const t = useTranslations('PAGE.PRODUCT');
    const p = useProductPurchase(storeContext, product);
    const [swing, setSwing] = useState(false);
    const facts: [string, string | undefined][] = [
        [t('SPECIFICATIONS'), product.manufacturer?.description?.name],
        [t('SKU'), p.sku],
    ];
    return (
        <div className="grid grid-cols-[minmax(0,1fr)] gap-6 lg:grid-cols-[minmax(0,11fr)_minmax(0,9fr)] lg:items-start lg:gap-8">
            <Gallery images={p.images} alt={product.description?.name ?? ''}/>
            <div className="flex flex-col gap-5">
                <h1 className="font-display text-3xl font-bold uppercase leading-[0.95] tracking-tight [overflow-wrap:anywhere] sm:text-5xl"><span className="q" dir="auto"><bdi>{product.description?.name}</bdi></span></h1>

                <dl className="plate grid grid-cols-[max-content_1fr] font-mono text-xs uppercase tracking-wide">
                    {facts.filter(([, v]) => v).map(([k, v]) => (
                        <div key={k} className="contents">
                            <dt className="border-b border-e border-foreground px-3 py-1.5 text-muted-foreground last-of-type:border-b-0">{k}</dt>
                            <dd className="border-b border-foreground px-3 py-1.5" dir="ltr">{v}</dd>
                        </div>
                    ))}
                    <div className="contents">
                        <dt className="border-e border-foreground px-3 py-1.5 text-muted-foreground">{t('IN_STOCK')}</dt>
                        <dd className="px-3 py-1.5">
                            {p.isOutOfStock ? <span className="q bg-foreground px-1.5 text-background">{t('OUT_OF_STOCK')}</span>
                                : p.maxQty <= 5 ? <span>{t('LOW_STOCK', {count: p.maxQty})}</span>
                                    : <span className="text-success">{t('IN_STOCK')}</span>}
                        </dd>
                    </div>
                </dl>

                <p className="flex items-baseline gap-3 font-mono">
                    <span className="text-3xl font-bold leading-none tracking-tight">{p.price.finalPrice}</span>
                    {p.price.discounted && p.price.originalPrice && <del className="text-base text-muted-foreground">{p.price.originalPrice}</del>}
                </p>

                {p.options.map(option => (
                    <fieldset key={option.id} className="flex flex-col gap-2">
                        <legend className="q mb-2 font-display text-sm font-semibold uppercase tracking-wide">
                            {option.name}{p.selection[option.id] === undefined && <span className="ms-2 font-mono text-[0.7rem] font-normal normal-case tracking-wide text-muted-foreground">— {t('SELECT_OPTION', {option: option.name})}</span>}
                        </legend>
                        <div className="flex flex-wrap" role="radiogroup" aria-label={option.name}>
                            {option.optionValues.map(value => {
                                const selected = p.selection[option.id] === value.id;
                                const available = p.isValueAvailable(option, value);
                                const label = value.name || value.code;
                                return (
                                    <button key={value.id} type="button" role="radio" aria-checked={selected} onClick={() => p.select(option.id, value.id)}
                                            aria-label={available ? label : `${label} — ${t('UNAVAILABLE_COMBINATION')}`}
                                            className={cn('-ms-px -mt-px min-w-11 px-3 py-2 font-mono text-xs uppercase tracking-wide transition-colors duration-(--motion-fast)',
                                                selected ? 'tag z-10 rounded-none' : 'plate hover:bg-foreground hover:text-background', !available && !selected && 'struck')}>
                                        {label}
                                    </button>
                                );
                            })}
                        </div>
                    </fieldset>
                ))}

                <div className="flex flex-wrap items-center gap-3 border-t border-foreground pt-5">
                    <QuantityStepper value={p.quantity} onDecrement={p.decrementQuantity} onIncrement={p.incrementQuantity} canDecrement={p.canDecrease} canIncrement={p.canIncrease}
                                     decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')} className="[&_button]:rounded-none [&_button]:border-foreground"/>
                    <TagButton size="lg" className="flex-1" swing={swing} disabled={!p.canAdd} onClick={() => { setSwing(false); requestAnimationFrame(() => setSwing(true)); p.addToCart(); }}>
                        <PlusIcon className="size-4"/>{p.status === 'adding' ? t('ADDING') : p.isOutOfStock ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                    </TagButton>
                </div>
                {p.inCartQuantity > 0 && <p className="font-mono text-xs uppercase tracking-wide text-muted-foreground">{t('IN_CART', {count: p.inCartQuantity})}</p>}
                {!p.allSelected && !p.isOutOfStock && <p className="font-mono text-xs uppercase tracking-wide text-muted-foreground">{t('OPTION_REQUIRED', {option: p.options.find(o => p.selection[o.id] === undefined)?.name ?? ''})}</p>}
            </div>
        </div>
    );
}
