'use client'
import {useTranslations} from 'next-intl';
import {Loader2Icon} from 'lucide-react';
import type {RedirectReason} from '@store-front/theme';
import {PageShell} from '../components/PageShell';

export function Redirecting({reason}: { reason: RedirectReason }) {
    const t = useTranslations('STATES');
    return (
        <PageShell width="narrow" className="flex min-h-[50vh] flex-col items-center justify-center" aria-busy aria-live="polite">
            <p className="strip h-11 gap-3 text-sm [--tilt:-0.6deg]">
                <Loader2Icon className="size-4 animate-spin text-primary" aria-hidden/>
                {reason === 'callback' ? t('AUTHENTICATING') : t('REDIRECTING_LOGIN')}
            </p>
        </PageShell>
    );
}
