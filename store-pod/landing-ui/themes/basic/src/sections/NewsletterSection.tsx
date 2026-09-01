'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {Check} from 'lucide-react';
import type {SectionRenderProps} from '@store-front/theme';

/**
 * The capture form in the catalogue's plain register: a bordered card, display caps, one primary
 * button. Submitting thanks the reader locally; a real audience list is future wiring.
 */
export function NewsletterSection({section}: SectionRenderProps) {
    const t = useTranslations('COMPONENTS.NEWSLETTER');
    const [done, setDone] = useState(false);
    const heading = section.text.heading ?? t('HEADING');
    return (
        <div className="mx-auto max-w-xl border p-6 text-center sm:p-8">
            <h2 className="font-display text-2xl uppercase"><bdi dir="auto">{heading}</bdi></h2>
            {section.text.body && <p className="mt-1 text-sm text-muted-foreground"><bdi dir="auto">{section.text.body}</bdi></p>}
            {done ? (
                <p className="mt-4 inline-flex items-center gap-1.5 text-sm font-medium">
                    <Check aria-hidden className="size-4"/>{t('THANKS')}
                </p>
            ) : (
                <form className="mt-5 flex gap-2" onSubmit={event => { event.preventDefault(); setDone(true); }}>
                    <input type="email" required placeholder={t('EMAIL')}
                           className="h-11 min-w-0 flex-1 border bg-background px-3 text-sm outline-none focus-visible:border-foreground"/>
                    <button type="submit" className="h-11 bg-primary px-5 text-sm font-semibold uppercase tracking-wide text-primary-foreground">
                        {section.text.cta ?? t('SUBSCRIBE')}
                    </button>
                </form>
            )}
        </div>
    );
}
