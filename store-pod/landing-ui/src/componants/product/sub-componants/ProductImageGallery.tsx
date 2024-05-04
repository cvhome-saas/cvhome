'use client'
import React from "react";
import {Image} from "@/types/product-groups";

import {Swiper, SwiperSlide} from 'swiper/react';
import {Navigation} from 'swiper/modules';

const ProductImageGallery = ({images}: { images: Image[] }) => {
    return <div className={"product-large-image-wrapper"}>
        <Swiper navigation={true} modules={[Navigation]} className="mySwiper" slidesPerView={1} spaceBetween={50}
                pagination={{clickable: true}}
                scrollbar={{draggable: true}}>
            {
                images.map(it => {
                    return (
                        <SwiperSlide key={it.id}>
                            <img src={it.imageUrl} alt={it.imageName} height={400} width={600}/>
                        </SwiperSlide>
                    )
                })
            }
        </Swiper>
    </div>

};


export default ProductImageGallery;