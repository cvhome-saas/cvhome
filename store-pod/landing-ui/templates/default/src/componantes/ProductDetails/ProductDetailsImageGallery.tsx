'use client'
import {Product} from "@/types/product-groups";
import {useState} from "react";
import {Swiper, SwiperSlide} from "swiper/react";
import {Navigation} from "swiper/modules";

export const ProductDetailsImageGallery = ({product}: { product: Product }) => {

    if (product && product.images && product.images.length > 0) {
        const [mainImage, setMainImage] = useState(product.images[0]);

        return (
            <div className="container mx-auto p-4">

                <div className="mb-4 relative group">
                    <div className="h-[400px] flex items-center justify-center mx-auto overflow-hidden">
                        <img
                            src={mainImage.imageUrl}
                            alt={mainImage.imageName}
                            className="max-w-full max-h-full object-contain transition-transform duration-300 group-hover:scale-110"
                        />
                    </div>
                </div>


                {product.images && product.images.length > 1 && (
                    <Swiper
                        spaceBetween={10}
                        slidesPerView={4}
                        navigation
                        modules={[Navigation]}
                        className="mySwiper"
                        breakpoints={{
                            640: {slidesPerView: 3},
                            768: {slidesPerView: 4},
                            1024: {slidesPerView: 5},
                        }}
                    >
                        {product.images.map((image, index) => (
                            <SwiperSlide key={index}>
                                <div
                                    className={`relative p-1 rounded-lg transition-all duration-300 ${
                                        mainImage.imageUrl === image.imageUrl
                                            ? "border-2 border-primary shadow-lg"
                                            : "border-2 border-transparent hover:border-neutral"
                                    }`}
                                >
                                    <img
                                        src={image.imageUrl}
                                        alt={image.imageName}
                                        className="w-full h-full object-cover rounded-md cursor-pointer transition-transform duration-300 hover:scale-105"
                                        onClick={() => setMainImage(image)}
                                    />
                                </div>
                            </SwiperSlide>
                        ))}
                    </Swiper>
                )}
            </div>
        );
    } else {
        return <></>
    }
}