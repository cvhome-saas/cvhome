'use client'
import * as React from "react";
import {useState} from "react";
import Image from "next/image";
import {Swiper, SwiperSlide} from "swiper/react";
import {Navigation} from "swiper/modules";
import {cn} from "@/lib/utils";
import {ImageIcon} from "lucide-react";
import {Product} from "@store-front/types";

export interface ProductDetailsImageGalleryProps extends React.HTMLAttributes<HTMLDivElement> {
    product: Product;
}

const ProductDetailsImageGallery = React.forwardRef<HTMLDivElement, ProductDetailsImageGalleryProps>(
    ({product, className, ...props}, ref) => {
        const [mainImage, setMainImage] = useState<any | undefined>(
            product?.images?.[0]
        );

        if (!mainImage) {
            return (
                <div
                    ref={ref}
                    className={cn("flex aspect-square w-full items-center justify-center bg-secondary", className)}
                    {...props}
                >
                    <ImageIcon className="size-12 text-muted-foreground" aria-hidden="true"/>
                </div>
            );
        }

        return (
            <div ref={ref} className={cn("w-full", className)} {...props}>
                {/* Main image */}
                <div className="relative aspect-square overflow-hidden bg-secondary mb-4 group">
                    <Image
                        src={mainImage.imageUrl}
                        alt={mainImage.imageName}
                        fill
                        className="object-contain transition-transform duration-500 group-hover:scale-105"
                        sizes="(max-width: 768px) 100vw, 50vw"
                    />
                </div>

                {/* Thumbnail strip */}
                {product.images && product.images.length > 1 && (
                    <Swiper
                        spaceBetween={8}
                        slidesPerView={4}
                        navigation
                        modules={[Navigation]}
                        className="coverflow"
                        breakpoints={{
                            640: {slidesPerView: 4},
                            768: {slidesPerView: 5},
                            1024: {slidesPerView: 5},
                        }}
                    >
                        {product.images.map((image) => (
                            <SwiperSlide key={image.id || image.imageUrl}>
                                <button
                                    onClick={() => setMainImage(image)}
                                    className={cn(
                                        "relative aspect-square w-full border overflow-hidden transition-all duration-200",
                                        mainImage.imageUrl === image.imageUrl
                                            ? "border-primary"
                                            : "border-border hover:border-muted-foreground"
                                    )}
                                    aria-label={image.imageName}
                                >
                                    <Image
                                        src={image.imageUrl}
                                        alt={image.imageName}
                                        fill
                                        className="object-cover"
                                        sizes="80px"
                                    />
                                </button>
                            </SwiperSlide>
                        ))}
                    </Swiper>
                )}
            </div>
        );
    }
);
ProductDetailsImageGallery.displayName = "ProductDetailsImageGallery";

export {ProductDetailsImageGallery};
