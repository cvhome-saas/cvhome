'use client'
import * as React from "react";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import ProductItem from "@/shared/ProductItem/ProductItem";
import {cn} from "@/lib/utils";

export interface ProductHorizontalScrollProps extends React.HTMLAttributes<HTMLDivElement> {
    storeContext: StoreContext;
    products: Product[];
    title?: string;
}

const ProductHorizontalScroll = React.forwardRef<HTMLDivElement, ProductHorizontalScrollProps>(
    ({storeContext, products, className, title, ...props}, ref) => {
        if (!products || products.length === 0) return null;

        return (
            <div ref={ref} className={cn("space-y-4 w-full overflow-hidden", className)} {...props}>
                {title && <h2 className="text-2xl font-bold tracking-tight">{title}</h2>}
                <div className="flex w-full gap-6 overflow-x-auto pb-6 scrollbar-hide snap-x snap-mandatory">
                    {products.map((product) => (
                        <div key={product.id} className="min-w-[280px] md:min-w-[320px] snap-start">
                            <ProductItem storeContext={storeContext} product={product} className="m-0" />
                        </div>
                    ))}
                </div>
            </div>
        );
    }
);
ProductHorizontalScroll.displayName = "ProductHorizontalScroll";

export default ProductHorizontalScroll;
