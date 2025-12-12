'use client'
import * as React from "react";
import {Swiper, SwiperSlide} from 'swiper/react';
import {EffectCoverflow, Navigation, Pagination} from "swiper/modules";

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
                    <p>{emptyStateMessage || "No products found."}</p>
                </div>
            );
        }

        return (
            <div
                ref={ref}
                className={cn("relative flex items-center justify-center min-h-[550px]", className)}
                {...props}
            >
                <Swiper
                    modules={[EffectCoverflow, Pagination, Navigation]}
                    slidesPerView={3}
                    spaceBetween={30}
                    navigation={true}
                    pagination={{clickable: true}}
                    breakpoints={{
                        320: {slidesPerView: 1, spaceBetween: 20},
                        640: {slidesPerView: 1, spaceBetween: 20},
                        768: {slidesPerView: 2, spaceBetween: 30},
                        1024: {slidesPerView: 3, spaceBetween: 40},
                    }}
                    effect={'coverflow'}
                    loop={true}
                    centeredSlides={true}
                    grabCursor={true}
                    coverflowEffect={{
                        rotate: 0,
                        stretch: 0,
                        depth: 100,
                        modifier: 1,
                        slideShadows: false,
                    }}
                    className="coverflow w-full h-full opacity-0 transition-opacity duration-500"
                    onSwiper={(swiper) => {
                        // A small timeout ensures styles are applied before fading in, preventing a flash of unstyled content.
                        setTimeout(() => {
                            swiper.el.style.opacity = '1';
                        }, 0);
                    }}
                >
                    {products.map((product) =>
                        <SwiperSlide key={product.id} className="flex justify-center">
                            <ProductItem storeContext={storeContext} product={product}/>
                        </SwiperSlide>
                    )}
                </Swiper>
            </div>
        );
    }
);
ProductSwiperGrid.displayName = "ProductSwiperGrid";

export default ProductSwiperGrid;