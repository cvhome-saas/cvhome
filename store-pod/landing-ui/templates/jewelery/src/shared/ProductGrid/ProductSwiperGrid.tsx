'use client'
import * as React from "react";
import {Swiper, SwiperSlide} from 'swiper/react';
import {Navigation, Pagination} from "swiper/modules";

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
                className={cn("relative min-h-[480px] flex items-center", className)}
                {...props}
            >
                <Swiper
                    modules={[Pagination, Navigation]}
                    navigation={true}
                    pagination={{clickable: true}}
                    breakpoints={{
                        320: {slidesPerView: 1, spaceBetween: 16},
                        640: {slidesPerView: 2, spaceBetween: 20},
                        768: {slidesPerView: 2, spaceBetween: 24},
                        1024: {slidesPerView: 3, spaceBetween: 32},
                        1280: {slidesPerView: 4, spaceBetween: 32},
                    }}
                    grabCursor={true}
                    className="coverflow w-full pb-10 opacity-0 transition-opacity duration-500"
                    onSwiper={(swiper) => {
                        setTimeout(() => {
                            swiper.el.style.opacity = '1';
                        }, 0);
                    }}
                >
                    {products.map((product) => (
                        <SwiperSlide key={product.id} className="flex justify-center pb-2">
                            <ProductItem storeContext={storeContext} product={product}/>
                        </SwiperSlide>
                    ))}
                </Swiper>
            </div>
        );
    }
);
ProductSwiperGrid.displayName = "ProductSwiperGrid";

export default ProductSwiperGrid;
