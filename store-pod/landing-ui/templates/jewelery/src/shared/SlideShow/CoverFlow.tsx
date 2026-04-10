'use client'
import * as React from "react";
import {Autoplay, Navigation, Pagination} from 'swiper/modules';
import {Swiper, SwiperSlide} from 'swiper/react';
import {Store} from "@/types/store";
import Image from 'next/image';
import {cn} from "@/lib/utils";

import 'swiper/css';
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
            <section ref={ref} className={cn("relative overflow-hidden bg-foreground", className)} {...props}>
                <Swiper
                    modules={[Pagination, Autoplay, Navigation]}
                    loop={true}
                    navigation={true}
                    pagination={{clickable: true}}
                    centeredSlides={true}
                    grabCursor={true}
                    autoplay={{
                        delay: 4500,
                        disableOnInteraction: false,
                    }}
                    slidesPerView={1}
                    spaceBetween={0}
                    className="jewelry-slideshow w-full opacity-0 transition-opacity duration-700"
                    onSwiper={(swiper) => {
                        setTimeout(() => {
                            swiper.el.style.opacity = '1';
                        }, 0);
                    }}
                >
                    {store.sliderImages.map((slide, index) => (
                        <SwiperSlide key={index} className="relative">
                            <div className="relative w-full h-[520px] sm:h-[600px] lg:h-[680px] overflow-hidden">
                                <Image
                                    src={slide.url}
                                    alt={`Slide ${index + 1}`}
                                    fill
                                    style={{objectFit: "cover"}}
                                    priority={index < 2}
                                    sizes="100vw"
                                    className="scale-105 transition-transform duration-[8000ms] ease-out"
                                />
                                {/* Overlay for luxury effect */}
                                <div className="absolute inset-0 bg-foreground/20"/>
                            </div>
                        </SwiperSlide>
                    ))}
                </Swiper>
            </section>
        );
    }
);
CoverFlow.displayName = "CoverFlow";

export default CoverFlow;
