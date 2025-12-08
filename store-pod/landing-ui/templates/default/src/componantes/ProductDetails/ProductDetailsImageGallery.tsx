'use client'
import * as React from "react";
import { useState } from "react";
import Image from "next/image";
import { Swiper, SwiperSlide } from "swiper/react";
import { Navigation } from "swiper/modules";

import { cn } from "@/lib/utils";
import { ImageIcon } from "lucide-react";
import {Product} from "@store-front/types";

export interface ProductDetailsImageGalleryProps extends React.HTMLAttributes<HTMLDivElement> {
  product: Product;
}

const ProductDetailsImageGallery = React.forwardRef<HTMLDivElement, ProductDetailsImageGalleryProps>(
  ({ product, className, ...props }, ref) => {
    const [mainImage, setMainImage] = useState<any | undefined>(
      product?.images?.[0]
    );

    if (!mainImage) {
      return (
        <div
          ref={ref}
          className={cn("flex aspect-square h-full w-full items-center justify-center bg-secondary", className)}
          {...props}
        >
          <ImageIcon className="size-12 text-muted-foreground" aria-hidden="true" />
        </div>
      );
    }

    return (
      <div ref={ref} className={cn("container mx-auto p-4", className)} {...props}>
        <div className="mb-4 relative group">
          <div className="h-[400px] relative flex items-center justify-center mx-auto overflow-hidden">
            <Image
              src={mainImage.imageUrl}
              alt={mainImage.imageName}
              fill
              className="object-contain transition-transform duration-300 group-hover:scale-110"
              sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
            />
          </div>
        </div>

        {product.images && product.images.length > 1 && (
          <Swiper
            spaceBetween={10}
            slidesPerView={4}
            navigation
            modules={[Navigation]}
            className="coverflow" // Use the custom theme class
            breakpoints={{
              640: { slidesPerView: 3 },
              768: { slidesPerView: 4 },
              1024: { slidesPerView: 5 },
            }}
          >
            {product.images.map((image) => (
              <SwiperSlide key={image.id || image.imageUrl}>
                <div
                  className={cn(
                    "relative aspect-square cursor-pointer rounded-lg border-2 p-1 transition-all duration-300",
                    mainImage.imageUrl === image.imageUrl
                      ? "border-primary shadow-lg"
                      : "border-transparent hover:border-border"
                  )}
                  onClick={() => setMainImage(image)}
                >
                  <Image
                    src={image.imageUrl}
                    alt={image.imageName}
                    fill
                    className="rounded-md object-cover transition-transform duration-300 hover:scale-105"
                    sizes="(max-width: 768px) 25vw, 20vw"
                  />
                </div>
              </SwiperSlide>
            ))}
          </Swiper>
        )}
      </div>
    );
  }
);
ProductDetailsImageGallery.displayName = "ProductDetailsImageGallery";

export { ProductDetailsImageGallery };