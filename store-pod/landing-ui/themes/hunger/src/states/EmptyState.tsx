'use client'
import type {ReactNode} from 'react';
import {useTranslations} from 'next-intl';
import type {EmptyKind} from '@store-front/theme';

/**
 * An empty stretch of the sheet, printed rather than illustrated: a ruled panel with the reason set in
 * the same voice as a section band. No icon-in-a-circle — nothing on a menu is drawn in a circle.
 */
export function EmptyState({kind, action}: { kind: EmptyKind; action?: ReactNode }) {
    const t = useTranslations('STATES');
    const K = kind.toUpperCase();
    return (
        <div className="flex flex-col items-start gap-3 border border-dashed border-foreground/45 px-5 py-8 text-start">
            <p className="press text-2xl leading-none">{t(`EMPTY_${K}_TITLE`)}</p>
            <p className="max-w-sm text-sm text-muted-foreground">{t(`EMPTY_${K}_BODY`)}</p>
            {action}
        </div>
    );
}
