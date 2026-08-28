'use client'
import {useTranslations} from 'next-intl';
import type {RedirectReason} from '@store-front/theme';
import {PageShell} from '../components/PageShell';

/** The press is still running: a plate wiping across the line, on the same clock as an add. */
export function Redirecting({reason}: { reason: RedirectReason }) {
    const t = useTranslations('STATES');
    return (
        <PageShell width="narrow" className="flex min-h-[50vh] flex-col items-center justify-center gap-4 text-center" aria-busy aria-live="polite">
            <span aria-hidden className="relative block h-1.5 w-40 overflow-hidden bg-[var(--wash)]">
                <span className="absolute inset-y-0 w-1/3 animate-[hunger-wipe_1.1s_var(--easing-standard)_infinite] bg-primary"/>
            </span>
            <p className="press text-sm tracking-wide text-muted-foreground">
                {reason === 'callback' ? t('AUTHENTICATING') : t('REDIRECTING_LOGIN')}
            </p>
        </PageShell>
    );
}
