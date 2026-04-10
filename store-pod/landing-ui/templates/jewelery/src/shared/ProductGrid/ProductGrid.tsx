import * as React from "react";
import {Product} from "@/types/product-groups";
import ProductItem from "@/shared/ProductItem/ProductItem";
import {StoreContext} from "@/types/store-context";
import {cn} from "@/lib/utils";

export interface ProductGridProps extends React.HTMLAttributes<HTMLDivElement> {
    storeContext: StoreContext;
    products: Product[];
    emptyStateMessage?: string;
}

const ProductGrid = React.forwardRef<HTMLDivElement, ProductGridProps>(
    ({storeContext, products, className, emptyStateMessage, ...props}, ref) => {
        if (!products || products.length === 0) {
            return (
                <div
                    ref={ref}
                    className={cn("flex items-center justify-center py-20 text-muted-foreground", className)}
                    {...props}
                >
                    <p className="text-xs tracking-widest uppercase">{emptyStateMessage || "No products found."}</p>
                </div>
            );
        }

        return (
            <div
                ref={ref}
                className={cn("grid grid-cols-2 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 sm:gap-6 lg:gap-8", className)}
                {...props}
            >
                {products.map((product) => (
                    <ProductItem key={product.id} storeContext={storeContext} product={product}/>
                ))}
            </div>
        );
    }
);
ProductGrid.displayName = "ProductGrid";

export default ProductGrid;
