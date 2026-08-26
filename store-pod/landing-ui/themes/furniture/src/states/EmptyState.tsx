'use client'
import type {ReactNode} from 'react';
import {useTranslations} from 'next-intl';
import type {EmptyKind} from '@store-front/theme';
import {EmptyState as EmptyBlock} from '@store-front/ui/empty-state';

/** Nothing on this floor — said on a plate, the way a closed department is signed. */
export function EmptyState({kind, action}: { kind: EmptyKind; action?: ReactNode }) {
    const t = useTranslations('STATES');
    const K = kind.toUpperCase();
    return (
        <EmptyBlock className="rule-brass items-start border border-dashed px-6 py-12 text-start lg:px-10 lg:py-16"
                    title={t(`EMPTY_${K}_TITLE`)} action={action}>
            {t(`EMPTY_${K}_BODY`)}
        </EmptyBlock>
    );
}
