import {useTranslations} from 'next-intl';
import type {Product} from '@store-front/types';
import {discountPercent, isLowStock, isOnSale, isOutOfStock} from '@store-front/services/product-presenter';

/**
 * State printed in outline, in the line itself — never a floating badge over a photo and never a toast.
 * Only the signals that change a decision: sale (with % when derivable), sold out, last few. In stock is
 * silence, the way a menu prints nothing beside a dish it simply has.
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
            {sale && <span className="mark mark-sale">{percent ? t('SAVE_PERCENT', {percent}) : t('SALE')}</span>}
            {out && <span className="mark mark-out">{t('OUT_OF_STOCK')}</span>}
            {low && <span className="mark">{t('LOW_STOCK', {count: product.quantity})}</span>}
        </div>
    );
}
