'use client'
import {useTranslations} from 'next-intl';
import type {RedirectReason} from '@store-front/theme';
import {PageShell} from '../components/PageShell';

export function Redirecting({reason}: { reason: RedirectReason }) {
    const t = useTranslations('STATES');
    return (
        <PageShell width="narrow" className="flex min-h-[50vh] flex-col items-center justify-center gap-4 text-center" aria-busy aria-live="polite">
            <div className="hazard-soft h-2 w-32 animate-pulse border border-foreground" aria-hidden/>
            <p className="q font-mono text-xs uppercase tracking-wide text-muted-foreground">{reason === 'callback' ? t('AUTHENTICATING') : t('REDIRECTING_LOGIN')}</p>
        </PageShell>
    );
}
