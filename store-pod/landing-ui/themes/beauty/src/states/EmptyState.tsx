'use client'
import type {ReactNode} from 'react';
import {useTranslations} from 'next-intl';
import type {EmptyKind} from '@store-front/theme';

/** An empty plate: the stripe where goods would be, the quoted reason, an action tag. */
export function EmptyState({kind, action}: { kind: EmptyKind; action?: ReactNode }) {
    const t = useTranslations('STATES');
    const K = kind.toUpperCase();
    return (
        <section role="status" className="flex flex-col items-center gap-3 py-10 text-center">
            <div className="hazard-soft h-10 w-28 border border-foreground" aria-hidden/>
            <h2 className="q font-display text-xl font-semibold uppercase tracking-tight">{t(`EMPTY_${K}_TITLE`)}</h2>
            <p className="max-w-prose font-mono text-xs uppercase tracking-wide text-muted-foreground">{t(`EMPTY_${K}_BODY`)}</p>
            {action && <div className="mt-2 flex flex-wrap justify-center gap-2">{action}</div>}
        </section>
    );
}
