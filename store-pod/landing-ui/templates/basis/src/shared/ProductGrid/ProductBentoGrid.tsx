import * as React from "react";
import {Product} from "@/types/product-groups";
import ProductItem from "@/shared/ProductItem/ProductItem";
import {StoreContext} from "@/types/store-context";
import {cn} from "@/lib/utils";

export interface ProductBentoGridProps extends React.HTMLAttributes<HTMLDivElement> {
    storeContext: StoreContext;
    products: Product[];
}

const ProductBentoGrid = React.forwardRef<HTMLDivElement, ProductBentoGridProps>(
    ({storeContext, products, className, ...props}, ref) => {
        if (!products || products.length === 0) return null;

        return (
            <div
                ref={ref}
                className={cn("grid grid-cols-1 md:grid-cols-4 gap-4", className)}
                {...props}
            >
                {products.slice(0, 6).map((product, idx) => {
                    // Create a "bento" effect by spanning certain items
                    const isLarge = idx === 0 || idx === 5;
                    return (
                        <div
                            key={product.id}
                            className={cn(
                                "relative overflow-hidden rounded-xl",
                                isLarge ? "md:col-span-2 md:row-span-2" : "md:col-span-1 md:row-span-1"
                            )}
                        >
                             <ProductItem 
                                storeContext={storeContext} 
                                product={product} 
                                // We might want a variant of ProductItem that works better in bento
                                className="m-0 h-full w-full max-w-none" 
                             />
                        </div>
                    );
                })}
            </div>
        );
    }
);
ProductBentoGrid.displayName = "ProductBentoGrid";

export default ProductBentoGrid;
