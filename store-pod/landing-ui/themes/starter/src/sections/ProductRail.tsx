'use client'
import {Swiper, SwiperSlide} from 'swiper/react';
import {A11y, Navigation} from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/navigation';
import {useDir} from '@store-front/i18n/use-dir';
import type {Product, StoreContext} from '@store-front/types';
import {ProductCard} from '../components/ProductCard';

/** Horizontal product strip (home groups, related products). */
export function ProductRail({products, storeContext}: { products: Product[]; storeContext: StoreContext }) {
    const dir = useDir();
    if (products.length === 0) return null;
    return (
        <Swiper key={dir} dir={dir} modules={[Navigation, A11y]} navigation a11y={{enabled: true}}
                spaceBetween={16} slidesPerView={2} breakpoints={{640: {slidesPerView: 3}, 1024: {slidesPerView: 4}, 1280: {slidesPerView: 5}}}
                className="starter-rail !px-1 !pb-2">
            {products.map(p => (
                <SwiperSlide key={p.id} className="!h-auto">
                    <ProductCard product={p} storeContext={storeContext}/>
                </SwiperSlide>
            ))}
        </Swiper>
    );
}
