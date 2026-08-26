'use client'
import {useTranslations} from 'next-intl';
import type {RedirectReason} from '@store-front/theme';
import {PageShell} from '../components/PageShell';

/** Between floors. The bar fills the way a lift indicator does — the only place this theme animates twice. */
export function Redirecting({reason}: { reason: RedirectReason }) {
    const t = useTranslations('STATES');
    return (
        <PageShell width="narrow" className="flex min-h-[50vh] flex-col items-center justify-center gap-5 text-center" aria-busy aria-live="polite">
            <span aria-hidden className="rule-brass h-1.5 w-40 overflow-hidden border bg-background">
                <span className="lift-bar block h-full w-1/3 bg-primary"/>
            </span>
            <p className="sign text-[0.6875rem] text-muted-foreground">{reason === 'callback' ? t('AUTHENTICATING') : t('REDIRECTING_LOGIN')}</p>
        </PageShell>
    );
}
