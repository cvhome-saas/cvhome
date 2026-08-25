import type {ReactNode} from 'react';
import type {Product, StoreContext} from '@store-front/types';
import type {ThemeLayoutConfig} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';
import {ProductCard} from './ProductCard';

const COLS: Record<number, string> = {1: 'grid-cols-1', 2: 'grid-cols-2', 3: 'grid-cols-3', 4: 'grid-cols-4', 5: 'grid-cols-5', 6: 'grid-cols-6'};
const SM: Record<number, string> = {1: 'sm:grid-cols-1', 2: 'sm:grid-cols-2', 3: 'sm:grid-cols-3', 4: 'sm:grid-cols-4'};
const LG: Record<number, string> = {2: 'lg:grid-cols-2', 3: 'lg:grid-cols-3', 4: 'lg:grid-cols-4', 5: 'lg:grid-cols-5', 6: 'lg:grid-cols-6'};
const XL: Record<number, string> = {2: 'xl:grid-cols-2', 3: 'xl:grid-cols-3', 4: 'xl:grid-cols-4', 5: 'xl:grid-cols-5', 6: 'xl:grid-cols-6'};

/**
 * A stretch of wall: posters on the theme's grid, each with its own small tilt. `lead` pastes one wider
 * sheet (a slider poster) at the start, spanning two columns and two rows.
 */
export function ProductGrid({products, storeContext, grid, className, lead, calm}: {
    products: Product[]; storeContext: StoreContext; grid: ThemeLayoutConfig['productGrid']; className?: string; lead?: ReactNode; calm?: boolean;
}) {
    return (
        <ul className={cn('wall grid gap-x-3 gap-y-5 sm:gap-x-4 sm:gap-y-6', calm && 'wall-calm', COLS[grid.base], SM[grid.sm], LG[grid.lg], XL[grid.xl], className)}>
            {lead && <li className="col-span-2 row-span-2 min-w-0">{lead}</li>}
            {products.map((p, i) => (
                <li key={p.id} className="min-w-0"><ProductCard product={p} storeContext={storeContext} priority={i < 4}/></li>
            ))}
        </ul>
    );
}
