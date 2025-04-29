'use client'
import {Product} from "@/types/product-groups";
import {Swiper, SwiperSlide} from 'swiper/react';
import {EffectCoverflow, Navigation, Pagination} from "swiper/modules";
import {StoreContext} from "@/types/store-context";
import ProductItem from "@/app/_themes/DEFAULT/componantes/ProductItem/ProductItem";
import {useState} from "react";

export default function ProductSwiperGrid({storeContext, products}: {
    storeContext: StoreContext
    products: Product[]
}) {
    const [swiperLoaded, setSwiperLoaded] = useState(false);

    return (
        <div className={`relative flex items-center justify-center min-h-[550px]`}>

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
                className={`coverflow w-full h-full transition-opacity duration-500 ${swiperLoaded ? 'opacity-100' : 'opacity-0'}`}
                onSwiper={(swiper) => {
                    setTimeout(() => {
                        setSwiperLoaded(true);
                    }, 100);
                }}
            >
                {products.map((product, index) =>
                    <SwiperSlide key={product.id} className="flex justify-center">
                        <ProductItem
                            storeContext={storeContext}
                            product={product}
                        />
                    </SwiperSlide>
                )}
            </Swiper>
        </div>
    );
}