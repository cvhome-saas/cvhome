import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import type {SectionRenderProps} from '@store-front/theme';
import {items, linkHref, mediaUrl} from '../support';
import {EmptyOrHint, SectionHeading} from './shared';

function Brands({section, preview}: SectionRenderProps) {
    const logos = items(section).filter(logo => mediaUrl(logo.props));
    if (logos.length === 0) {
        return <EmptyOrHint preview={preview} label="Brand logos — add one"/>;
    }
    return (
        <div>
            <SectionHeading title={section.text.title}/>
            <div className="flex flex-wrap items-center justify-center gap-8 opacity-80">
                {logos.map(logo => {
                    const href = linkHref(logo.props.link);
                    const img = (
                        <span className="relative block h-10 w-24">
                            <Image src={mediaUrl(logo.props)!} alt="" fill className="object-contain"/>
                        </span>
                    );
                    return href === '#'
                        ? <span key={logo.id}>{img}</span>
                        : <Link key={logo.id} href={href}>{img}</Link>;
                })}
            </div>
        </div>
    );
}

export const brandsFallback = {row: Brands};
