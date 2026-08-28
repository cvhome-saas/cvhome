import {useTranslations} from 'next-intl';
import type {Product} from '@store-front/types';
import {discountPercent, isLowStock, isOnSale, isOutOfStock} from '@store-front/services/product-presenter';
import {cn} from '@store-front/ui/lib/utils';

/** State as overprints: a SALE stamp (with % when derivable), a SOLD OUT stamp, an ONLY N LEFT stamp. In stock is silence. */
export function ProductBadges({product, className, size}: { product: Product; className?: string; size?: 'lg' }) {
    const t = useTranslations('PAGE.PRODUCT');
    const percent = discountPercent(product);
    const out = isOutOfStock(product);
    const sale = isOnSale(product) && !out;
    const low = !out && isLowStock(product);
    if (!out && !sale && !low) return null;
    return (
        <div className={className}>
            {sale && <span className={cn('stamp stamp-sale', size === 'lg' && 'stamp-lg')}>{percent ? t('SAVE_PERCENT', {percent}) : t('SALE')}</span>}
            {out && <span className={cn('stamp', size === 'lg' && 'stamp-lg')}>{t('OUT_OF_STOCK')}</span>}
            {low && <span className={cn('stamp', size === 'lg' && 'stamp-lg')}>{t('LOW_STOCK', {count: product.quantity})}</span>}
        </div>
    );
}
