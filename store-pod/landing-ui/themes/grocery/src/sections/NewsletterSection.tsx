'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {Check} from 'lucide-react';
import type {SectionRenderProps} from '@store-front/theme';

/**
 * The capture form as a coupon on the price board: signage heading on the primary field, the entry
 * line in a plain cell, and a plain (never a second primary) redeem button — the board itself is the
 * primary spend. Submitting thanks the shopper locally; a real audience list is future wiring.
 */
export function NewsletterSection({section}: SectionRenderProps) {
    const t = useTranslations('COMPONENTS.NEWSLETTER');
    const [done, setDone] = useState(false);
    const heading = section.text.heading ?? t('HEADING');
    return (
        <div className="board mx-auto max-w-xl p-6 text-center sm:p-8">
            <h2 className="signage text-2xl uppercase"><bdi dir="auto">{heading}</bdi></h2>
            {section.text.body && <p className="mt-1 text-sm opacity-90"><bdi dir="auto">{section.text.body}</bdi></p>}
            {done ? (
                <p className="mt-4 inline-flex items-center gap-1.5 text-sm font-semibold">
                    <Check aria-hidden className="size-4"/>{t('THANKS')}
                </p>
            ) : (
                <form className="mt-5 flex gap-2" onSubmit={event => { event.preventDefault(); setDone(true); }}>
                    <input type="email" required placeholder={t('EMAIL')}
                           className="h-11 min-w-0 flex-1 rounded-(--r-control) border-2 border-transparent bg-background px-3 text-sm text-foreground outline-none focus-visible:border-foreground"/>
                    <button type="submit" className="h-11 rounded-(--r-control) bg-foreground px-5 text-sm font-bold uppercase tracking-wide text-background">
                        {section.text.cta ?? t('SUBSCRIBE')}
                    </button>
                </form>
            )}
        </div>
    );
}
