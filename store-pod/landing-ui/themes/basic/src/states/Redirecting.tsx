'use client'
import {useTranslations} from 'next-intl';
import {Loader2Icon} from 'lucide-react';
import type {RedirectReason} from '@store-front/theme';
import {PageShell} from '../components/PageShell';

export function Redirecting({reason}: { reason: RedirectReason }) {
    const t = useTranslations('STATES');
    return (
        <PageShell width="narrow" className="flex min-h-[50vh] flex-col items-center justify-center gap-3 text-center" aria-busy aria-live="polite">
            <Loader2Icon className="size-8 animate-spin text-primary" aria-hidden/>
            <p className="display text-xl text-muted-foreground">{reason === 'callback' ? t('AUTHENTICATING') : t('REDIRECTING_LOGIN')}</p>
        </PageShell>
    );
}
