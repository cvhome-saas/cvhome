'use client'
import * as React from "react";
import {useMemo, useState} from "react";
import Image from "next/image";

import {cn} from "@/lib/utils";
import {ImageIcon} from "lucide-react";
import {Product} from "@store-front/types";

export interface ProductDetailsImageGalleryProps extends React.HTMLAttributes<HTMLDivElement> {
    product: Product;
}

const ProductDetailsImageGallery = React.forwardRef<HTMLDivElement, ProductDetailsImageGalleryProps>(
    ({product, className, ...props}, ref) => {
        const images = useMemo(() => product?.images ?? [], [product?.images]);
        const [mainImage, setMainImage] = useState<any | undefined>(images[0]);

        if (!mainImage) {
            return (
                <div
                    ref={ref}
                    className={cn(
                        "flex aspect-[4/5] w-full items-center justify-center rounded-2xl bg-secondary",
                        className
                    )}
                    {...props}
                >
                    <ImageIcon className="size-12 text-muted-foreground" aria-hidden="true"/>
                </div>
            );
        }

        return (
            <div
                ref={ref}
                className={cn("w-full", className)}
                {...props}
            >
                <div className="grid grid-cols-1 gap-4 lg:grid-cols-12 lg:gap-6">
                    {images.length > 1 && (
                        <div className="hidden lg:col-span-2 lg:block">
                            <div className="max-h-[640px] overflow-y-auto pe-1">
                                <div className="grid gap-3">
                                    {images.map((img) => {
                                        const isActive = mainImage?.imageUrl === img.imageUrl;
                                        return (
                                            <button
                                                key={img.id || img.imageUrl}
                                                type="button"
                                                onClick={() => setMainImage(img)}
                                                className={cn(
                                                    "group relative overflow-hidden rounded-xl border bg-background transition",
                                                    isActive
                                                        ? "border-primary ring-2 ring-primary/20"
                                                        : "border-border hover:border-foreground/30"
                                                )}
                                                aria-pressed={isActive}
                                            >
                                                <div className="relative aspect-square">
                                                    <Image
                                                        src={img.imageUrl}
                                                        alt={img.imageName}
                                                        fill
                                                        className="object-cover transition-transform duration-500 group-hover:scale-[1.03]"
                                                        sizes="120px"
                                                    />
                                                </div>
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>
                        </div>
                    )}

                    <div className={cn(images.length > 1 ? "lg:col-span-10" : "lg:col-span-12")}>
                        <div className="relative overflow-hidden rounded-2xl bg-secondary">
                            <div className="relative aspect-[4/5] w-full">
                                <Image
                                    src={mainImage.imageUrl}
                                    alt={mainImage.imageName}
                                    fill
                                    className="object-cover"
                                    sizes="(max-width: 1024px) 100vw, 60vw"
                                    priority
                                />
                            </div>

                            <div className="pointer-events-none absolute inset-0 bg-linear-to-t from-black/10 via-transparent to-transparent"/>
                        </div>

                        {images.length > 1 && (
                            <div className="mt-4 lg:hidden">
                                <div className="flex gap-3 overflow-x-auto pb-2">
                                    {images.map((img) => {
                                        const isActive = mainImage?.imageUrl === img.imageUrl;
                                        return (
                                            <button
                                                key={img.id || img.imageUrl}
                                                type="button"
                                                onClick={() => setMainImage(img)}
                                                className={cn(
                                                    "shrink-0 overflow-hidden rounded-xl border bg-background transition",
                                                    isActive
                                                        ? "border-primary ring-2 ring-primary/20"
                                                        : "border-border"
                                                )}
                                                aria-pressed={isActive}
                                            >
                                                <div className="relative h-20 w-20">
                                                    <Image
                                                        src={img.imageUrl}
                                                        alt={img.imageName}
                                                        fill
                                                        className="object-cover"
                                                        sizes="80px"
                                                    />
                                                </div>
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        );
    }
);

ProductDetailsImageGallery.displayName = "ProductDetailsImageGallery";

export {ProductDetailsImageGallery};