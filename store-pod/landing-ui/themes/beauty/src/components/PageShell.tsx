import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/** The stockroom grid: content runs wide, gutters are the plate spacing. */
export function PageShell({children, className, width = 'wide'}: { children: ReactNode; className?: string; width?: 'narrow' | 'content' | 'wide' | 'fluid' }) {
    return (
        <div className={cn('mx-auto w-full px-gutter', width === 'narrow' && 'max-w-narrow', width === 'content' && 'max-w-content', width === 'wide' && 'max-w-wide', className)}>
            {children}
        </div>
    );
}
