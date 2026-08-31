import type {SectionRenderProps} from '@store-front/theme';
import {EmptyOrHint, SectionHeading} from './shared';

/** YouTube/Vimeo page URL → privacy-friendly embed URL; undefined for anything else. */
export function embedUrl(raw: unknown): string | undefined {
    if (typeof raw !== 'string' || !raw) return undefined;
    try {
        const url = new URL(raw);
        const host = url.hostname.replace(/^www\./, '');
        if (host === 'youtube.com' && url.searchParams.get('v')) {
            return `https://www.youtube-nocookie.com/embed/${url.searchParams.get('v')}`;
        }
        if (host === 'youtu.be' && url.pathname.length > 1) {
            return `https://www.youtube-nocookie.com/embed/${url.pathname.slice(1)}`;
        }
        if (host === 'vimeo.com' && /^\/\d+/.test(url.pathname)) {
            return `https://player.vimeo.com/video/${url.pathname.slice(1)}`;
        }
    } catch {
        return undefined;
    }
    return undefined;
}

function Video({section, preview}: SectionRenderProps) {
    const src = embedUrl(section.props.url);
    if (!src) {
        return <EmptyOrHint preview={preview} label="Video — paste a YouTube or Vimeo link"/>;
    }
    return (
        <div className="mx-auto max-w-3xl">
            <SectionHeading title={section.text.title}/>
            <div className="aspect-video w-full overflow-hidden rounded-md bg-muted">
                <iframe src={src} title={section.text.title ?? 'Video'} className="size-full"
                        allow="accelerometer; encrypted-media; picture-in-picture" allowFullScreen
                        loading="lazy" referrerPolicy="no-referrer"/>
            </div>
        </div>
    );
}

export const videoFallback = {embed: Video};
