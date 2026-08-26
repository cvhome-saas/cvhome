'use client'
import {useState} from 'react';
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import type {Image as ProductImage} from '@store-front/types';
import {PLACEHOLDER_IMAGE} from '@store-front/services/product-presenter';
import {AspectBox} from '@store-front/ui/aspect-box';
import {cn} from '@store-front/ui/lib/utils';

/** The feature picture in its ruled well, the rest of the shoot ranged under it. Resets when a variant changes the set. */
export function Gallery({images, alt}: { images: ProductImage[]; alt: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const [index, setIndex] = useState(0);
    const [prevImages, setPrevImages] = useState(images);
    if (images !== prevImages) { setPrevImages(images); setIndex(0); }  // variant changed the image set
    const current = images[index] ?? images[0];
    return (
        <div className="flex flex-col gap-2">
            <AspectBox className="wash hair border" ratio="1 / 1">
                <Image src={current?.imageUrl || PLACEHOLDER_IMAGE} alt={current?.altText || alt} fill priority
                       sizes="(max-width: 1024px) 100vw, 50vw" className="object-contain"/>
            </AspectBox>
            {images.length > 1 && (
                <ul className="flex gap-2 overflow-x-auto pb-1" aria-label={alt}>
                    {images.map((img, i) => (
                        <li key={img.id ?? img.imageUrl}>
                            <button type="button" onClick={() => setIndex(i)} aria-current={i === index}
                                    aria-label={t('GALLERY_IMAGE', {index: i + 1, total: images.length})}
                                    className={cn('wash relative size-16 shrink-0 overflow-hidden border-2 transition-colors duration-(--motion-fast)',
                                        i === index ? 'border-primary' : 'hair border-transparent hover:border-current')}>
                                <Image src={img.imageUrl} alt="" fill sizes="64px" className="object-cover"/>
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
