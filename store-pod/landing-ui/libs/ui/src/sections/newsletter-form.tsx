'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {Check} from 'lucide-react';

/**
 * The one newsletter capture form every theme shares: submitting thanks the shopper locally (wiring
 * a real audience list is the newsletter feature's job, not the layout's), copy comes from the
 * shared COMPONENTS.NEWSLETTER keys, and the theme's voice arrives as class strings through the
 * section chrome — never as a fork of this file.
 */
export function NewsletterForm({cta, inputClassName, buttonClassName}: {
    cta?: string;
    inputClassName: string;
    buttonClassName: string;
}) {
    const t = useTranslations('COMPONENTS.NEWSLETTER');
    const [done, setDone] = useState(false);
    if (done) {
        return (
            <p className="mt-4 inline-flex items-center gap-1.5 text-sm font-medium">
                <Check aria-hidden className="size-4"/>{t('THANKS')}
            </p>
        );
    }
    return (
        <form className="mt-5 flex gap-2" onSubmit={event => { event.preventDefault(); setDone(true); }}>
            <input type="email" required placeholder={t('EMAIL')} className={inputClassName}/>
            <button type="submit" className={buttonClassName}>{cta ?? t('SUBSCRIBE')}</button>
        </form>
    );
}
