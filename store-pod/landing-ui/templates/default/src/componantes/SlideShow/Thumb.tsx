/*
'use client'
import * as React from 'react';

import { Navigation, Thumbs } from 'swiper/modules';
import { Swiper, SwiperSlide } from 'swiper/react';

const imgList = [
    {
        img: 'https://images.unsplash.com/photo-1594818898109-44704fb548f6?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80"',
    },
    {
        img: 'https://images.unsplash.com/photo-1594818896795-35ad7bcf3c6a?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80',
    },
    {
        img: 'https://images.unsplash.com/photo-1594818896744-57eca4d47b07?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80',
    },
    {
        img: 'https://images.unsplash.com/photo-1594818897077-aec41f55241f?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1951&q=80',
    },
    {
        img: 'https://images.unsplash.com/photo-1649859394614-dc4f7290b7f2?ixlib=rb-1.2.1&ixid=MnwxMjA3fDF8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80',
    },
    {
        img: 'https://images.unsplash.com/photo-1658909914248-15157163c878?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80',
    },
];

export default function Thumb() {
    const [activeThumb, setActiveThumb] = React.useState<null | boolean>(null);

    return (
        <section className="pt-[7rem] pb-[2rem] ">
            <div className="lg:mx-auto max-w-5xl mx-[1.5rem]">
                <div className="border-8 bg-white border-white">
                    <Swiper
                        modules={[Navigation, Thumbs]}
                        loop={true}
                        slidesPerView={1}
                        pagination={{
                            clickable: true,
                        }}
                        grabCursor={true}
                        navigation={true}
                        thumbs={{
                            // @ts-ignore
                            swiper:
                                activeThumb && !activeThumb.destroyed ? activeThumb : null,
                        }}
                        className="thumbShow"
                    >
                        {imgList.map((p, index) => {
                            return (
                                <SwiperSlide key={index}>
                                    <img src={p.img} alt="" />
                                </SwiperSlide>
                            );
                        })}
                    </Swiper>
                    <Swiper
                        // @ts-ignore
                        onSwiper={setActiveThumb}
                        loop={false}
                        grabCursor={true}
                        spaceBetween={10}
                        slidesPerView={4}
                        modules={[Navigation, Thumbs]}
                        className="thumbBtn mt-5"
                    >
                        {imgList.map((item, index) => (
                            <SwiperSlide key={index}>
                                <div className="">
                                    <img src={item.img} alt="product images" />
                                </div>
                            </SwiperSlide>
                        ))}
                    </Swiper>
                </div>
            </div>
        </section>
    );
}
*/
