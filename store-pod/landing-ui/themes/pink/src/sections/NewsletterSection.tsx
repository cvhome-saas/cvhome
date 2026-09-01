'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {Check} from 'lucide-react';
import type {SectionRenderProps} from '@store-front/theme';

/**
 * The capture form as a flooded coupon strip: the issue's field owns the region whole, display caps
 * for the head, a paper entry line, the ink field as the send. Submitting thanks the reader locally;
 * a real audience list is future wiring.
 */
export function NewsletterSection({section}: SectionRenderProps) {
    const t = useTranslations('COMPONENTS.NEWSLETTER');
    const [done, setDone] = useState(false);
    const heading = section.text.heading ?? t('HEADING');
    return (
        <div className="flood tone mx-auto max-w-xl p-6 text-center sm:p-8">
            <h2 className="display text-2xl sm:text-3xl"><bdi dir="auto">{heading}</bdi></h2>
            {section.text.body && <p className="mt-1 text-sm"><bdi dir="auto">{section.text.body}</bdi></p>}
            {done ? (
                <p className="mt-4 inline-flex items-center gap-1.5 text-sm font-bold">
                    <Check aria-hidden className="size-4"/>{t('THANKS')}
                </p>
            ) : (
                <form className="mt-5 flex gap-2" onSubmit={event => { event.preventDefault(); setDone(true); }}>
                    <input type="email" required placeholder={t('EMAIL')}
                           className="h-11 min-w-0 flex-1 border border-transparent bg-background px-3 text-sm text-foreground outline-none focus-visible:border-foreground"/>
                    <button type="submit" className="ink-field h-11 px-5 text-sm font-bold uppercase tracking-wide">
                        {section.text.cta ?? t('SUBSCRIBE')}
                    </button>
                </form>
            )}
        </div>
    );
}
