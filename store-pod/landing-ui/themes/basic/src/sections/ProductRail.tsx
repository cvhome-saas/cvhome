'use client'
import {Swiper, SwiperSlide} from 'swiper/react';
import {A11y, Navigation} from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/navigation';
import {useDir} from '@store-front/i18n/use-dir';
import type {Product, StoreContext} from '@store-front/types';
import {ProductCard} from '../components/ProductCard';

/** A ruled row of entries that scrolls (home groups, related products); arrows are square cells at the row's ends. */
export function ProductRail({products, storeContext}: { products: Product[]; storeContext: StoreContext }) {
    const dir = useDir();
    if (products.length === 0) return null;
    return (
        <Swiper key={dir} dir={dir} modules={[Navigation, A11y]} navigation a11y={{enabled: true}}
                spaceBetween={0} slidesPerView={2} breakpoints={{640: {slidesPerView: 3}, 1024: {slidesPerView: 4}, 1280: {slidesPerView: 5}}}
                className="rail">
            {products.map(p => (
                <SwiperSlide key={p.id} className="!h-auto">
                    <ProductCard product={p} storeContext={storeContext}/>
                </SwiperSlide>
            ))}
        </Swiper>
    );
}
