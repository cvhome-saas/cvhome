import {useTranslations} from 'next-intl';
import type {Product} from '@store-front/types';
import {discountPercent, isLowStock, isOnSale, isOutOfStock} from '@store-front/services/product-presenter';
import {StatePlate} from './StatePlate';

/**
 * Only the signals that matter, and each one printed: sale (with the % when it is derivable), sold out,
 * only-N-left. In stock is silence. Nothing here is carried by colour alone.
 */
export function ProductBadges({product, className}: { product: Product; className?: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const percent = discountPercent(product);
    const out = isOutOfStock(product);
    const sale = isOnSale(product) && !out;
    const low = !out && isLowStock(product);
    if (!out && !sale && !low) return null;
    return (
        <div className={className}>
            {sale && <StatePlate tone="sale">{percent ? t('SAVE_PERCENT', {percent}) : t('SALE')}</StatePlate>}
            {out && <StatePlate tone="quiet">{t('OUT_OF_STOCK')}</StatePlate>}
            {low && <StatePlate tone="ink">{t('LOW_STOCK', {count: product.quantity})}</StatePlate>}
        </div>
    );
}
