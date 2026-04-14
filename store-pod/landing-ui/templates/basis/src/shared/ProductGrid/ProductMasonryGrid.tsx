import * as React from "react";
import {Product} from "@/types/product-groups";
import ProductItem from "@/shared/ProductItem/ProductItem";
import {StoreContext} from "@/types/store-context";
import {cn} from "@/lib/utils";

export interface ProductMasonryGridProps extends React.HTMLAttributes<HTMLDivElement> {
    storeContext: StoreContext;
    products: Product[];
}

const ProductMasonryGrid = React.forwardRef<HTMLDivElement, ProductMasonryGridProps>(
    ({storeContext, products, className, ...props}, ref) => {
        if (!products || products.length === 0) return null;

        return (
            <div
                ref={ref}
                className={cn("columns-1 sm:columns-2 lg:columns-3 xl:columns-4 gap-6 space-y-6", className)}
                {...props}
            >
                {products.map((product) => (
                    <div key={product.id} className="break-inside-avoid">
                        <ProductItem storeContext={storeContext} product={product} className="m-0" />
                    </div>
                ))}
            </div>
        );
    }
);
ProductMasonryGrid.displayName = "ProductMasonryGrid";

export default ProductMasonryGrid;
