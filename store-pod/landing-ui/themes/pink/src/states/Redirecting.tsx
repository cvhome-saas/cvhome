'use client'
import {useTranslations} from 'next-intl';
import type {RedirectReason} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {StarMark} from '../components/Marks';

/** Between pages: the issue's mark turning while the shopper is handed to the login server. */
export function Redirecting({reason}: { reason: RedirectReason }) {
    const t = useTranslations('STATES');
    return (
        <PageShell width="narrow" className="flex min-h-[50vh] flex-col items-center justify-center gap-4 text-center" aria-busy aria-live="polite">
            <span className="flood inline-flex size-14 items-center justify-center">
                <StarMark className="size-7 animate-spin [animation-duration:2.4s] motion-reduce:animate-none" aria-hidden/>
            </span>
            <p className="cover-line text-muted-foreground">{reason === 'callback' ? t('AUTHENTICATING') : t('REDIRECTING_LOGIN')}</p>
        </PageShell>
    );
}
