import {useTranslations} from 'next-intl';
import type {Product} from '@store-front/types';
import {discountPercent, isLowStock, isOnSale, isOutOfStock} from '@store-front/services/product-presenter';

/** State as a printed stamp over the photo: SALE (with % when derivable), OUT OF STOCK, ONLY N LEFT. In stock is silence here. */
export function ProductBadges({product, className}: { product: Product; className?: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const percent = discountPercent(product);
    const out = isOutOfStock(product);
    const sale = isOnSale(product) && !out;
    const low = !out && isLowStock(product);
    if (!out && !sale && !low) return null;
    return (
        <div className={className}>
            {sale && <span className="stamp stamp-sale">{percent ? t('SAVE_PERCENT', {percent}) : t('SALE')}</span>}
            {out && <span className="stamp">{t('OUT_OF_STOCK')}</span>}
            {low && <span className="stamp stamp-outline">{t('LOW_STOCK', {count: product.quantity})}</span>}
        </div>
    );
}
