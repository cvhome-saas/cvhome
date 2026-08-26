import type {Product, StoreContext} from '@store-front/types';
import {ProductCard} from '../components/ProductCard';

/**
 * A short printed list — "with this, people order" under a dish. Not a carousel: on a menu, a suggestion
 * you have to scroll sideways to find is a suggestion nobody reads.
 */
export function ProductRail({products, storeContext}: { products: Product[]; storeContext: StoreContext }) {
    if (products.length === 0) return null;
    return (
        <ul className="grid border-t border-border">
            {products.slice(0, 6).map(p => (
                <li key={p.id}><ProductCard product={p} storeContext={storeContext}/></li>
            ))}
        </ul>
    );
}
