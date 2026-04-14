'use client'
import * as React from "react";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import ProductItem from "@/shared/ProductItem/ProductItem";
import {cn} from "@/lib/utils";

export interface ProductMarqueeProps extends React.HTMLAttributes<HTMLDivElement> {
    storeContext: StoreContext;
    products: Product[];
    reverse?: boolean;
    pauseOnHover?: boolean;
}

const ProductMarquee = React.forwardRef<HTMLDivElement, ProductMarqueeProps>(
    ({storeContext, products, className, reverse = false, pauseOnHover = true, ...props}, ref) => {
        if (!products || products.length === 0) return null;

        // Duplicate items for seamless loop
        const displayProducts = [...products, ...products];

        return (
            <div
                ref={ref}
                className={cn("group flex overflow-hidden p-2 [--gap:1.5rem] [gap:var(--gap)] flex-row", className)}
                {...props}
            >
                <div
                    className={cn(
                        "flex shrink-0 justify-around [gap:var(--gap)] min-w-full animate-marquee flex-row",
                        reverse && "direction-reverse",
                        pauseOnHover && "group-hover:[animation-play-state:paused]"
                    )}
                >
                    {displayProducts.map((product, idx) => (
                        <div key={`${product.id}-${idx}`} className="w-[300px]">
                            <ProductItem storeContext={storeContext} product={product} className="m-0" />
                        </div>
                    ))}
                </div>
            </div>
        );
    }
);
ProductMarquee.displayName = "ProductMarquee";

export default ProductMarquee;
