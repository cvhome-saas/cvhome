'use client'
import * as React from "react";
import {Autoplay, EffectCoverflow, Navigation, Pagination} from 'swiper/modules';
import {Swiper, SwiperSlide} from 'swiper/react';
import {Store} from "@/types/store";
import Image from 'next/image';
import { cn } from "@/lib/utils";

import 'swiper/css';
import 'swiper/css/effect-coverflow';
import 'swiper/css/pagination';
import 'swiper/css/navigation';
import './swiper-custom.css';

export interface CoverFlowProps extends React.HTMLAttributes<HTMLElement> {
    store: Store;
}

const CoverFlow = React.forwardRef<HTMLElement, CoverFlowProps>(
    ({ store, className, ...props }, ref) => {
    const containerHeightClass = "h-[450px]";

    if (!store.sliderImages || store.sliderImages.length === 0) {
        return null;
    }

    return (
        <section ref={ref} className={cn("py-8 bg-background overflow-hidden", className)} {...props}>
            <div className="lg:mx-auto max-w-7xl mx-4 sm:mx-6">
                <div className={`relative ${containerHeightClass} flex items-center justify-center`}>
                    <Swiper
                        modules={[EffectCoverflow, Pagination, Autoplay, Navigation]}
                        effect={'coverflow'}
                        loop={true}
                        navigation={true}
                        pagination={{ clickable: true }}
                        centeredSlides={true}
                        grabCursor={true}
                        autoplay={{
                            delay: 3000,
                            disableOnInteraction: false,
                        }}
                        coverflowEffect={{
                            rotate: 0,
                            stretch: 0,
                            depth: 100,
                            modifier: 1,
                            slideShadows: false,
                        }}
                        breakpoints={{
                            320: {
                                slidesPerView: 1,
                                spaceBetween: 20,
                            },
                            640: {
                                slidesPerView: 2,
                                spaceBetween: 30,
                            },
                            1024: {
                                slidesPerView: 3,
                                spaceBetween: 40,
                            },
                        }}
                        className="coverflow w-full h-full opacity-0 transition-opacity duration-500"
                        onSwiper={(swiper) => {
                            // Use a timeout to ensure styles are applied before fading in
                            setTimeout(() => {
                            swiper.el.style.opacity = '1';
                            }, 0);
                        }}
                    >
                        {store.sliderImages.map((slide, index) => (
                            <SwiperSlide key={ index} className="flex justify-center items-center bg-transparent">
                                <div className="relative w-full h-full">
                                    <Image
                                        src={slide.url}
                                        alt={`Slide ${index + 1}`}
                                        fill
                                        style={{objectFit: "contain"}}
                                        priority={index < 3}
                                        sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
                                        className="block rounded-lg"
                                    />
                                </div>
                            </SwiperSlide>
                        ))}
                    </Swiper>
                </div>
            </div>
        </section>
    );
});
CoverFlow.displayName = "CoverFlow";

export default CoverFlow;