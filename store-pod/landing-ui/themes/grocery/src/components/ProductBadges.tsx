import {useTranslations} from 'next-intl';
import type {Product} from '@store-front/types';
import {discountPercent, isLowStock, isOnSale, isOutOfStock} from '@store-front/services/product-presenter';

/** State prints itself: SALE (with % when derivable), OUT OF STOCK, ONLY N LEFT — stickers slapped on the crate. */
export function ProductBadges({product, className}: { product: Product; className?: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const percent = discountPercent(product);
    const out = isOutOfStock(product);
    return (
        <div className={className} aria-hidden={!out && !isOnSale(product) && !isLowStock(product)}>
            {isOnSale(product) && !out && (
                <span className="sticker sticker-sale" data-tilt>{percent ? t('SAVE_PERCENT', {percent}) : t('SALE')}</span>
            )}
            {out && <span className="sticker" data-tilt>{t('OUT_OF_STOCK')}</span>}
            {!out && isLowStock(product) && <span className="sticker sticker-outline" data-tilt="alt">{t('LOW_STOCK', {count: product.quantity})}</span>}
        </div>
    );
}
