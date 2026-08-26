import {useTranslations} from 'next-intl';
import type {Product} from '@store-front/types';
import {discountPercent, isLowStock, isOnSale, isOutOfStock} from '@store-front/services/product-presenter';
import {cn} from '@store-front/ui/lib/utils';
import {ArrowMark} from './Marks';

/**
 * What the issue prints over a die-cut. Sale is a flag (the same pennant the price rides on); a last few
 * units is a marker annotation with an arrow; sold out is an ink flag over a greyed picture. In stock is
 * silence — the magazine does not print what is unremarkable.
 */
export function ProductBadges({product, className, size = 'md'}: { product: Product; className?: string; size?: 'sm' | 'md' }) {
    const t = useTranslations('PAGE.PRODUCT');
    const percent = discountPercent(product);
    const out = isOutOfStock(product);
    const sale = isOnSale(product) && !out;
    const low = !out && isLowStock(product);
    if (!out && !sale && !low) return null;
    return (
        <div className={cn('pointer-events-none flex flex-col items-start gap-1.5', className)}>
            {sale && (
                <span className={cn('flag flag-sale', size === 'md' && 'text-base')}>
                    {percent ? t('SAVE_PERCENT', {percent}) : t('SALE')}
                </span>
            )}
            {out && <span className="flag flag-ink">{t('OUT_OF_STOCK')}</span>}
            {low && (
                <span className="marker">
                    <ArrowMark className="h-3.5 w-6 shrink-0 rtl:-scale-x-100"/>
                    {t('LOW_STOCK', {count: product.quantity})}
                </span>
            )}
        </div>
    );
}
