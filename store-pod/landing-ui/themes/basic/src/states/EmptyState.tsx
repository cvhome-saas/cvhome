'use client'
import type {ReactNode} from 'react';
import {useTranslations} from 'next-intl';
import {PackageOpenIcon, ReceiptTextIcon, SearchIcon, ShoppingBagIcon} from 'lucide-react';
import type {EmptyKind} from '@store-front/theme';
import {EmptyState as EmptyBlock} from '@store-front/ui/empty-state';

const ICONS = {cart: ShoppingBagIcon, listing: PackageOpenIcon, orders: ReceiptTextIcon, search: SearchIcon};

/** A blank page of the catalogue: the fact printed in the display voice, the way forward under it. */
export function EmptyState({kind, action}: { kind: EmptyKind; action?: ReactNode }) {
    const t = useTranslations('STATES');
    const Icon = ICONS[kind];
    const K = kind.toUpperCase();
    return (
        <EmptyBlock icon={<Icon strokeWidth={1.5}/>} title={t(`EMPTY_${K}_TITLE`)} action={action}
                    className="px-6 py-14 [&_[data-slot=empty-state-title]]:font-display [&_[data-slot=empty-state-title]]:text-2xl [&_[data-slot=empty-state-title]]:uppercase">
            {t(`EMPTY_${K}_BODY`)}
        </EmptyBlock>
    );
}
