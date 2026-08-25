'use client'
import type {ReactNode} from 'react';
import {useTranslations} from 'next-intl';
import type {EmptyKind} from '@store-front/theme';
import {EmptyState as EmptyBlock} from '@store-front/ui/empty-state';

/** An empty stretch of wall: a small sheet with the title as a stamp. */
export function EmptyState({kind, action}: { kind: EmptyKind; action?: ReactNode }) {
    const t = useTranslations('STATES');
    const K = kind.toUpperCase();
    return (
        <EmptyBlock className="sheet stamp-title mx-auto w-full max-w-md px-6 [--tilt:-0.5deg]" title={t(`EMPTY_${K}_TITLE`)} action={action}>
            {t(`EMPTY_${K}_BODY`)}
        </EmptyBlock>
    );
}
