'use client'
import * as React from "react";
import {Autoplay, EffectFade, Navigation, Pagination} from 'swiper/modules';
import {Swiper, SwiperSlide} from 'swiper/react';
import Image from 'next/image';

import {Store} from "@/types/store";
import {cn} from "@/lib/utils";

import 'swiper/css';
import 'swiper/css/effect-fade';
import 'swiper/css/pagination';
import 'swiper/css/navigation';
import './swiper-custom.css';

export interface CoverFlowProps extends React.HTMLAttributes<HTMLElement> {
    store: Store;
}

const CoverFlow = React.forwardRef<HTMLElement, CoverFlowProps>(
    ({store, className, ...props}, ref) => {
        if (!store.sliderImages || store.sliderImages.length === 0) {
            return null;
        }

        return (
            <section
                ref={ref}
                className={cn("relative overflow-hidden rounded-3xl border border-border bg-background", className)}
                {...props}
            >
                <div className="relative">
                    <Swiper
                        modules={[EffectFade, Pagination, Autoplay, Navigation]}
                        effect="fade"
                        fadeEffect={{crossFade: true}}
                        loop={store.sliderImages.length > 1}
                        navigation
                        pagination={{clickable: true}}
                        autoplay={{
                            delay: 4500,
                            disableOnInteraction: false,
                        }}
                        className="coverflow w-full"
                    >
                        {store.sliderImages.map((slide, index) => (
                            <SwiperSlide key={index}>
                                <div className="relative">
                                    <div className="relative h-[360px] sm:h-[440px] lg:h-[520px] w-full">
                                        <Image
                                            src={slide.url}
                                            alt={`Hero slide ${index + 1}`}
                                            fill
                                            priority={index === 0}
                                            sizes="(max-width: 640px) 100vw, (max-width: 1024px) 100vw, 1200px"
                                            className="object-cover"
                                        />
                                    </div>

                                    <div
                                        className="pointer-events-none absolute inset-0 bg-linear-to-t from-black/35 via-black/10 to-transparent"/>

                                    <div className="absolute inset-x-6 bottom-6 sm:inset-x-10 sm:bottom-10">
                                        <p className="text-xs tracking-[0.22em] uppercase text-white/85">
                                            {store.name}
                                        </p>
                                    </div>
                                </div>
                            </SwiperSlide>
                        ))}
                    </Swiper>
                </div>
            </section>
        );
    }
);
CoverFlow.displayName = "CoverFlow";

export default CoverFlow;