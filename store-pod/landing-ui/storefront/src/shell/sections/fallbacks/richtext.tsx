import type {SectionRenderProps} from '@store-front/theme';
import {EmptyOrHint, SectionHeading} from './shared';

function RichText({section, preview}: SectionRenderProps) {
    if (!section.text.body && !section.text.title) {
        return <EmptyOrHint preview={preview} label="Rich text — write something"/>;
    }
    const centered = section.variant === 'centered';
    return (
        <div className={centered ? 'mx-auto max-w-prose text-center' : 'max-w-prose'}>
            <SectionHeading title={section.text.title}/>
            {section.text.body && (
                // CMS-authored HTML, sanitized by the content service on write.
                <div className="prose-sm leading-relaxed [&_a]:underline"
                     dangerouslySetInnerHTML={{__html: section.text.body}}/>
            )}
        </div>
    );
}

export const richtextFallback = {default: RichText, centered: RichText};
