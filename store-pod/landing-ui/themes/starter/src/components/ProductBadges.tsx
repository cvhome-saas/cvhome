import {useTranslations} from 'next-intl';
import {Badge} from '@store-front/ui/badge';
import type {Product} from '@store-front/types';
import {discountPercent, isLowStock, isOnSale, isOutOfStock} from '@store-front/services/product-presenter';

/** Only the signals that matter: sale (with % when derivable), out of stock, low stock. In-stock is silence. */
export function ProductBadges({product, className}: { product: Product; className?: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const percent = discountPercent(product);
    const out = isOutOfStock(product);
    return (
        <div className={className} aria-hidden={!out && !isOnSale(product) && !isLowStock(product)}>
            {isOnSale(product) && !out && (
                <Badge className="rounded-badge border-transparent bg-sale text-sale-foreground">
                    {percent ? t('SAVE_PERCENT', {percent}) : t('SALE')}
                </Badge>
            )}
            {out && <Badge variant="secondary" className="rounded-badge">{t('OUT_OF_STOCK')}</Badge>}
            {!out && isLowStock(product) && <Badge variant="outline" className="rounded-badge">{t('LOW_STOCK', {count: product.quantity})}</Badge>}
        </div>
    );
}
