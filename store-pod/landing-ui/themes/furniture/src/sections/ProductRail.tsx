'use client'
import {useDir} from '@store-front/i18n/use-dir';
import type {Product, StoreContext} from '@store-front/types';
import {Swiper, SwiperSlide} from '@store-front/ui/swiper';
import {ProductCard} from '../components/ProductCard';

/** A run of pieces along one wall (home groups, related products). */
export function ProductRail({products, storeContext}: { products: Product[]; storeContext: StoreContext }) {
    const dir = useDir();
    if (products.length === 0) return null;
    return (
        <Swiper key={dir} dir={dir} navigation a11y={{enabled: true}}
                observer observeParents resizeObserver
                spaceBetween={20} slidesPerView={1.35}
                breakpoints={{640: {slidesPerView: 2.4, spaceBetween: 24}, 1024: {slidesPerView: 3.4, spaceBetween: 28}, 1280: {slidesPerView: 4.2, spaceBetween: 32}}}
                className="furniture-rail !pb-2">
            {products.map(p => (
                <SwiperSlide key={p.id} className="!h-auto">
                    <ProductCard product={p} storeContext={storeContext}/>
                </SwiperSlide>
            ))}
        </Swiper>
    );
}
