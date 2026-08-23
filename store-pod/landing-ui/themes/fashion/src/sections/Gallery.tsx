'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import type {Image as ProductImage} from '@store-front/types';
import {AspectBox} from '@store-front/ui/aspect-box';
import {cn} from '@store-front/ui/lib/utils';
import {PosterImage} from '../components/PosterImage';

/** The product's own poster (a big peeling sheet) with its other pictures as small sheets under it. Resets when the variant changes the set. */
export function Gallery({images, alt, brand}: { images: ProductImage[]; alt: string; brand?: string }) {
    const t = useTranslations('PAGE.PRODUCT');
    const [index, setIndex] = useState(0);
    const [prevImages, setPrevImages] = useState(images);
    if (images !== prevImages) { setPrevImages(images); setIndex(0); }  // variant changed the image set
    const current = images[index] ?? images[0];
    return (
        <div className="flex flex-col gap-4">
            <figure className="sheet sheen peel [--tilt:-0.6deg]">
                <AspectBox className="bg-background" ratio={current?.imageUrl ? '4 / 5' : '4 / 3'}>
                    <PosterImage key={current?.imageUrl ?? 'none'} src={current?.imageUrl} alt={current?.imageName || alt} title={alt} meta={brand} priority fit="contain" align="start"
                                 sizes="(max-width: 1024px) 100vw, 55vw"/>
                </AspectBox>
            </figure>
            {images.length > 1 && (
                <ul className="wall flex gap-3 overflow-x-auto px-1 pb-3 pt-1" aria-label={alt}>
                    {images.map((img, i) => (
                        <li key={img.id ?? img.imageUrl} className="shrink-0">
                            <button type="button" onClick={() => setIndex(i)} aria-current={i === index}
                                    aria-label={t('GALLERY_IMAGE', {index: i + 1, total: images.length})}
                                    className={cn('sheet sheet-flat sheet-lift relative block size-20 overflow-hidden', i === index && 'outline outline-[3px] outline-offset-2 outline-primary')}>
                                <PosterImage src={img.imageUrl} alt="" title={alt} tone="faint" sizes="80px"/>
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
