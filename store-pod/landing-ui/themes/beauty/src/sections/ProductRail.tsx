'use client'
import {Swiper, SwiperSlide} from 'swiper/react';
import {A11y, Navigation} from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/navigation';
import {useDir} from '@store-front/i18n/use-dir';
import type {Product, StoreContext} from '@store-front/types';
import {ProductCard} from '../components/ProductCard';

/** A shelf: plates shingled on a 1px ink pitch, arrows as plates. */
export function ProductRail({products, storeContext}: { products: Product[]; storeContext: StoreContext }) {
    const dir = useDir();
    if (products.length === 0) return null;
    return (
        <Swiper key={dir} dir={dir} modules={[Navigation, A11y]} navigation a11y={{enabled: true}} spaceBetween={-1} slidesPerView={2}
                breakpoints={{640: {slidesPerView: 3}, 1024: {slidesPerView: 4}, 1400: {slidesPerView: 5}}} className="border border-foreground">
            {products.map(p => (
                <SwiperSlide key={p.id} className="!h-auto border-e border-foreground last:border-e-0">
                    <ProductCard product={p} storeContext={storeContext} className="border-0"/>
                </SwiperSlide>
            ))}
        </Swiper>
    );
}
