'use client'
import {useState} from 'react';
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import type {Image as ProductImage} from '@store-front/types';
import {PLACEHOLDER_IMAGE} from '@store-front/services/product-presenter';
import {AspectBox} from '@store-front/ui/aspect-box';
import {cn} from '@store-front/ui/lib/utils';

/**
 * A registered pair of views: the piece as it is shot, and every other view of it numbered on the strip
 * beneath, so a shopper can move between the room and the object without losing which is which. Resets to
 * the first view when the (variant-aware) image set changes.
 */
export function Gallery({images, alt}: { images: ProductImage[]; alt: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const [index, setIndex] = useState(0);
    const [prevImages, setPrevImages] = useState(images);
    if (images !== prevImages) { setPrevImages(images); setIndex(0); }  // variant changed the image set
    const current = images[index] ?? images[0];
    return (
        <div className="flex flex-col gap-3">
            <AspectBox ratio="4 / 5" className="window lg:max-h-[36rem]">
                <Image src={current?.imageUrl || PLACEHOLDER_IMAGE} alt={current?.imageName || alt} fill priority
                       sizes="(max-width: 1024px) 100vw, 50vw" className="object-cover"/>
            </AspectBox>
            {images.length > 1 && (
                <ul className="flex flex-wrap gap-2" aria-label={alt}>
                    {images.map((img, i) => (
                        <li key={img.id ?? img.imageUrl}>
                            <button type="button" onClick={() => setIndex(i)} aria-current={i === index}
                                    aria-label={t('GALLERY_IMAGE', {index: i + 1, total: images.length})}
                                    className={cn('window relative block size-16 transition-shadow duration-(--motion-fast) lg:size-20',
                                        i === index && 'shadow-[0_0_0_2px_var(--primary)]')}>
                                <Image src={img.imageUrl} alt="" fill sizes="80px" className="object-cover"/>
                                <span aria-hidden className={cn('figure absolute bottom-0 start-0 px-1.5 py-0.5 text-[0.625rem] leading-none',
                                    i === index ? 'bg-primary text-primary-foreground' : 'bg-background/90 text-foreground')}>
                                    {String(i + 1).padStart(2, '0')}
                                </span>
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
