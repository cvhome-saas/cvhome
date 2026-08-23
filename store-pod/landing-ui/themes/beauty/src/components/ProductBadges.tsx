import {useTranslations} from 'next-intl';
import type {Product} from '@store-front/types';
import {discountPercent, isLowStock, isOnSale, isOutOfStock} from '@store-front/services/product-presenter';

/** State is a mark: sale = a tag clipped on; out of stock = an ink plate; low stock = a mono note. */
export function ProductBadges({product, className}: { product: Product; className?: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const percent = discountPercent(product);
    const out = isOutOfStock(product);
    return (
        <div className={className}>
            {isOnSale(product) && !out && (
                <span className="tag inline-flex h-6 items-center rounded-control ps-2 font-display text-xs font-semibold uppercase tracking-wide">
                    <span className="q">{percent ? t('SAVE_PERCENT', {percent}) : t('SALE')}</span>
                </span>
            )}
            {out && <span className="inline-flex h-6 items-center bg-foreground px-2 font-mono text-[0.65rem] uppercase tracking-wide text-background"><span className="q">{t('OUT_OF_STOCK')}</span></span>}
            {!out && isLowStock(product) && <span className="plate inline-flex h-6 items-center px-2 font-mono text-[0.65rem] uppercase tracking-wide">{t('LOW_STOCK', {count: product.quantity})}</span>}
        </div>
    );
}
