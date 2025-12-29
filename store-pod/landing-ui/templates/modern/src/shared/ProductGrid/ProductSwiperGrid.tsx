'use client'
import * as React from "react";
import {Swiper, SwiperSlide} from 'swiper/react';
import {Navigation, Pagination, A11y} from "swiper/modules";

import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import ProductItem from "@/shared/ProductItem/ProductItem";
import {cn} from "@/lib/utils";

export interface ProductSwiperGridProps extends React.HTMLAttributes<HTMLDivElement> {
    storeContext: StoreContext;
    products: Product[];
    emptyStateMessage?: string;
}

const ProductSwiperGrid = React.forwardRef<HTMLDivElement, ProductSwiperGridProps>(
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
                className={cn("relative", className)}
                {...props}
            >
                <div className="pointer-events-none absolute inset-y-0 start-0 z-10 w-10 bg-linear-to-r from-background to-transparent"/>
                <div className="pointer-events-none absolute inset-y-0 end-0 z-10 w-10 bg-linear-to-l from-background to-transparent"/>

                <Swiper
                    modules={[Navigation, Pagination, A11y]}
                    navigation
                    pagination={{type: "progressbar"}}
                    loop={products.length > 4}
                    watchOverflow
                    grabCursor
                    centeredSlides={false}
                    spaceBetween={16}
                    slidesPerView={1.15}
                    breakpoints={{
                        480: {slidesPerView: 1.4, spaceBetween: 16},
                        640: {slidesPerView: 2.1, spaceBetween: 18},
                        768: {slidesPerView: 2.4, spaceBetween: 18},
                        1024: {slidesPerView: 3.1, spaceBetween: 22},
                        1280: {slidesPerView: 4, spaceBetween: 24},
                    }}
                    className="coverflow w-full"
                >
                    {products.map((product) => (
                        <SwiperSlide key={product.id} className="pb-10">
                            <div className="px-1">
                                <ProductItem storeContext={storeContext} product={product}/>
                            </div>
                        </SwiperSlide>
                    ))}
                </Swiper>
            </div>
        );
    }
);

ProductSwiperGrid.displayName = "ProductSwiperGrid";

export default ProductSwiperGrid;