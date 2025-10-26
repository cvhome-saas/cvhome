'use client'
import * as React from 'react';
import {Autoplay, EffectCoverflow, Navigation, Pagination} from 'swiper/modules';
import {Swiper, SwiperSlide} from 'swiper/react';
import {Store} from "@/types/store";
import Image from 'next/image';


export default function CoverFlow({store}: { store: Store }) {
    const containerHeightClass = "h-[450px]";

    return (
        <section className="py-8 bg-background overflow-hidden">
            <div className="lg:mx-auto max-w-7xl mx-4 sm:mx-6">
                <div className={`relative ${containerHeightClass} flex items-center justify-center`}>
                    <Swiper
                        modules={[EffectCoverflow, Pagination, Autoplay, Navigation]}
                        effect={'coverflow'}
                        loop={true}
                        autoplay={{
                            delay: 3000,
                            disableOnInteraction: false,
                        }}
                        navigation={true}
                        pagination={{
                            clickable: true,
                        }}
                        centeredSlides={true}
                        grabCursor={true}
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
                                spaceBetween: 15,
                            },
                            640: {
                                slidesPerView: 2,
                                spaceBetween: 20,
                            },
                            1024: {
                                slidesPerView: 3,
                                spaceBetween: 30,
                            },
                        }}
                        className="coverflow w-full h-full opacity-0 transition-opacity duration-500"
                        onSwiper={(swiper) => {
                            swiper.el.style.opacity = '1';
                        }}
                    >
                        {store.sliderImages && store.sliderImages.map((p, index) => {
                            return (
                                <SwiperSlide key={index} className="flex justify-center items-center bg-transparent">
                                    <div className="relative w-full h-full">
                                        <Image
                                            src={p.url}
                                            alt={`Slide ${index + 1}`}
                                            fill
                                            style={{objectFit: "contain"}}
                                            priority={index < 3}
                                            sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
                                            className="block rounded-lg shadow-md"
                                        />
                                    </div>
                                </SwiperSlide>
                            );
                        })}
                    </Swiper>
                </div>
            </div>
        </section>
    );
}