import type {Product, StoreContext} from '@store-front/types';
import type {ThemeLayoutConfig} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';
import {ProductCard} from './ProductCard';

const COLS: Record<number, string> = {1: 'grid-cols-1', 2: 'grid-cols-2', 3: 'grid-cols-3', 4: 'grid-cols-4', 5: 'grid-cols-5', 6: 'grid-cols-6'};
const SM: Record<number, string> = {1: 'sm:grid-cols-1', 2: 'sm:grid-cols-2', 3: 'sm:grid-cols-3', 4: 'sm:grid-cols-4'};
const LG: Record<number, string> = {2: 'lg:grid-cols-2', 3: 'lg:grid-cols-3', 4: 'lg:grid-cols-4', 5: 'lg:grid-cols-5'};
const XL: Record<number, string> = {2: 'xl:grid-cols-2', 3: 'xl:grid-cols-3', 4: 'xl:grid-cols-4', 5: 'xl:grid-cols-5', 6: 'xl:grid-cols-6'};

/**
 * The plate: entries ranged on one ruled sheet, every neighbour sharing its hairline. Columns follow the
 * theme's layout config so every listing in the issue is set to the same measure.
 */
export function ProductGrid({products, storeContext, grid, className}: {
    products: Product[]; storeContext: StoreContext; grid: ThemeLayoutConfig['productGrid']; className?: string
}) {
    return (
        <ul className={cn('plate', COLS[grid.base], SM[grid.sm], LG[grid.lg], XL[grid.xl], className)}>
            {products.map((p, i) => (
                <li key={p.id}><ProductCard product={p} storeContext={storeContext} priority={i < 5}/></li>
            ))}
        </ul>
    );
}
