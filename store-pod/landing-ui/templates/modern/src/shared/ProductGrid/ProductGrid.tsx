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
                    className={cn("flex items-center justify-center rounded-2xl border border-border bg-background py-16 text-muted-foreground", className)}
                    {...props}
                >
                    <p className="text-sm">{emptyStateMessage || "No products found."}</p>
                </div>
            );
        }

        return (
            <div
                ref={ref}
                className={cn(
                    "grid grid-cols-2 gap-x-4 gap-y-8 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 xl:gap-x-6",
                    className
                )}
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