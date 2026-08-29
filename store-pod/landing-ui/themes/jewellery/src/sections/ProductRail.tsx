'use client'
import {useDir} from '@store-front/i18n/use-dir';
import type {Product, StoreContext} from '@store-front/types';
import {Swiper, SwiperSlide} from '@store-front/ui/swiper';
import {ProductCard} from '../components/ProductCard';

/** Horizontal product strip (home groups, related products). */
export function ProductRail({products, storeContext}: { products: Product[]; storeContext: StoreContext }) {
    const dir = useDir();
    if (products.length === 0) return null;
    return (
        <Swiper key={dir} dir={dir} navigation a11y={{enabled: true}}
                spaceBetween={16} slidesPerView={2} breakpoints={{640: {slidesPerView: 3}, 1024: {slidesPerView: 4}, 1280: {slidesPerView: 5}}}
                className="jewellery-rail !px-1 !pb-2">
            {products.map(p => (
                <SwiperSlide key={p.id} className="!h-auto">
                    <ProductCard product={p} storeContext={storeContext}/>
                </SwiperSlide>
            ))}
        </Swiper>
    );
}
