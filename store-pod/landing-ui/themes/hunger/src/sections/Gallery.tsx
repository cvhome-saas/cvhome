'use client'
import {useState} from 'react';
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import type {Image as ProductImage} from '@store-front/types';
import {PLACEHOLDER_IMAGE} from '@store-front/services/product-presenter';
import {AspectBox} from '@store-front/ui/aspect-box';
import {cn} from '@store-front/ui/lib/utils';

/**
 * The dish's picture in a ruled frame, with numbered stubs beneath it instead of thumbnails — a menu
 * numbers its plates. Resets to the first image when the (variant-aware) image set changes.
 */
export function Gallery({images, alt}: { images: ProductImage[]; alt: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const [index, setIndex] = useState(0);
    const [prevImages, setPrevImages] = useState(images);
    if (images !== prevImages) { setPrevImages(images); setIndex(0); }  // variant changed the image set
    const current = images[index] ?? images[0];
    return (
        <div className="flex flex-col">
            <AspectBox className="crop border border-foreground bg-muted">
                <Image src={current?.imageUrl || PLACEHOLDER_IMAGE} alt={current?.altText || alt} fill priority
                       sizes="(max-width: 1024px) 100vw, 50vw" className="object-cover"/>
            </AspectBox>
            {images.length > 1 && (
                <ul className="scroll-x -mt-px flex" aria-label={alt}>
                    {images.map((img, i) => (
                        <li key={img.id ?? img.imageUrl} className="-ms-px first:ms-0">
                            <button type="button" onClick={() => setIndex(i)} aria-current={i === index}
                                    aria-label={t('GALLERY_IMAGE', {index: i + 1, total: images.length})}
                                    className={cn('press flex h-9 min-w-10 items-center justify-center border border-foreground text-sm tabular-nums transition-colors duration-(--motion-fast)',
                                        i === index ? 'bg-primary text-primary-foreground border-primary' : 'hover:bg-[var(--wash)]')}>
                                {i + 1}
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
