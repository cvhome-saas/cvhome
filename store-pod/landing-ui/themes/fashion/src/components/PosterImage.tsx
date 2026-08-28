'use client'
import {useState} from 'react';
import Image from 'next/image';
import {cn} from '@store-front/ui/lib/utils';

/**
 * A picture pasted on a sheet. When the merchant gave no picture, or the file does not load, the sheet
 * becomes a typographic poster: the title in poster caps, the meta line printed under it — a wall never
 * shows a broken image or a grey box.
 */
export function PosterImage({src, alt, title, meta, sizes, priority, className, imgClassName, fit = 'cover', tone = 'ink', align = 'end'}: {
    src: string | undefined; alt: string; title: string; meta?: string; sizes: string; priority?: boolean;
    className?: string; imgClassName?: string; fit?: 'cover' | 'contain'; tone?: 'ink' | 'faint'; align?: 'start' | 'end';
}) {
    const [failed, setFailed] = useState(false);
    const showImage = !!src && !src.endsWith('/placeholder.png') && !failed;
    return (
        <div className={cn('@container absolute inset-0', className)}>
            {showImage ? (
                <Image src={src} alt={alt} fill sizes={sizes} priority={priority} onError={() => setFailed(true)}
                       ref={node => { if (node && node.complete && node.naturalWidth === 0) setFailed(true); }}  // failed before hydration: onError never fires
                       className={cn(fit === 'cover' ? 'object-cover' : 'object-contain', imgClassName)}/>
            ) : (
                <div className={cn('typo-poster absolute inset-0 p-[8%]', align === 'start' && 'justify-start')} aria-hidden>
                    <p className={cn('font-display text-[clamp(0.75rem,13cqi,5.5rem)] uppercase leading-[0.86] [overflow-wrap:anywhere]', tone === 'faint' ? 'typo-faint line-clamp-3' : 'line-clamp-4')} dir="auto"><bdi>{title}</bdi></p>
                    {meta && <p className="mt-[0.6em] truncate text-[clamp(0.6rem,3cqi,0.8rem)] uppercase tracking-wide text-muted-foreground"><bdi dir="auto">{meta}</bdi></p>}
                </div>
            )}
        </div>
    );
}
