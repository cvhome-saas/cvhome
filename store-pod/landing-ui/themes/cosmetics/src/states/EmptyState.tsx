'use client'
import type {ReactNode} from 'react';
import {useTranslations} from 'next-intl';
import {PackageOpenIcon, ReceiptTextIcon, SearchIcon, ShoppingBagIcon} from 'lucide-react';
import type {EmptyKind} from '@store-front/theme';
import {EmptyState as EmptyBlock} from '@store-front/ui/empty-state';

const ICONS = {cart: ShoppingBagIcon, listing: PackageOpenIcon, orders: ReceiptTextIcon, search: SearchIcon};

export function EmptyState({kind, action}: { kind: EmptyKind; action?: ReactNode }) {
    const t = useTranslations('STATES');
    const Icon = ICONS[kind];
    const K = kind.toUpperCase();
    return <EmptyBlock icon={<Icon/>} title={t(`EMPTY_${K}_TITLE`)} action={action}>{t(`EMPTY_${K}_BODY`)}</EmptyBlock>;
}
