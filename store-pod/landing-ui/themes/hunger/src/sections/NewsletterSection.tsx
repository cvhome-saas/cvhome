'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {Check} from 'lucide-react';
import type {SectionRenderProps} from '@store-front/theme';

/**
 * The capture form printed as a coupon on the sheet: press caps, a ruled entry line, the fold as the
 * clip-and-send. Zero radius like everything on the menu. Submitting thanks the reader locally; a
 * real audience list is future wiring.
 */
export function NewsletterSection({section}: SectionRenderProps) {
    const t = useTranslations('COMPONENTS.NEWSLETTER');
    const [done, setDone] = useState(false);
    const heading = section.text.heading ?? t('HEADING');
    return (
        <div className="mx-auto max-w-xl border-2 border-dashed border-foreground/50 p-6 text-center sm:p-8">
            <h2 className="press text-2xl"><bdi dir="auto">{heading}</bdi></h2>
            {section.text.body && <p className="mt-1 text-sm text-muted-foreground"><bdi dir="auto">{section.text.body}</bdi></p>}
            {done ? (
                <p className="mt-4 inline-flex items-center gap-1.5 text-sm font-bold">
                    <Check aria-hidden className="size-4"/>{t('THANKS')}
                </p>
            ) : (
                <form className="mt-5 flex gap-2" onSubmit={event => { event.preventDefault(); setDone(true); }}>
                    <input type="email" required placeholder={t('EMAIL')}
                           className="h-11 min-w-0 flex-1 rounded-none border border-foreground/45 bg-background px-3 text-sm outline-none focus-visible:border-foreground"/>
                    <button type="submit" className="fold px-5 text-sm">
                        {section.text.cta ?? t('SUBSCRIBE')}
                    </button>
                </form>
            )}
        </div>
    );
}
