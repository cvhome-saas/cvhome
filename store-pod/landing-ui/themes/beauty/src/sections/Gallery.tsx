'use client'
import {useState} from 'react';
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import type {Image as ProductImage} from '@store-front/types';
import {PLACEHOLDER_IMAGE} from '@store-front/services/product-presenter';
import {AspectBox} from '@store-front/ui/aspect-box';
import {cn} from '@store-front/ui/lib/utils';

/** The item window: a plate, NN / NN counter, thumbnails as small plates butted in a row. */
export function Gallery({images, alt}: { images: ProductImage[]; alt: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const [index, setIndex] = useState(0);
    const [prevImages, setPrevImages] = useState(images);
    if (images !== prevImages) { setPrevImages(images); setIndex(0); }  // variant changed the image set
    const current = images[index] ?? images[0];
    const pad = (n: number) => String(n).padStart(2, '0');
    return (
        <div className="flex flex-col">
            <AspectBox className="plate relative bg-muted">
                <Image src={current?.imageUrl || PLACEHOLDER_IMAGE} alt={current?.imageName || alt} fill priority sizes="(max-width: 1024px) 100vw, 55vw" className="object-contain"/>
                {images.length > 0 && <span className="plate absolute bottom-3 end-3 px-2 py-1 font-mono text-xs tabular-nums" dir="ltr">{pad(index + 1)} / {pad(Math.max(images.length, 1))}</span>}
            </AspectBox>
            {images.length > 1 && (
                <ul className="-mt-px flex overflow-x-auto" aria-label={alt}>
                    {images.map((img, i) => (
                        <li key={img.id ?? img.imageUrl} className="-ms-px first:ms-0">
                            <button type="button" onClick={() => setIndex(i)} aria-current={i === index} aria-label={t('GALLERY_IMAGE', {index: i + 1, total: images.length})}
                                    className={cn('plate relative block size-16 overflow-hidden bg-muted', i === index ? 'ring-2 ring-inset ring-primary' : 'opacity-70 hover:opacity-100')}>
                                <Image src={img.imageUrl} alt="" fill sizes="64px" className="object-cover"/>
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
