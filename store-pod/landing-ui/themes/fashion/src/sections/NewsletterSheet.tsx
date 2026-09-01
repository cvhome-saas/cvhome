'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {Check} from 'lucide-react';
import type {SectionRenderProps} from '@store-front/theme';

/**
 * The capture form as a sheet on the wall: poster-caps heading, the pencil-box input, the day-glo
 * submit. Submitting thanks the shopper locally — wiring a real audience list is the newsletter
 * feature's job, not the layout's.
 */
export function NewsletterSheet({section}: SectionRenderProps) {
    const t = useTranslations('COMPONENTS.NEWSLETTER');
    const [done, setDone] = useState(false);
    const heading = section.text.heading ?? t('HEADING');
    return (
        <div className="sheet sheen peel mx-auto max-w-xl p-6 text-center sm:p-8 [--tilt:-0.5deg]">
            <h2 className="font-display text-2xl uppercase leading-tight"><bdi dir="auto">{heading}</bdi></h2>
            {section.text.body && (
                <p className="mt-1 text-sm text-muted-foreground"><bdi dir="auto">{section.text.body}</bdi></p>
            )}
            {done ? (
                <p className="mt-4 inline-flex items-center gap-1.5 text-sm font-medium">
                    <Check aria-hidden className="size-4"/>{t('THANKS')}
                </p>
            ) : (
                <form className="mt-5 flex gap-2" onSubmit={event => { event.preventDefault(); setDone(true); }}>
                    <input type="email" required placeholder={t('EMAIL')}
                           className="h-11 min-w-0 flex-1 border border-foreground/45 bg-background px-3 text-sm outline-none focus-visible:border-primary focus-visible:ring-[3px] focus-visible:ring-primary/35"/>
                    <button type="submit" className="glo h-11 px-5 text-sm">
                        {section.text.cta ?? t('SUBSCRIBE')}
                    </button>
                </form>
            )}
        </div>
    );
}
