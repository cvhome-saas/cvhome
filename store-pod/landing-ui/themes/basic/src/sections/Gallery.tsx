'use client'
import {useState} from 'react';
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import type {Image as ProductImage} from '@store-front/types';
import {PLACEHOLDER_IMAGE} from '@store-front/services/product-presenter';
import {AspectBox} from '@store-front/ui/aspect-box';
import {cn} from '@store-front/ui/lib/utils';

/** The entry's plate: main photo in a ruled cell, thumbnails as a ruled strip. Resets to the first image when the (variant-aware) set changes. */
export function Gallery({images, alt}: { images: ProductImage[]; alt: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const [index, setIndex] = useState(0);
    const [prevImages, setPrevImages] = useState(images);
    if (images !== prevImages) { setPrevImages(images); setIndex(0); }  // variant changed the image set
    const current = images[index] ?? images[0];
    return (
        <div className="flex flex-col gap-2">
            <AspectBox className="border bg-card">
                <Image src={current?.imageUrl || PLACEHOLDER_IMAGE} alt={current?.altText || alt} fill priority sizes="(max-width: 1024px) 100vw, 50vw" className="object-contain"/>
            </AspectBox>
            {images.length > 1 && (
                <ul className="scroll-x flex gap-2" aria-label={alt}>
                    {images.map((img, i) => (
                        <li key={img.id ?? img.imageUrl} className="shrink-0">
                            <button type="button" onClick={() => setIndex(i)} aria-current={i === index}
                                    aria-label={t('GALLERY_IMAGE', {index: i + 1, total: images.length})}
                                    className={cn('relative block size-16 overflow-hidden border bg-card transition-colors duration-(--motion-fast) sm:size-20',
                                        i === index ? 'border-primary ring-1 ring-primary' : 'hover:border-foreground')}>
                                <Image src={img.imageUrl} alt="" fill sizes="80px" className="object-cover"/>
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
