import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import type {SectionRenderProps} from '@store-front/theme';
import {linkHref, mediaUrl} from '../support';
import {EmptyOrHint} from './shared';

function ImageSection({section, preview}: SectionRenderProps) {
    const src = mediaUrl(section.props);
    if (!src) {
        return <EmptyOrHint preview={preview} label="Image — pick one from the media library"/>;
    }
    const alt = typeof section.props.alt === 'string' ? section.props.alt : section.text.caption ?? '';
    const href = linkHref(section.props.link);
    const figure = (
        <figure>
            <div className="relative aspect-[21/9] w-full overflow-hidden rounded-md bg-muted">
                <Image src={src} alt={alt} fill className="object-cover"/>
            </div>
            {section.text.caption && (
                <figcaption className="mt-2 text-center text-sm text-muted-foreground">
                    <bdi dir="auto">{section.text.caption}</bdi>
                </figcaption>
            )}
        </figure>
    );
    return href === '#' ? figure : <Link href={href} className="block">{figure}</Link>;
}

export const imageFallback = {full: ImageSection, contained: ImageSection};
