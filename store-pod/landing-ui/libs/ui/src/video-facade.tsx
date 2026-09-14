'use client';

import {useState} from 'react';
import {PlayIcon} from 'lucide-react';
import {useTranslations} from 'next-intl';

export interface VideoFacadeProps {
    /** The player's embed URL (YouTube's nocookie host, Vimeo's player), loaded only once the shopper presses play. */
    embedSrc: string;
    title: string;
    /** A still of the video to show in the player's place; without one the frame is the theme's muted surface. */
    posterSrc?: string;
}

/**
 * A video section's player, loaded on demand.
 *
 * An embedded YouTube player pulls about a megabyte of script over several connections before the page's `load`
 * event: in the 2026-09-14 load test every browser failure on the home page was the seeded video, the event
 * arriving 13–26 s after the document with the stack idle. Until the shopper presses play the section is one still
 * image and a button, and the player, with autoplay so that press is the only one needed, replaces it.
 */
export function VideoFacade({embedSrc, title, posterSrc}: VideoFacadeProps) {
    const [playing, setPlaying] = useState(false);
    const t = useTranslations('COMMON');
    if (playing) {
        const src = `${embedSrc}${embedSrc.includes('?') ? '&' : '?'}autoplay=1`;
        return (
            <iframe src={src} title={title} className="absolute inset-0 size-full"
                    allow="accelerometer; autoplay; encrypted-media; picture-in-picture" allowFullScreen
                    referrerPolicy="no-referrer"/>
        );
    }
    return (
        <button type="button" onClick={() => setPlaying(true)} aria-label={t('PLAY_VIDEO', {title})}
                className="group absolute inset-0 size-full cursor-pointer bg-muted">
            {posterSrc && (
                // eslint-disable-next-line @next/next/no-img-element -- the provider's still; images are unoptimized
                <img src={posterSrc} alt="" loading="lazy" decoding="async"
                     className="absolute inset-0 size-full object-cover"/>
            )}
            <span className="absolute inset-0 grid place-items-center">
                <span className="grid size-16 place-items-center rounded-full bg-foreground/80 text-background
                    transition-colors group-hover:bg-foreground group-focus-visible:bg-foreground">
                    <PlayIcon className="size-8 fill-current" aria-hidden="true"/>
                </span>
            </span>
        </button>
    );
}
